package qora.transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.account.PublicKeyAccount;
import qora.crypto.Base58;
import qora.crypto.Crypto;
import utils.Converter;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

import database.DBSet;



public class MessageTransactionV1 extends MessageTransaction {

	private static final int RECIPIENT_LENGTH = Account.ADDRESS_LENGTH;
	private static final int AMOUNT_LENGTH = 8;
	protected static final int CREATOR_LENGTH = 32;
	protected static final int DATA_SIZE_LENGTH = 4;
	protected static final int REFERENCE_LENGTH = 64;
	protected static final int FEE_LENGTH = 8;
	protected static final int SIGNATURE_LENGTH = 64;
	protected static final int ENCRYPTED_LENGTH = 1;
	protected static final int IS_TEXT_LENGTH = 1;
	protected static final int BASE_LENGTH = TIMESTAMP_LENGTH + REFERENCE_LENGTH + IS_TEXT_LENGTH + ENCRYPTED_LENGTH + CREATOR_LENGTH + DATA_SIZE_LENGTH + FEE_LENGTH + SIGNATURE_LENGTH + RECIPIENT_LENGTH + AMOUNT_LENGTH;

	private PublicKeyAccount creator;
	private byte[] data;

	private Account recipient;
	private BigDecimal amount;
	private byte[] encrypted;
	private byte[] isText;

