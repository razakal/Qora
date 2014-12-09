package network.message;

import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;

import com.google.common.primitives.Bytes;

public class TransactionMessage extends Message{

	private Transaction transaction;
	
	public TransactionMessage(Transaction transaction)
	{
		super(TRANSACTION_TYPE);	
		
		this.transaction = transaction;
	}
	
	public Transaction getTransaction()
	{
		return this.transaction;
	}
	
	public static TransactionMessage parse(byte[] data) throws Exception
	{
		//PARSE TRANSACTION
		Transaction transaction = TransactionFactory.getInstance().parse(data);
		
		return new TransactionMessage(transaction);
	}
	
	public byte[] toBytes() 
	{
		byte[] data = new byte[0];
		
		//WRITE BLOCK
		byte[] blockBytes = this.transaction.toBytes();
		data = Bytes.concat(data, blockBytes);
		
		//ADD CHECKSUM
		data = Bytes.concat(super.toBytes(), this.generateChecksum(data), data);
		
		return data;
	}	
	
	protected int getDataLength()
	{
		return this.transaction.getDataLength();
	}
	
}
