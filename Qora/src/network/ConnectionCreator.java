package network;

import java.util.List;
import java.util.logging.Logger;

import network.message.Message;
import network.message.MessageFactory;
import network.message.PeersMessage;
import settings.Settings;

public class ConnectionCreator extends Thread {

	private ConnectionCallback callback;
	
	public ConnectionCreator(ConnectionCallback callback)
	{
		this.callback = callback;
	}
	
	public void run()
	{
		try
		{	
			while(true)
			{
				//CHECK IF WE NEED NEW CONNECTIONS
				if(Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size())
				{			
					//GET LIST OF KNOWN PEERS
					List<Peer> knownPeers = PeerManager.getInstance().getKnownPeers();
					
					//ITERATE knownPeers
					for(Peer peer: knownPeers)
					{
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
						{
							//CHECK IF ALREADY CONNECTED TO PEER
							if(!callback.isConnectedTo(peer.getAddress()))
							{							
								//CHECK IF SOCKET IS NOT LOCALHOST
								if(true)
								//if(!peer.getAddress().isSiteLocalAddress() && !peer.getAddress().isLoopbackAddress() && !peer.getAddress().isAnyLocalAddress())
								{
									//CONNECT
									Logger.getGlobal().info("Connecting to peer : " + peer.getAddress());
								
									peer.connect(callback);
								}
							}
						}
					}
				}
				
				//CHECK IF WE STILL NEED NEW CONNECTIONS
				if(Settings.getInstance().getMinConnections() >= callback.getActiveConnections().size())
				{
					//OLD SCHOOL ITERATE activeConnections
					//avoids Exception when adding new elements
					for(int i=0; i<callback.getActiveConnections().size(); i++)
					{
						Peer peer = callback.getActiveConnections().get(i);
	
						//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
						if(Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
						{
								//ASK PEER FOR PEERS
								Message getPeersMessage = MessageFactory.getInstance().createGetPeersMessage();
								PeersMessage peersMessage = (PeersMessage) peer.getResponse(getPeersMessage);
								if(peersMessage != null)
								{
									//FOR ALL THE RECEIVED PEERS
									for(Peer newPeer: peersMessage.getPeers())
									{		
										//CHECK IF WE ALREADY HAVE MAX CONNECTIONS
										if(Settings.getInstance().getMaxConnections() > callback.getActiveConnections().size())
										{
											//CHECK IF THAT PEER IS NOT BLACKLISTED
											if(!PeerManager.getInstance().isBlacklisted(newPeer))
											{
												//CHECK IF CONNECTED
												if(!callback.isConnectedTo(newPeer))
												{
													//CHECK IF SOCKET IS NOT LOCALHOST
													if(!newPeer.getAddress().isSiteLocalAddress() && !newPeer.getAddress().isLoopbackAddress() && !newPeer.getAddress().isAnyLocalAddress())
													{
														Logger.getGlobal().info("Connecting to peer : " + newPeer.getAddress());
														
														//CONNECT
														newPeer.connect(callback);
													}
												}
											}
										}
									}									
								}					
							
						}
					}
				}			
				//SLEEP
				Thread.sleep(60 * 1000);	
			}
			
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			Logger.getGlobal().info("Error creating new connection");			
		}					
	}
	
}
