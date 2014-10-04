package database;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import network.Peer;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import database.DBSet;

public class PeerMap extends DBMap<byte[], byte[]> 
{
	private static final byte[] BYTE_WHITELISTED = new byte[]{0, 0};
	private static final byte[] BYTE_BLACKLISTED = new byte[]{1, 1};
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public PeerMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public PeerMap(PeerMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], byte[]> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("peers")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], byte[]> getMemoryMap() 
	{
		return new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public List<Peer> getKnownPeers(int amount)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
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
				if(Arrays.equals(this.get(addressBI), BYTE_WHITELISTED))
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
		//ADD PEER INTO DB
		this.map.put(peer.getAddress().getAddress(), BYTE_WHITELISTED);
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
		//CHECK IF PEER IS BLACKLISTED
		if(this.contains(address.getAddress()))
		{
			return Arrays.equals(this.get(address.getAddress()), BYTE_BLACKLISTED);
		}
			
		return false;
	}
}
