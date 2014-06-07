package network.message;

import java.io.DataInputStream;
import java.util.Arrays;
import java.util.List;

import qora.block.Block;
import qora.crypto.Crypto;
import qora.transaction.Transaction;

import com.google.common.primitives.Ints;

import network.Peer;

public class MessageFactory {

	private static MessageFactory instance;
	
	public static MessageFactory getInstance()
	{
		if(instance == null)
		{
			instance = new MessageFactory();
		}
		
		return instance;
	}
	
	public Message createPingMessage()
	{
		//CREATE A MESSAGE WITH ping ACTION
		return new Message(Message.PING_TYPE);
	}
	
	public Message createGetPeersMessage()
	{	
		//CREATE A MESSAGE WITH getPeers ACTION
		return new Message(Message.GET_PEERS_TYPE);
	}
	
	public Message createPeersMessage(List<Peer> peers)
	{
		return new PeersMessage(peers);
	}
	
	public Message createVersionMessage(int height)
	{
		return new VersionMessage(height);
	}
	
	public Message createGetHeadersMessage(byte[] parent)
	{
		return new GetSignaturesMessage(parent);
	}
	
	public Message createHeadersMessage(List<byte[]> headers)
	{
		return new SignaturesMessage(headers);
	}
	
	public Message createGetBlockMessage(byte[] header)
	{
		return new GetBlockMessage(header);
	}
	
	public Message createBlockMessage(Block block)
	{
		return new BlockMessage(block);
	}
	
	public Message createTransactionMessage(Transaction transaction)
	{
		return new TransactionMessage(transaction);
	}
	
	public Message parse(Peer sender, DataInputStream inputStream) throws Exception
	{
		//READ MESSAGE TYPE
		byte[] typeBytes = new byte[Message.TYPE_LENGTH];
		inputStream.readFully(typeBytes);
		int type = Ints.fromByteArray(typeBytes);
		
		//READ HAS ID
		int hasId = inputStream.read();
		int id = -1;
		
		if(hasId == 1)
		{
			//READ ID
			byte[] idBytes = new byte[Message.ID_LENGTH];
			inputStream.readFully(idBytes);
			id = Ints.fromByteArray(idBytes);
		}
		
		//READ LENGTH
		//byte[] lengthBytes = new byte[Message.MESSAGE_LENGTH];
		//inputStream.readFully(lengthBytes);
		int length = inputStream.readInt();//Ints.fromByteArray(lengthBytes);		
		
		//IF MESSAGE CONTAINS DATA READ DATA AND VALIDATE CHECKSUM
		byte[] data = new byte[length];
		if(length > 0)
		{
			//READ CHECKSUM
			byte[] checksum = new byte[Message.CHECKSUM_LENGTH];
			inputStream.readFully(checksum);
			
			//READ DATA
			inputStream.readFully(data);
			
			/*int position = 0;
			while(position < length)
			{
				//READ MULTIPLE TIMES BECAUSE OF MAX READ OF 65536 BYTES
				
				//TODO CHECK THIS FIXES PROBLEM
				//position += inputStream.read(data, position, length - position);
				int temp = inputStream.read(data, position, length - position);
				position += temp;
				
				if(temp == -1)
				{
					boolean tempa = true;
					tempa = false;
					if(tempa);
				}
			}*/
			
			//VALIDATE CHECKSUM
			byte[] digest = Crypto.getInstance().digest(data);
			
			//TAKE FOR FIRST BYTES
			digest = Arrays.copyOfRange(digest, 0, Message.CHECKSUM_LENGTH);
			
			//CHECK IF CHECKSUM MATCHES
			if(!Arrays.equals(checksum, digest))
			{
				throw new Exception("Invalid data checksum length="+length);
			}
		}
		
		Message message = null;
			
		switch(type)
		{
		//GETPEERS
		case Message.GET_PEERS_TYPE:
						
			message = new Message(type);
			break;
			
		//PEERS
		case Message.PEERS_TYPE:
				
			//CREATE MESSAGE FROM DATA
			message = PeersMessage.parse(data);
			break;
				
		//VERSION
		case Message.VERSION_TYPE:
							
			//CREATE MESSAGE FROM DATA
			message = VersionMessage.parse(data);
			break;
			
		//GETSIGNATURES
		case Message.GET_SIGNATURES_TYPE:
				
			//CREATE MESSAGE FROM DATA
			message = GetSignaturesMessage.parse(data);
			break;
				
		//SIGNATURES
		case Message.SIGNATURES_TYPE:
						
			//CREATE MESSAGE FROM DATA
			message = SignaturesMessage.parse(data);
			break;
		
		//GETBLOCK
		case Message.GET_BLOCK_TYPE:
								
			//CREATE MESSAGE FROM DATA
			message = GetBlockMessage.parse(data);
			break;	
				
		//BLOCK
		case Message.BLOCK_TYPE:
												
			//CREATE MESSAGE FROM DATA
			message = BlockMessage.parse(data);
			break;		
			
		//TRANSACTION
		case Message.TRANSACTION_TYPE:
			
			//CREATE MESSAGE FRO MDATA
			message = TransactionMessage.parse(data);
			break;
		}
			
		//SET SENDER
		message.setSender(sender);	
			
		//SET ID
		if(hasId == 1)
		{
			message.setId(id);
		}
			
		//RETURN
		return message;
	}
}
