package network.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

public class SignaturesMessage extends Message{

	private static final int SIGNATURE_LENGTH = 128;
	private static final int DATA_LENGTH = 4;
	
	private List<byte[]> signatures;
	
	public SignaturesMessage(List<byte[]> signatures)
	{
		super(SIGNATURES_TYPE);	
		
		this.signatures = signatures;
	}
	
	public List<byte[]> getSignatures()
	{
		return this.signatures;
	}
	
	public static SignaturesMessage parse(byte[] data) throws Exception
	{
		//READ LENGTH
		byte[] lengthBytes =  Arrays.copyOfRange(data, 0, DATA_LENGTH);
		int length = Ints.fromByteArray(lengthBytes);
		
		//CHECK IF DATA MATCHES LENGTH
		if(data.length != DATA_LENGTH + (length * SIGNATURE_LENGTH))
		{
			throw new Exception("Data does not match length");
		}
		
		//CREATE HEADERS LIST
		List<byte[]> headers = new ArrayList<byte[]>();
		
		for(int i=0; i<length; i++)
		{
			//CALCULATE POSITION
			int position = DATA_LENGTH + (i * SIGNATURE_LENGTH);
			
			//READ HEADER
			byte[] header = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);
			
			//ADD TO LIST
			headers.add(header);
		}
		
		return new SignaturesMessage(headers);
	}
	
	@Override
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE LENGTH
		int length = this.signatures.size();
		byte[] lengthBytes = Ints.toByteArray(length);
		lengthBytes = Bytes.ensureCapacity(lengthBytes, DATA_LENGTH, 0);
		data = Bytes.concat(data, lengthBytes);
		
		//WRITE SIGNATURES
		for(byte[] header: this.signatures)
		{
			//WRITE SIGNATURE
			data = Bytes.concat(data, header);
		}
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}
	
	@Override
	public int getDataLength()
	{
		return DATA_LENGTH + (this.signatures.size() * SIGNATURE_LENGTH);
	}

}
