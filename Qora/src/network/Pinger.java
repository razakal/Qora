package network;

import settings.Settings;
import network.message.Message;
import network.message.MessageFactory;

public class Pinger extends Thread
{
	private Peer peer;
	private boolean run;
	
	public Pinger(Peer peer)
	{
		this.peer = peer;
		this.run = true;
		
		this.start();
	}
	
	public void run()
	{
		while(this.run)
		{
			//CREATE PING
			Message ping = MessageFactory.getInstance().createPingMessage();
			
			//GET RESPONSE
			Message response = this.peer.getResponse(ping);
			
			//CHECK IF VALID PING
			if(response == null || response.getType() != Message.PING_TYPE)
			{
				//PING FAILES
				this.peer.onPingFail();
				
				//STOP PINGER
				this.run = false;
				return;
			}
			
			//SLEEP
			try 
			{
				Thread.sleep(Settings.getInstance().getPingInterval());
			} 
			catch (InterruptedException e)
			{
				//FAILED TO SLEEP
			}
		}
	}

	public void stopPing() 
	{
		try
		{
			this.run = false;
			this.interrupt();
			this.join();
		}
		catch(Exception e)
		{
			
		}
	}

}
