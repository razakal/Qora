package database;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import network.Peer;

public class PeerDatabase {
	
	private static final byte[] BYTE_WHITELISTED = new byte[]{0, 0};
	private static final byte[] BYTE_BLACKLISTED = new byte[]{1, 1};
	
	private PeerDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<byte[], byte[]> peersMap;
	
	public PeerDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.peersMap = database.createTreeMap("peers")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}
	
	public PeerDatabase(PeerDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.peersMap = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}
	
	public List<Peer> getKnownPeers(int amount)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.peersMap.keySet().iterator();
			
			//PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			//ITERATE AS LONG AS:
			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				
				//CHECK IF ADDRESS IS WHITELISTED
				if(Arrays.equals(this.peersMap.get(addressBI), BYTE_WHITELISTED))
				{
					InetAddress address = InetAddress.getByAddress(addressBI);
					
					//CHECK IF SOCKET IS NOT LOCALHOST
					if(!address.equals(InetAddress.getLocalHost()))
					{
						//CREATE PEER
						Peer peer = new Peer(address);	
						
						//ADD TO LIST
						peers.add(peer);
					}
				}			
			}
			
			//ADD PEERS FROM PARENT
			if(parent != null)
			{
				peers.addAll(parent.getKnownPeers(amount - peers.size()));
			}
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			return new ArrayList<Peer>();
		}
	}
	
	public void addPeer(Peer peer)
	{
		try
		{
			//ADD PEER INTO DB
			this.peersMap.put(peer.getAddress().getAddress(), BYTE_WHITELISTED);
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}			
	}
	
	public void blacklistPeer(Peer peer)
	{
		//TODO DISABLED WHILE UNSTABLE
		return;
		
		/*try
		{
			//ADD PEER INTO DB
			this.peersMap.put(peer.getAddress().getAddress(), BYTE_BLACKLISTED);
				
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}	*/		
	}
	
	public boolean isBlacklisted(InetAddress address)
	{
		try
		{
			//CHECK IF PEER IS BLACKLISTED
			if(this.peersMap.containsKey(address.getAddress()))
			{
				return Arrays.equals(this.peersMap.get(address.getAddress()), BYTE_BLACKLISTED);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.isBlacklisted(address);
				}
			}
			
			return false;
		}
		catch(Exception e)
		{
			//e.printStackTrace();
						
			return false;
		}		
	}
}
