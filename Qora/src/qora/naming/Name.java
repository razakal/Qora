package qora.naming;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.json.simple.JSONObject;

import qora.account.Account;
import qora.crypto.Base58;
import utils.NumberAsString;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Ints;

import database.DBSet;

public class Name {

	private static final int OWNER_LENGTH = 25;
	private static final int NAME_SIZE_LENGTH = 4;
	private static final int VALUE_SIZE_LENGTH = 4;
	
	private Account owner;
	private String name;
	private String value;
	
	public Name(Account owner, String name, String value)
	{
		this.owner = owner;
		this.name = name;
		this.value = value;
	}
	
	//GETTERS/SETTERS
	
	public Account getOwner()
	{
		return this.owner;
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public String getValue()
	{
		return this.value;
	}
	
	public void setOwner(Account owner)
	{
		this.owner = owner;
	}
	
	public void setValue(String value)
	{
		this.value = value;
	}
	
	public boolean isConfirmed()
	{
		return DBSet.getInstance().getNameMap().contains(this);
	}
	
	//PARSE
	
	public static Name Parse(byte[] data) throws Exception
	{	
		int position = 0;
		
		//READ OWNER
		byte[] ownerBytes = Arrays.copyOfRange(data, position, position + OWNER_LENGTH);
		Account owner = new Account(Base58.encode(ownerBytes));
		position += OWNER_LENGTH;
		
		//READ NAME
		byte[] nameLengthBytes = Arrays.copyOfRange(data, position, position + NAME_SIZE_LENGTH);
		int nameLength = Ints.fromByteArray(nameLengthBytes);
		position += NAME_SIZE_LENGTH;
		
		if(nameLength < 1 || nameLength > 400)
		{
			throw new Exception("Invalid name length");
		}
		
		byte[] nameBytes = Arrays.copyOfRange(data, position, position + nameLength);
		String name = new String(nameBytes, StandardCharsets.UTF_8);
		position += nameLength;
		
		//READ VALUE
		byte[] valueLengthBytes = Arrays.copyOfRange(data, position, position + VALUE_SIZE_LENGTH);
		int valueLength = Ints.fromByteArray(valueLengthBytes);
		position += VALUE_SIZE_LENGTH;
		
		if(valueLength < 1 || valueLength > 4000)
		{
			throw new Exception("Invalid value length");
		}
		
		byte[] valueBytes = Arrays.copyOfRange(data, position, position + valueLength);
		String value = new String(valueBytes, StandardCharsets.UTF_8);
		position += valueLength;
		
		return new Name(owner, name, value);
	}
	
	@SuppressWarnings("unchecked")
	public JSONObject toJson() 
	{
		//GET BASE
		JSONObject name = new JSONObject();
								
		//ADD NAME/VALUE/OWNER
		name.put("name", this.getName());
		name.put("value", this.getValue());
		name.put("owner", this.getOwner().getAddress());
								
		return name;	
	}
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE OWNER
		try
		{
			data = Bytes.concat(data , Base58.decode(this.owner.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE NAME SIZE
		byte[] nameBytes = this.name.getBytes(StandardCharsets.UTF_8);
		int nameLength = nameBytes.length;
		byte[] nameLengthBytes = Ints.toByteArray(nameLength);
		data = Bytes.concat(data, nameLengthBytes);
		
		//WRITE NAME
		data = Bytes.concat(data, nameBytes);
		
		//WRITE VALUE SIZE
		byte[] valueBytes = this.value.getBytes(StandardCharsets.UTF_8);
		int valueLength = valueBytes.length;
		byte[] valueLengthBytes = Ints.toByteArray(valueLength);
		data = Bytes.concat(data, valueLengthBytes);
				
		//WRITE VALUE
		data = Bytes.concat(data, valueBytes);
		
		return data;
	}

	public int getDataLength() 
	{
		return OWNER_LENGTH + NAME_SIZE_LENGTH + this.name.getBytes(StandardCharsets.UTF_8).length + VALUE_SIZE_LENGTH + this.value.getBytes(StandardCharsets.UTF_8).length;
	}	
	
	//REST
	
	@Override
	public String toString()
	{
		return this.name;
	}
	
	@Override
	public boolean equals(Object b)
	{
		if(b != null && b instanceof Name)
		{
			Name two = (Name) b;
			
			return this.getName().endsWith(two.getName());
		}
		
		return false;
	}
	
	public String getNameBalanceString()
	{
		return NumberAsString.getInstance().numberAsString(this.getOwner().getBalance(0)) + " - " + this.getName();
	}
}