	public MessageTransactionV1(PublicKeyAccount creator, Account recipient, BigDecimal amount, BigDecimal fee, byte[] data, byte[] isText, byte[] encrypted, long timestamp, byte[] reference, byte[] signature) {
		super(fee, timestamp, reference, signature);

		this.data = data;
		this.creator = creator;
		this.recipient = recipient;
		this.amount = amount;
		this.encrypted = encrypted;
		this.isText = isText;
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

	public static Transaction Parse(byte[] data) throws Exception
	{
		if (data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match block length");
		}

		int position = 0;

		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;

		//READ REFERENCE
		byte[] reference = Arrays.copyOfRange(data, position, position + REFERENCE_LENGTH);
		position += REFERENCE_LENGTH;

		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		PublicKeyAccount creator = new PublicKeyAccount(creatorBytes);
		position += CREATOR_LENGTH;

		//READ SENDER
		byte[] recipientBytes = Arrays.copyOfRange(data, position, position + RECIPIENT_LENGTH);
		Account recipient = new Account(Base58.encode(recipientBytes));
		position += RECIPIENT_LENGTH;

		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;

		//READ DATA SIZE
		byte[] dataSizeBytes = Arrays.copyOfRange(data, position, position + DATA_SIZE_LENGTH);
		int dataSize = Ints.fromByteArray(dataSizeBytes);	
		position += DATA_SIZE_LENGTH;

		//READ DATA
		byte[] arbitraryData = Arrays.copyOfRange(data, position, position + dataSize);
		position += dataSize;
		
		byte[] encryptedByte = Arrays.copyOfRange(data, position, position + ENCRYPTED_LENGTH);
		position += ENCRYPTED_LENGTH;
		
		byte[] isTextByte = Arrays.copyOfRange(data, position, position + IS_TEXT_LENGTH);
		position += IS_TEXT_LENGTH;
		
		
		//READ FEE
		byte[] feeBytes = Arrays.copyOfRange(data, position, position + FEE_LENGTH);
		BigDecimal fee = new BigDecimal(new BigInteger(feeBytes), 8);
		position += FEE_LENGTH;

		//READ SIGNATURE
		byte[] signatureBytes = Arrays.copyOfRange(data, position, position + SIGNATURE_LENGTH);

		return new MessageTransactionV1(creator, recipient, amount, fee, arbitraryData, isTextByte, encryptedByte, timestamp, reference, signatureBytes);

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
		transaction.put("amount", this.amount.toPlainString());
		if ( this.isText() && !this.isEncrypted() )
		{
			transaction.put("data", new String(this.data, Charset.forName("UTF-8")));
		}
		else
			transaction.put("data", Converter.toHex(this.data));
		transaction.put("encrypted", this.isEncrypted());
		transaction.put("isText", this.isText());
		
		return transaction;	
	}

	@Override
	public byte[] toBytes() {

		byte[] data = new byte[0];

		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MESSAGE_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);

		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);

		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);

		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());

		//WRITE RECIPIENT
		data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));

		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);

		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);

		//WRITE DATA
		data = Bytes.concat(data, this.data);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText);

		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);

		//SIGNATURE
		data = Bytes.concat(data, this.signature);

		return data;	
	}


	@Override
	public int getDataLength() {
		return TYPE_LENGTH + BASE_LENGTH + this.data.length;
	}


	@Override
	public boolean isSignatureValid() {
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MESSAGE_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, this.reference);
		
		//WRITE CREATOR
		data = Bytes.concat(data, this.creator.getPublicKey());
		
		try
		{
			//WRITE RECIPIENT
			data = Bytes.concat(data, Base58.decode(this.recipient.getAddress()));
		}
		catch(Exception e)
		{
			//ERROR DECODING ADDRESS
			System.out.println("Error decoding recipient " + this.recipient.getAddress());
		}
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(this.data.length);
		data = Bytes.concat(data, dataSizeBytes);
		
		//WRITE DATA
		data = Bytes.concat(data, this.data);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, this.encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, this.isText );
		
		//WRITE FEE
		byte[] feeBytes = this.fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().verify(this.creator.getPublicKey(), this.signature, data);
	
	}


	@Override
	public int isValid(DBSet db) {
		//CHECK IF RELEASED
		if( db.getBlockMap().getLastBlock().getHeight(db) < Transaction.MESSAGE_BLOCK_HEIGHT_RELEASE)
		{
			return NOT_YET_RELEASED;
		}
		
		//CHECK DATA SIZE
		if(data.length > 4000 || data.length < 1)
		{
			return INVALID_DATA_LENGTH;
		}
	
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(this.recipient.getAddress()))
		{
			return INVALID_ADDRESS;
		}
		
		//CHECK IF SENDER HAS ENOUGH MONEY
		if(this.creator.getBalance(1, db).compareTo(this.amount.add(this.fee)) == -1)
		{
			return NO_BALANCE;
		}
		
		//CHECK IF REFERENCE IS OKE
		if(!Arrays.equals(this.creator.getLastReference(db), this.reference))
		{
			return INVALID_REFERENCE;
		}
		
		//CHECK IF AMOUNT IS POSITIVE
		if(this.amount.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_AMOUNT;
		}
		
		//CHECK IF FEE IS POSITIVE
		if(this.fee.compareTo(BigDecimal.ZERO) <= 0)
		{
			return NEGATIVE_FEE;
		}

		return VALIDATE_OKE;
	}


	@Override
	public void process(DBSet db) {
		//UPDATE SENDER
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).subtract(this.amount).subtract(this.fee), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.recipient.getConfirmedBalance(db).add(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.signature, db);
		
		//UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(this.recipient.getLastReference(db), new byte[0]))
		{
			this.recipient.setLastReference(this.signature, db);
		}	
	}


	@Override
	public void orphan(DBSet db) {
		//UPDATE SENDER
		this.creator.setConfirmedBalance(this.creator.getConfirmedBalance(db).add(this.amount).add(this.fee), db);
						
		//UPDATE RECIPIENT
		this.recipient.setConfirmedBalance(this.recipient.getConfirmedBalance(db).subtract(this.amount), db);
		
		//UPDATE REFERENCE OF SENDER
		this.creator.setLastReference(this.reference, db);
		
		///UPDATE REFERENCE OF RECIPIENT
		if(Arrays.equals(this.recipient.getLastReference(db), this.signature))
		{
			this.recipient.removeReference(db);
		}	
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
	public BigDecimal getAmount(Account account) {
		String address = account.getAddress();
		
		//CHECK OF BOTH SENDER AND RECIPIENT
		if(address.equals(creator.getAddress()) && address.equals(recipient.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.fee);
		}
		
		//CHECK IF ONLY SENDER
		if(address.equals(creator.getAddress()))
		{
			return BigDecimal.ZERO.setScale(8).subtract(this.amount).subtract(this.fee);
		}
		
		//CHECK IF ONLY RECIPIENT
		if(address.equals(recipient.getAddress()))
		{
			return this.amount;
		}
		
		return BigDecimal.ZERO;
	}
	
	public static byte[] generateSignature(PrivateKeyAccount creator, Account recipient, BigDecimal amount, BigDecimal fee, byte[] arbitraryData, byte[] isText, byte[] encrypted, long timestamp) 
	{
		return generateSignature(DBSet.getInstance(), creator, recipient, amount, fee, arbitraryData, isText, encrypted, timestamp);
	}
	
	public static byte[] generateSignature(DBSet db, PrivateKeyAccount creator, Account recipient, BigDecimal amount, BigDecimal fee, byte[] arbitraryData,byte[] isText, byte[] encrypted, long timestamp) 
	{
		byte[] data = new byte[0];
		
		//WRITE TYPE
		byte[] typeBytes = Ints.toByteArray(MESSAGE_TRANSACTION);
		typeBytes = Bytes.ensureCapacity(typeBytes, TYPE_LENGTH, 0);
		data = Bytes.concat(data, typeBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		//WRITE REFERENCE
		data = Bytes.concat(data, creator.getLastReference(db));
		
		//WRITE CREATOR
		data = Bytes.concat(data, creator.getPublicKey());
		
		try
		{
			//WRITE RECIPIENT
			data = Bytes.concat(data, Base58.decode(recipient.getAddress()));
		}
		catch(Exception e)
		{
			//ERROR DECODING ADDRESS
			System.out.println("Error decoding address");
		}
		
		//WRITE AMOUNT
		byte[] amountBytes = amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE DATA SIZE
		byte[] dataSizeBytes = Ints.toByteArray(arbitraryData.length);
		data = Bytes.concat(data, dataSizeBytes);
		
		//WRITE DATA
		data = Bytes.concat(data, arbitraryData);
		
		//WRITE ENCRYPTED
		data = Bytes.concat(data, encrypted);
		
		//WRITE ISTEXT
		data = Bytes.concat(data, isText);
		
		//WRITE FEE
		byte[] feeBytes = fee.unscaledValue().toByteArray();
		fill = new byte[FEE_LENGTH - feeBytes.length];
		feeBytes = Bytes.concat(fill, feeBytes);
		data = Bytes.concat(data, feeBytes);
		
		return Crypto.getInstance().sign(creator, data);
	}
	
	@Override
	public long getKey() {
		return 0L;
	}
}

