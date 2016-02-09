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
import qora.payment.Payment;
import qora.web.blog.BlogEntry;

public abstract class ArbitraryTransaction extends Transaction {

	int version; 
	
	public ArbitraryTransaction(BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(ARBITRARY_TRANSACTION, fee, timestamp, reference, signature);
		
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
	
	// GETTERS/SETTERS

	public abstract int getService();
	
	public abstract byte[] getData();

	// PARSE CONVERT

	@Override
	public abstract JSONObject toJson();

	@Override
	public abstract byte[] toBytes();

	@Override
	public abstract int getDataLength();

	// VALIDATE

	@Override
	public abstract boolean isSignatureValid();

	@Override
	public abstract int isValid(DBSet db);
	
	// PROCESS/ORPHAN
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
	
	public abstract void addToCommentMapOnDemand(DBSet db);

	public abstract void deleteInternal(DBSet db, boolean isShare, BlogEntry blogEntryOpt);
	
	public abstract void deleteCommentInternal(DBSet db, BlogEntry commentEntry);

	public abstract List<Payment> getPayments();
	
	public static Transaction Parse(byte[] data) throws Exception
	{
		// READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, 0, TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
	
		if(timestamp < Transaction.POWFIX_RELEASE) {
			return ArbitraryTransactionV1.Parse(data);			
		} else {
			return ArbitraryTransactionV3.Parse(data);
		}
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator,
			int service, byte[] arbitraryData, BigDecimal fee, long timestamp) {

		if(timestamp < Transaction.POWFIX_RELEASE) {
			return ArbitraryTransactionV1.generateSignature(db, creator, service, 
					arbitraryData, fee, timestamp);	
		} else {
			return ArbitraryTransactionV3.generateSignature(db, creator, null, service, 
					arbitraryData, fee, timestamp);	
		}
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, List<Payment> payments,
			int service, byte[] arbitraryData, BigDecimal fee, long timestamp) {

		if(timestamp < Transaction.POWFIX_RELEASE) {
			return ArbitraryTransactionV1.generateSignature(db, creator, service, 
					arbitraryData, fee, timestamp);	
		} else {
			return ArbitraryTransactionV3.generateSignature(db, creator, payments, service, 
					arbitraryData, fee, timestamp);	
		}
	}
}
