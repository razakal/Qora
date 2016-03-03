package qora.transaction;

import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.google.common.primitives.Longs;

import database.BalanceMap;
import database.DBSet;
import qora.account.Account;
import qora.account.PublicKeyAccount;
import utils.Converter;



public abstract class MessageTransaction extends Transaction {

	private int version; 
	
	protected PublicKeyAccount creator;
	protected byte[] data;

	protected Account recipient;
	protected BigDecimal amount;
	protected long key;
	protected byte[] encrypted;
	protected byte[] isText;
	
	public MessageTransaction(BigDecimal fee, long timestamp, byte[] reference, byte[] signature) {
		super(MESSAGE_TRANSACTION, fee, timestamp, reference, signature);
		
		if(timestamp < Transaction.getPOWFIX_RELEASE()) {
			version = 1;
		} else {
			version = 3;
		}
	}

	public int getVersion()
	{
		return this.version;
	}
	
	public Account getSender()
	{
		return this.creator;
	}

	public byte[] getData() 
	{
		return this.data;
	}

	public Account getRecipient()
	{
		return this.recipient;
	}

	public long getKey()
	{
		return this.key;
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	public byte[] getEncrypted()
	{
		byte[] enc = new byte[1];
		enc[0] = (isEncrypted())?(byte)1:(byte)0;
		return enc;
	}
	
	public boolean isText()
	{
		return (Arrays.equals(this.isText,new byte[1]))?false:true;
	}
	
	public boolean isEncrypted()
	{
		return (Arrays.equals(this.encrypted,new byte[1]))?false:true;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject transaction = this.getJsonBase();

		//ADD CREATOR/SERVICE/DATA
		transaction.put("creator", this.creator.getAddress());
		transaction.put("recipient", this.recipient.getAddress());
		transaction.put("asset", this.key);
		transaction.put("amount", this.amount.toPlainString());
		if ( this.isText() && !this.isEncrypted() )
		{
			transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
		}
		else
		{
			transaction.put("data", Converter.toHex(this.data));
		}
		transaction.put("encrypted", this.isEncrypted());
		transaction.put("isText", this.isText());
		
		return transaction;	
	}
	
	@Override
	public PublicKeyAccount getCreator() {
		return this.creator;
	}

	@Override
	public List<Account> getInvolvedAccounts() {
		return Arrays.asList(this.creator, this.recipient);
	}

	@Override
	public boolean isInvolved(Account account) {
		String address = account.getAddress();
		
		if(address.equals(creator.getAddress()) || address.equals(recipient.getAddress()))
		{
			return true;
		}
		
		return false;
	}
	
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
	public BigDecimal getAmount(Account account) {
		BigDecimal amount = BigDecimal.ZERO.setScale(8);
		String address = account.getAddress();
		
		//IF SENDER
		if(address.equals(this.creator.getAddress()))
		{
			amount = amount.subtract(this.fee);
		}

		//IF QORA ASSET
		if(this.key == BalanceMap.QORA_KEY)
		{
			//IF SENDER
			if(address.equals(this.creator.getAddress()))
			{
				amount = amount.subtract(this.amount);
			}
			
			//IF RECIPIENT
			if(address.equals(this.recipient.getAddress()))
			{
				amount = amount.add(this.amount);
			}
		}
		
		return amount;
	}
	
	@Override
	public Map<String, Map<Long, BigDecimal>> getAssetAmount() 
	{
		Map<String, Map<Long, BigDecimal>> assetAmount = new LinkedHashMap<>();
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), BalanceMap.QORA_KEY, this.fee);
		
		assetAmount = subAssetAmount(assetAmount, this.creator.getAddress(), this.key, this.amount);
		assetAmount = addAssetAmount(assetAmount, this.recipient.getAddress(), this.key, this.amount);
		
		return assetAmount;
	}
	
	public static Transaction Parse(byte[] data) throws Exception
	{
		// READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, 0, TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		
		if(timestamp < Transaction.getPOWFIX_RELEASE())
		{
			return MessageTransactionV1.Parse(data);			
		} else {
			return MessageTransactionV3.Parse(data);
		}
	}
	
}

