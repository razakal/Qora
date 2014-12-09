package qora.naming;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.simple.JSONObject;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import database.DBSet;

public class NameSale 
{
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int AMOUNT_LENGTH = 8;
	
	private String key;
	private BigDecimal amount;
	
	public NameSale(String key, BigDecimal amount)
	{
		this.key = key;
		this.amount = amount;
	}
	
	//GETTERS/SETTERS
	
	public String getKey()
	{
		return key;
	}
	
	public Name getName(DBSet db)
	{
		return db.getNameMap().get(this.key);
	}
	
	public Name getName()
	{
		return this.getName(DBSet.getInstance());
	}
	
	public BigDecimal getAmount()
	{
		return this.amount;
	}
	
	//PARSE
	
	public static NameSale Parse(byte[] data) throws Exception
	{	
		int position = 0;
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
				
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
				
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String nameName = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;
		
		return new NameSale(nameName, amount);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject nameSale = new JSONObject();
								
		//ADD NAME/AMOUNT/OWNER
		nameSale.put("name", this.getKey());
		nameSale.put("amount", this.getAmount().toPlainString());
		nameSale.put("seller", this.getName().getOwner().getAddress());
								
		return nameSale;	
	}
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.key.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		byte[] fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		return data;
	}
	
	public int getDataLength()
	{
		byte[] nameBytes = this.key.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		
		return NAME_SIZE_LENGTH + nameLength + AMOUNT_LENGTH;
	}
	
	//REST
	
	@Override
	public String toString()
	{
		return this.key;
	}
}
