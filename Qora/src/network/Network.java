package network;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.SortedSet;
import java.util.TreeSet;

import lang.Lang;
import network.message.FindMyselfMessage;
import network.message.Message;
import network.message.MessageFactory;

import org.apache.log4j.Logger;

import utils.ObserverMessage;
import controller.Controller;

public class Network extends Observable implements ConnectionCallback {

	
	private static final Logger LOGGER = Logger.getLogger(Network.class);
	public static final int MAINNET_PORT = 9084;
	public static final int TESTNET_PORT = 4809;
	
	private static final int MAX_HANDLED_MESSAGES_SIZE = 10000;
	
	private ConnectionCreator creator;
	private ConnectionAcceptor acceptor;
	
	private List<Peer> connectedPeers;
	
	private SortedSet<String> handledMessages;
	
	private boolean run;
	
	public Network()
	{	
		this.connectedPeers = new ArrayList<Peer>();
		this.run = true;
		
		this.start();
	}
	
	private void start()
	{
		this.handledMessages = Collections.synchronizedSortedSet(new TreeSet<String>());
		
		//START ConnectionCreator THREAD
		creator = new ConnectionCreator(this);
		creator.start();
		
		//START ConnectionAcceptor THREAD
		acceptor = new ConnectionAcceptor(this);
		acceptor.start();
	}

