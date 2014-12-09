package network.message;

import com.google.common.primitives.Bytes;

public class GetSignaturesMessage extends Message{

	private byte[] parent;
	
	private static final int GET_HEADERS_LENGTH = 128;
	
	public GetSignaturesMessage(byte[] parent)
	{
		super(GET_SIGNATURES_TYPE);	
		
		this.parent = parent;
	}
	
	public byte[] getParent()
	{
		return this.parent;
	}

	public static Message parse(byte[] data) throws Exception 
	{
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != GET_HEADERS_LENGTH)
		{
			throw new Exception("Data does not match length");
		}
				
		return new GetSignaturesMessage(data);
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE PARENT
		data = Bytes.concat(data, this.parent);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return GET_HEADERS_LENGTH;
	}

}
