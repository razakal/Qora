package network.message;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import network.Peer;

public class PeersMessage extends Message {

	private static final int ADDRESS_LENGTH = 4;
	private static final int DATA_LENGTH = 4;
	
	private List<Peer> peers;
	
	public PeersMessage(List<Peer> peers) 
	{
		super(Message.PEERS_TYPE);
		
		this.peers = peers;
	}
	public List<Peer> getPeers()
	{
		return this.peers;
	}

	public static PeersMessage parse(byte[] data) throws Exception
	{
		//READ LENGTH
		byte[] lengthBytes =  Arrays.copyOfRange(data, 0, DATA_LENGTH);
		int length = Ints.fromByteArray(lengthBytes);
		
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != DATA_LENGTH + (length * ADDRESS_LENGTH))
		{
			throw new Exception("Data does not match length");
		}
		
		//CREATE PEER LIST
		List<Peer> peers = new ArrayList<Peer>();
		
		for(int i=0; i<length; i++)
		{
			//CALCULATE POSITION
			int position = lengthBytes.length + (i * ADDRESS_LENGTH);
			
			//READ ADDRESS
			byte[] addressBytes = Arrays.copyOfRange(data, position, position + ADDRESS_LENGTH);
			InetAddress address = InetAddress.getByAddress(addressBytes);
			
			//CREATE PEER
			Peer peer = new Peer(address);
			
			//ADD TO LIST
			peers.add(peer);
		}
		
		return new PeersMessage(peers);
	}	
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE LENGTH
		int length = this.peers.size();
		byte[] lengthBytes = Ints.toByteArray(length);
		lengthBytes = Bytes.ensureCapacity(lengthBytes, DATA_LENGTH, 0);
		data = Bytes.concat(data, lengthBytes);
		
		//WRITE PEERS
		for(Peer peer: this.peers)
		{
			//WRITE ADDRESS
			byte[] addressBytes = peer.getAddress().getAddress();
			data = Bytes.concat(data, addressBytes);
		}
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return DATA_LENGTH + (this.peers.size() * ADDRESS_LENGTH);
	}
}
