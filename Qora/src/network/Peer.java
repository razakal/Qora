package network;

import java.io.DataInputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import settings.Settings;
import network.message.Message;
import network.message.MessageFactory;

public class Peer extends Thread{

	private InetAddress address;
	private ConnectionCallback callback;
	private Socket socket;
	private OutputStream out;
	private Pinger pinger;
	
	private Map<Integer, BlockingQueue<Message>> messages;
	
	public Peer(InetAddress address)
	{
		this.address = address;
		this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
	}
	
	public Peer(ConnectionCallback callback, Socket socket)
	{
		try
		{	
			this.callback = callback;
			this.socket = socket;
			this.address = socket.getInetAddress();
			this.messages = Collections.synchronizedMap(new HashMap<Integer, BlockingQueue<Message>>());
			
			//ENABLE KEEPALIVE
			//this.socket.setKeepAlive(true);
			
			//TIMEOUT
			this.socket.setSoTimeout(1000*60*60);
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
			
			//START COMMUNICATON THREAD
			this.start();
			
			//START PINGER
			this.pinger = new Pinger(this);
			
			//ON SOCKET CONNECT
			this.callback.onConnect(this);			
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			Logger.getGlobal().info("Failed to connect to : " + address);
		}
	}
	
	public InetAddress getAddress()
	{
		return address;
	}
	
	public void connect(ConnectionCallback callback)
	{
		this.callback = callback;
		
		try
		{
			//OPEN SOCKET
			this.socket = new Socket(address, Network.PORT);
			
			//ENABLE KEEPALIVE
			//this.socket.setKeepAlive(true);
			
			//TIMEOUT
			this.socket.setSoTimeout(1000*60*60);
			
			//CREATE STRINGWRITER
			this.out = socket.getOutputStream();
			
			//START COMMUNICATON THREAD
			this.start();
			
			//START PINGER
			this.pinger = new Pinger(this);
			
			//ON SOCKET CONNECT
			this.callback.onConnect(this);			
		}
		catch(Exception e)
		{
			//FAILED TO CONNECT NO NEED TO BLACKLIST
			Logger.getGlobal().info("Failed to connect to : " + address);
		}
	}
	
	public void run()
	{
		try 
		{
			DataInputStream in = new DataInputStream(socket.getInputStream());
			
			while(true)
			{
				//READ FIRST 4 BYTES
				byte[] messageMagic = new byte[Message.MAGIC_LENGTH];
				in.readFully(messageMagic);
				
				if(Arrays.equals(messageMagic, Message.MAGIC))
				{
					//PROCESS NEW MESSAGE
					Message message = MessageFactory.getInstance().parse(this, in);
					
					//Logger.getGlobal().info("received message " + message.getType() + " from " + this.address.toString());
					
					//CHECK IF WE ARE WAITING FOR A MESSAGE WITH THAT ID
					if(message.hasId() && this.messages.containsKey(message.getId()))
					{
						//ADD TO OUR OWN LIST
						this.messages.get(message.getId()).add(message);
					}
					else
					{
						//CALLBACK
						this.callback.onMessage(message);
					}
				}
				else
				{
					Logger.getGlobal().warning("received message with wrong magic");
					
					//ERROR
					callback.onError(this);
					return;
				}
			}
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
			
			//DISCONNECT
			callback.onDisconnect(this);
			return;
		}
	}
	
	public boolean sendMessage(Message message)
	{
		try 
		{
			//CHECK IF SOCKET IS STILL ALIVE
			if(!this.socket.isConnected())
			{
				//ERROR
				callback.onError(this);
				
				return false;
			}
			
			//Logger.getGlobal().info("Sending message " + message.getType() + " to " + this.address.toString());
			
			//SEND MESSAGE
			synchronized(this.out)
			{
				this.out.write(message.toBytes());
				this.out.flush();
			}
			
			//Logger.getGlobal().info("Sent message " + message.getType() + " to " + this.address.toString());
			
			//RETURN
			return true;
		}
		catch (Exception e) 
		{
			e.printStackTrace();
			
			//ERROR
			callback.onError(this);
			
			//RETURN
			return false;
		}
	}
	
	public Message getResponse(Message message)
	{
		//GENERATE ID
		int id = (int) ((Math.random() * 1000000) + 1);
		
		//SET ID
		message.setId(id);
		
		//PUT QUEUE INTO MAP SO WE KNOW WE ARE WAITING FOR A RESPONSE
		BlockingQueue<Message> blockingQueue = new ArrayBlockingQueue<Message>(1);
		this.messages.put(id, blockingQueue);
		
		//WHEN FAILED TO SEND MESSAGE
		if(!this.sendMessage(message))
		{
			return null;
		}
		
		try 
		{
			Message response = blockingQueue.poll(Settings.getInstance().getConnectionTimeout(), TimeUnit.MILLISECONDS);
			this.messages.remove(id);
			
			return response;
		} 
		catch (InterruptedException e)
		{
			e.printStackTrace();
			//NO MESSAGE RECEIVED WITHIN TIME;
			return null;
		}
	}
	
	public void onPingFail()
	{
		//DISCONNECTED
		this.callback.onDisconnect(this);
	}

	public void close() 
	{
		try
		{
			//STOP PINGER
			if(this.pinger != null)
			{
				this.pinger.stopPing();
			}
			
			//CHECK IS SOCKET EXISTS
			if(socket != null)
			{
				//CHECK IF SOCKET IS CONNECTED
				if(socket.isConnected())
				{
					//CLOSE SOCKET
					socket.close();
				}
			}
		}
		catch(Exception e)
		{
			
		}		
	}
}
