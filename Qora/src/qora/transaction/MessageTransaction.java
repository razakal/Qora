package qora.transaction;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;



public abstract class MessageTransaction extends Transaction {

	int version; 

	public MessageTransaction(BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(MESSAGE_TRANSACTION, fee, timestamp, reference, signature);
		
		if(timestamp < Transaction.POWFIX_RELEASE) {
			version = 1;
		} else {
			version = 3;
		}
	}

	public int getVersion()
	{
		return this.version;
	}
	
	public abstract Account getSender();

	public abstract byte[] getData();

	public abstract Account getRecipient();

	public abstract BigDecimal getAmount();
	
	public abstract byte[] getEncrypted();
	
	public abstract boolean isText();
	
	public abstract boolean isEncrypted();

	public abstract long getKey();

	@Override
	public abstract JSONObject toJson();

	@Override
	public abstract byte[] toBytes();

	@Override
	public abstract int getDataLength();

	@Override
	public abstract boolean isSignatureValid();
	@Override
	public abstract int isValid(DBSet db);

	@Override
	public abstract void process(DBSet db);
	
	@Override
	public abstract void orphan(DBSet db);

	@Override
	public abstract PublicKeyAccount getCreator();

	@Override
	public abstract List<Account> getInvolvedAccounts();

	@Override
	public abstract boolean isInvolved(Account account);

	@Override
	public abstract BigDecimal getAmount(Account account);
	
	public static Transaction Parse(byte[] data) throws Exception
	{
		// READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, 0, TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		
		if(timestamp < Transaction.POWFIX_RELEASE)
		{
			return MessageTransactionV1.Parse(data);			
		} else {
			return MessageTransactionV3.Parse(data);
		}
	}
	
	public static byte[] generateSignature(PrivateKeyAccount creator, Account recipient, BigDecimal amount, BigDecimal fee, byte[] arbitraryData, byte[] isText, byte[] encrypted, long timestamp) 
	{
		if(timestamp < Transaction.POWFIX_RELEASE) {
			return MessageTransactionV1.generateSignature(creator, recipient, amount, fee, arbitraryData, isText, encrypted, timestamp);
		} else {
			return MessageTransactionV3.generateSignature(creator, recipient, 0L, amount, fee, arbitraryData, isText, encrypted, timestamp);	
		}	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, Account recipient, BigDecimal amount, BigDecimal fee, byte[] arbitraryData,byte[] isText, byte[] encrypted, long timestamp) 
	{
		if(timestamp < Transaction.POWFIX_RELEASE) {
			return MessageTransactionV1.generateSignature(db, creator, recipient, amount, fee, arbitraryData, isText, encrypted, timestamp);
		} else {
			return MessageTransactionV3.generateSignature(db, creator, recipient, 0L, amount, fee, arbitraryData, isText, encrypted, timestamp);	
		}
	}

	public static byte[] generateSignature(PrivateKeyAccount creator, Account recipient, long key, BigDecimal amount, BigDecimal fee, byte[] arbitraryData, byte[] isText, byte[] encrypted, long timestamp) 
	{
		if(timestamp < Transaction.POWFIX_RELEASE) {
			return MessageTransactionV1.generateSignature(creator, recipient, amount, fee, arbitraryData, isText, encrypted, timestamp);
		} else {
			return MessageTransactionV3.generateSignature(creator, recipient, key, amount, fee, arbitraryData, isText, encrypted, timestamp);	
		}	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, Account recipient, long key, BigDecimal amount, BigDecimal fee, byte[] arbitraryData,byte[] isText, byte[] encrypted, long timestamp) 
	{
		if(timestamp < Transaction.POWFIX_RELEASE) {
			return MessageTransactionV1.generateSignature(db, creator, recipient, amount, fee, arbitraryData, isText, encrypted, timestamp);
		} else {
			return MessageTransactionV3.generateSignature(db, creator, recipient, key, amount, fee, arbitraryData, isText, encrypted, timestamp);	
		}
	}
}

