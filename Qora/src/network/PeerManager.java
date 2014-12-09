package network;

import java.net.InetAddress;
import java.util.List;
import java.util.logging.Logger;

import database.DBSet;
import settings.Settings;

public class PeerManager {

	private static final int DATABASE_PEERS_AMOUNT = 1000;
	
	private static PeerManager instance;
	
	public static PeerManager getInstance()
	{
		if(instance == null)
		{
			instance = new PeerManager();
		}
		
		return instance;
	}
	
	private PeerManager()
	{
		
	}
	
	public List<Peer> getKnownPeers()
	{
		//ASK DATABASE FOR A LIST OF PEERS
		List<Peer> knownPeers = DBSet.getInstance().getPeerMap().getKnownPeers(DATABASE_PEERS_AMOUNT);
				
		Logger.getGlobal().info("Peers retrieved from database : " + knownPeers.size());
				
		//IF PEERS LESS THEN DATABASE_PEERS_AMOUNT ALSO LOAD FROM SETTINGS
		if(knownPeers.size() < DATABASE_PEERS_AMOUNT)
		{
			List<Peer> settingsPeers = Settings.getInstance().getKnownPeers();
			settingsPeers.addAll(knownPeers);
			
			Logger.getGlobal().info("Peers retrieved after settings : " + settingsPeers.size());
			
			return settingsPeers;
		}		
		
		//RETURN
		return knownPeers;
	}
	
	public void addPeer(Peer peer)
	{
		//NO NEED TO INSERT PEER INTO DATABASE IF IT IS ALREADY IN SETTINGS
		for(Peer knownPeer: Settings.getInstance().getKnownPeers())
		{
			if(knownPeer.getAddress().equals(peer.getAddress()))
			{
				return;
			}
		}
		
		//ADD TO DATABASE
		DBSet.getInstance().getPeerMap().addPeer(peer);
	}
	
	public void blacklistPeer(Peer peer)
	{
		DBSet.getInstance().getPeerMap().blacklistPeer(peer);
	}
	
	public boolean isBlacklisted(InetAddress address)
	{
		return DBSet.getInstance().getPeerMap().isBlacklisted(address);
	}
	
	public boolean isBlacklisted(Peer peer)
	{
		return DBSet.getInstance().getPeerMap().isBlacklisted(peer.getAddress());
	}
}
