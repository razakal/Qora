package database;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;

import network.Peer;
import ntp.NTP;
import qora.transaction.Transaction;
import settings.Settings;
import utils.PeersForSortComparator;
import utils.ReverseComparator;

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
				if(Arrays.equals(Arrays.copyOfRange(this.get(addressBI), 0, 2), BYTE_WHITELISTED))
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
	
	
	public class PeersForSort {

		private byte[] address;
		private long whiteConnectTime;
		private long grayConnectTime;
		private long pingCouner;
		
		public PeersForSort(byte[] address, long whiteConnectTime, long grayConnectTime, long pingCouner) {
			this.address = address;
			this.whiteConnectTime = whiteConnectTime;
			this.grayConnectTime = grayConnectTime;
			this.pingCouner = pingCouner;
		} 
		public PeersForSort(byte[] address) {
			this.address = address;
			this.whiteConnectTime = 0;
			this.grayConnectTime = 0;
			this.pingCouner = 0;
		} 
		
		public byte[] getAddress(){
			return address;
		}

		public long getWhiteConnectTime(){
			return whiteConnectTime;
		}
		
		public long getGrayConnectTime(){
			return grayConnectTime;
		}
		
		public long getPingCouner(){
			return pingCouner;
		}
				
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Peer> getBestPeers(int amount, boolean allFromSettings)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
			//PEERS
			List<Peer> peers = new ArrayList<Peer>();
			List<PeersForSort> peersForSort = new ArrayList<PeersForSort>();
			//ITERATE AS LONG AS:
			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				
				//CHECK IF ADDRESS IS WHITELISTED
				
				byte[] data = this.get(addressBI);
				
				if(Arrays.equals(Arrays.copyOfRange(data, 0, 2), BYTE_WHITELISTED))
				{
					//InetAddress address = InetAddress.getByAddress(addressBI);
					
					if(data.length==26)
					{
						int position = 2;
						byte[] timestampBytesBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
						long whiteConnectTime = Longs.fromByteArray(timestampBytesBytes);
						position += Transaction.TIMESTAMP_LENGTH;
						byte[] grayConnectTimeBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
						long grayConnectTime = Longs.fromByteArray(grayConnectTimeBytes);
						position += Transaction.TIMESTAMP_LENGTH;
						byte[] pingCounerBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
						long pingCouner = Longs.fromByteArray(pingCounerBytes);
						
						peersForSort.add(new PeersForSort(addressBI, whiteConnectTime, grayConnectTime, pingCouner));
					}
					else
					{
						peersForSort.add(new PeersForSort(addressBI));
					}
						
										
				}			
			}
			
			Collections.sort(peersForSort, new ReverseComparator(new PeersForSortComparator())); 
			
			for (PeersForSort peer : peersForSort) {
				InetAddress address = InetAddress.getByAddress(peer.getAddress());

				//CHECK IF SOCKET IS NOT LOCALHOST
				if(!address.equals(InetAddress.getLocalHost()))
				{
					if(peers.size() >= amount)
					{
						if(allFromSettings)
							break;
						else
							return peers;
					}

					//CREATE PEER
					Peer peer1 = new Peer(address);	
					
					//ADD TO LIST
					peers.add(peer1);
				}	
			}
			
			List<Peer> knownPeers = Settings.getInstance().getKnownPeers();
			
			for (Peer knownPeer : knownPeers) {
				if(!allFromSettings && peers.size() >= amount)
					break;
				
				boolean found = false;
				for (Peer peer : peers) {
					if(peer.getAddress().equals(knownPeer.getAddress()))
					{
						found = true;
						break;
					}
				}
				
				if (!found){
					//ADD TO LIST
					peers.add(knownPeer);
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
	
	public List<PeersForSort> getAllPeers(int amount)
	{
		try
		{
			//GET ITERATOR
			Iterator<byte[]> iterator = this.getKeys().iterator();
			
			//PEERS
			List<PeersForSort> peers = new ArrayList<PeersForSort>();
			
			//ITERATE AS LONG AS:
			// 1. we have not reached the amount of peers
			// 2. we have read all records
			while(iterator.hasNext() && peers.size() < amount)
			{
				//GET ADDRESS
				byte[] addressBI = iterator.next();
				
				int position = 2;
				byte [] data = this.get(addressBI);
				long whiteConnectTime = 0;
				long grayConnectTime = 0;
				long pingCouner = 0;
				if(data.length==26)
				{
					byte[] timestampBytesBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
					whiteConnectTime = Longs.fromByteArray(timestampBytesBytes);
					position += Transaction.TIMESTAMP_LENGTH;
					byte[] grayConnectTimeBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
					grayConnectTime = Longs.fromByteArray(grayConnectTimeBytes);
					position += Transaction.TIMESTAMP_LENGTH;
					byte[] pingCounerBytes = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
					pingCouner = Longs.fromByteArray(pingCounerBytes);
				}		
				peers.add(new PeersForSort(addressBI, whiteConnectTime, grayConnectTime, pingCouner));
			}
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			return new ArrayList<PeersForSort>();
		}
	}
	
	
	public void addPeer(Peer peer)
	{
		// BYTE_WHITELISTED 2 whiteConnectTime 8 grayConnectTime 8  
		byte[] whiteConnectTime = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
		byte[] grayConnectTime = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
		byte[] pingCounter = new byte[]{0, 0, 0, 0, 0, 0, 0, 0};
		
		if(this.map.containsKey(peer.getAddress().getAddress()))
		{
			byte[] data = this.map.get(peer.getAddress().getAddress());
			
			try {
				if(data.length == 26)
				{
					int position = 2;
					whiteConnectTime = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
					position += Transaction.TIMESTAMP_LENGTH;
					grayConnectTime = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
					position += Transaction.TIMESTAMP_LENGTH;
					pingCounter = Arrays.copyOfRange(data, position, position + Transaction.TIMESTAMP_LENGTH);
				}
			
			} catch (Exception e) {
				
			}
			
		}
		
		long timestamp = NTP.getTime();
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, Transaction.TIMESTAMP_LENGTH, 0);

		if(peer.getPingCounter()>1)
		{
			if(peer.isWhite())
			{
				long longPingCounter = Longs.fromByteArray(pingCounter);
				longPingCounter ++;
				pingCounter = Longs.toByteArray(longPingCounter);
				pingCounter = Bytes.ensureCapacity(pingCounter, Transaction.TIMESTAMP_LENGTH, 0);
				
				whiteConnectTime = Bytes.ensureCapacity(timestampBytes, Transaction.TIMESTAMP_LENGTH, 0);
			}
			else
			{
				grayConnectTime = Bytes.ensureCapacity(timestampBytes, Transaction.TIMESTAMP_LENGTH, 0);
			}
		}
		
		//ADD PEER INTO DB
		this.map.put(peer.getAddress().getAddress(), Bytes.concat(BYTE_WHITELISTED, whiteConnectTime, grayConnectTime, pingCounter));
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
