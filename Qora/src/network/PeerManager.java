package network;

import java.net.InetAddress;
import java.util.List;

import database.DBSet;
import settings.Settings;

public class PeerManager {

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
	
	public List<Peer> getBestPeers()
	{
		return DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxSentPeers(), false);
	}
	
	
	public List<Peer> getKnownPeers()
	{
		//ASK DATABASE FOR A LIST OF PEERS
		List<Peer> knownPeers = DBSet.getInstance().getPeerMap().getBestPeers(Settings.getInstance().getMaxReceivePeers(), true);
		
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