	@Override
	public void onConnect(Peer peer) {
		
		LOGGER.info(Lang.getInstance().translate("Connection successfull : ") + peer.getAddress());
		
		//ADD TO CONNECTED PEERS
		synchronized(this.connectedPeers)
		{
			this.connectedPeers.add(peer);
		}
		
		//ADD TO WHITELIST
		PeerManager.getInstance().addPeer(peer);
		
		//PASS TO CONTROLLER
		Controller.getInstance().onConnect(peer);
		
		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_PEER_TYPE, peer));		
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.connectedPeers));		
	}

	@Override
	public void onDisconnect(Peer peer) {

		LOGGER.info(Lang.getInstance().translate("Connection close : ") + peer.getAddress());
		
		//REMOVE FROM CONNECTED PEERS
		synchronized(this.connectedPeers)
		{
			this.connectedPeers.remove(peer);
		}
		
		//PASS TO CONTROLLER
		Controller.getInstance().onDisconnect(peer);
		
		//CLOSE CONNECTION IF STILL ACTIVE
		peer.close();
		peer.interrupt();
		
		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));		
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.connectedPeers));		
	}
	
	@Override
	public void onError(Peer peer, String error) {
		
		LOGGER.warn(Lang.getInstance().translate("Connection error : ") + peer.getAddress() + " : " + error);
		
		//REMOVE FROM CONNECTED PEERS
		synchronized(this.connectedPeers)
		{
			this.connectedPeers.remove(peer);
		}
		
		//ADD TO BLACKLIST
		PeerManager.getInstance().blacklistPeer(peer);
		
		//PASS TO CONTROLLER
		Controller.getInstance().onError(peer);
		
		//CLOSE CONNECTION IF STILL ACTIVE
		peer.close();
		peer.interrupt();
					
		//NOTIFY OBSERVERS
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_PEER_TYPE, peer));		
		
		this.setChanged();
		this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.connectedPeers));		
	}
	
	@Override
	public boolean isConnectedTo(InetAddress address) {
		
		try
		{
			synchronized(this.connectedPeers)
			{
				//FOR ALL connectedPeers
				for(Peer connectedPeer: connectedPeers)
				{
					//CHECK IF ADDRESS IS THE SAME
					if(address.equals(connectedPeer.getAddress()))
					{
						return true;
					}
				}
			}
		}
		catch(Exception e)
		{
			//CONCURRENCY ERROR
		}
		
		return false;
	}
	
	@Override
	public boolean isConnectedTo(Peer peer) {
		
		return this.isConnectedTo(peer.getAddress());
	}
	
	@Override
	public List<Peer> getActiveConnections() {
		
		return this.connectedPeers;
	}
	
	private void addHandledMessage(byte[] hash)
	{
		try
		{
			synchronized(this.handledMessages)
			{
				//CHECK IF LIST IS FULL
				if(this.handledMessages.size() > MAX_HANDLED_MESSAGES_SIZE)
				{
					this.handledMessages.remove(this.handledMessages.first());
				}
				
				this.handledMessages.add(new String(hash));
			}
		}
		catch(Exception e)
		{
			LOGGER.error(e.getMessage(),e);
		}
	}

	@Override
	public void onMessage(Message message) {
	
		//CHECK IF WE ARE STILL PROCESSING MESSAGES
		if(!this.run)
		{
			return;
		}
		
		//ONLY HANDLE BLOCK AND TRANSACTION MESSAGES ONCE
		if(message.getType() == Message.TRANSACTION_TYPE || message.getType() == Message.BLOCK_TYPE)
		{
			synchronized(this.handledMessages)
			{
				//CHECK IF NOT HANDLED ALREADY
				if(this.handledMessages.contains(new String(message.getHash())))
				{
					return;
				}
				
				//ADD TO HANDLED MESSAGES
				this.addHandledMessage(message.getHash());
			}
		}		
		
		switch(message.getType())
		{
		//PING
		case Message.PING_TYPE:
			
			//CREATE PING
			Message response = MessageFactory.getInstance().createPingMessage();
			
			//SET ID
			response.setId(message.getId());
			
			//SEND BACK TO SENDER
			message.getSender().sendMessage(response);
			
			break;
		
		//GETPEERS
		case Message.GET_PEERS_TYPE: 
			
			//CREATE NEW PEERS MESSAGE WITH PEERS
			Message answer = MessageFactory.getInstance().createPeersMessage(PeerManager.getInstance().getBestPeers());
			answer.setId(message.getId());
			
			//SEND TO SENDER
			message.getSender().sendMessage(answer);
			break;
			
			
		case Message.FIND_MYSELF_TYPE:

			FindMyselfMessage findMyselfMessage = (FindMyselfMessage) message;
			
			if(Arrays.equals(findMyselfMessage.getFoundMyselfID(),Controller.getInstance().getFoundMyselfID())) {
				LOGGER.info(Lang.getInstance().translate("Connected to self. Disconnection."));
				message.getSender().close();
			}
			
			break;
			
		//SEND TO CONTROLLER
		default:
			
			Controller.getInstance().onMessage(message);
			break;
		}		
	}

	public void broadcast(Message message, List<Peer> exclude) 
	{		
		LOGGER.info(Lang.getInstance().translate("Broadcasting"));
		
		try
		{
			for(int i=0; i < this.connectedPeers.size() ; i++)
			{
				Peer peer = this.connectedPeers.get(i);
				
				//EXCLUDE PEERS
				if(peer != null && !exclude.contains(peer))
				{
					peer.sendMessage(message);
				}
			}	
		}
		catch(Exception e)
		{
			//error broadcasting
			LOGGER.error(e.getMessage(),e);
		}
		
		LOGGER.info(Lang.getInstance().translate("Broadcasting end"));
	}
	
	@Override
	public void addObserver(Observer o)
	{
		super.addObserver(o);
		
		//SEND CONNECTEDPEERS ON REGISTER
		o.update(this, new ObserverMessage(ObserverMessage.LIST_PEER_TYPE, this.connectedPeers));
	}
	
	public static boolean isPortAvailable(int port)
	{
		try 
		{
		    ServerSocket socket = new ServerSocket(port);
		    socket.close();
		    return true;
		} 
		catch (Exception e)
		{
		    return false;
		}
	}

	public void stop() 
	{
		this.run = false;
		this.onMessage(null);
		while (this.connectedPeers.size() > 0) {
			try {
				this.connectedPeers.get(0).close();
			} catch (Exception e) {
				LOGGER.debug(e.getMessage(),e);
			}
		}
		this.acceptor.halt();
		this.creator.halt();
	}
}
