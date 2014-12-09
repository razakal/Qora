package qora.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import database.DBSet;

public class Trade {
	
	private static final int ORDER_LENGTH = 64;
	private static final int AMOUNT_LENGTH = 12;
	private static final int PRICE_LENGTH = 12;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int BASE_LENGTH = ORDER_LENGTH + ORDER_LENGTH + AMOUNT_LENGTH + PRICE_LENGTH + TIMESTAMP_LENGTH;
	
	private BigInteger initiator;
	private BigInteger target;
	private BigDecimal amount;
	private BigDecimal price;
	private long timestamp;
	
	public Trade(BigInteger initiator, BigInteger target, BigDecimal amount, BigDecimal price, long timestamp)
	{
		this.initiator = initiator;
		this.target = target;
		this.amount = amount;
		this.price = price;
		this.timestamp = timestamp;
	}

	public BigInteger getInitiator() 
	{
		return this.initiator;
	}
	
	public Order getInitiatorOrder(DBSet db)
	{
		return this.getOrder(this.initiator, db);
	}

	public BigInteger getTarget() 
	{
		return this.target;
	}
	
	public Order getTargetOrder(DBSet db)
	{
		return this.getOrder(this.target, db);
	}
	
	private Order getOrder(BigInteger key, DBSet db)
	{
		if(db.getOrderMap().contains(key))
		{
			return db.getOrderMap().get(key);
		}
		
		return db.getCompletedOrderMap().get(key);
	}

	public BigDecimal getAmount() 
	{
		return this.amount;
	}

	public BigDecimal getPrice() 
	{
		return this.price;
	}
	
	public long getTimestamp()
	{
		return this.timestamp;
	}
	
	//PARSE/CONVERT
	
	public static Trade parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match trade length");
		}
		
		int position = 0;
		
		//READ INITIATOR
		byte[] initiatorBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger initiator = new BigInteger(initiatorBytes);
		position += ORDER_LENGTH;
		
		//READ TARGET
		byte[] targetBytes = Arrays.copyOfRange(data, position, position + ORDER_LENGTH);
		BigInteger target = new BigInteger(targetBytes);
		position += ORDER_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;		
		
		//READ PRICE
		byte[] priceBytes = Arrays.copyOfRange(data, position, position + PRICE_LENGTH);
		BigDecimal price = new BigDecimal(new BigInteger(priceBytes), 8);
		position += PRICE_LENGTH;		
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);
		position += TIMESTAMP_LENGTH;	
		
		return new Trade(initiator, target, amount, price, timestamp);
	}	
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE INITIATOR
		byte[] initiatorBytes = this.initiator.toByteArray();
		byte[] fill = new byte[ORDER_LENGTH - initiatorBytes.length];
		initiatorBytes = Bytes.concat(fill, initiatorBytes);
		data = Bytes.concat(data, initiatorBytes);
		
		//WRITE TARGET
		byte[] targetBytes = this.target.toByteArray();
		fill = new byte[ORDER_LENGTH - targetBytes.length];
		targetBytes = Bytes.concat(fill, targetBytes);
		data = Bytes.concat(data, targetBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE PRICE
		byte[] priceBytes = this.price.unscaledValue().toByteArray();
		fill = new byte[PRICE_LENGTH - priceBytes.length];
		priceBytes = Bytes.concat(fill, priceBytes);
		data = Bytes.concat(data, priceBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		data = Bytes.concat(data, timestampBytes);
		
		return data;
	}
	
	public int getDataLength() 
	{
		return BASE_LENGTH;
	}	
	
	//PROCESS/ORPHAN
	
	public void process(DBSet db)
	{
		Order initiator = this.getInitiatorOrder(db).copy();
		Order target = this.getTargetOrder(db).copy();
			
		//ADD TRADE TO DATABASE
		db.getTradeMap().add(this);
		
		//UPDATE FULFILLED
		initiator.setFulfilled(initiator.getFulfilled().add(this.price));
		target.setFulfilled(target.getFulfilled().add(this.amount));
		
		//CHECK IF FULFILLED
		if(initiator.isFulfilled())
		{
			//REMOVE FROM ORDERS
			db.getOrderMap().delete(initiator);
			
			//ADD TO COMPLETED ORDERS
			db.getCompletedOrderMap().add(initiator);
		}
		else
		{
			//UPDATE ORDER
			db.getOrderMap().add(initiator);
		}
		
		if(target.isFulfilled())
		{
			//REMOVE FROM ORDERS
			db.getOrderMap().delete(target);
			
			//ADD TO COMPLETED ORDERS
			db.getCompletedOrderMap().add(target);
		}
		else
		{
			//UPDATE ORDER
			db.getOrderMap().add(target);
		}
		
		//TRANSFER FUNDS
		initiator.getCreator().setConfirmedBalance(initiator.getWant(), initiator.getCreator().getConfirmedBalance(initiator.getWant(), db).add(this.amount), db);
		target.getCreator().setConfirmedBalance(target.getWant(), target.getCreator().getConfirmedBalance(target.getWant(), db).add(this.price), db);	
	}

	public void orphan(DBSet db) 
	{
		Order initiator = this.getInitiatorOrder(db).copy();
		Order target = this.getTargetOrder(db).copy();
		
		//REVERSE FUNDS
		initiator.getCreator().setConfirmedBalance(initiator.getWant(), initiator.getCreator().getConfirmedBalance(initiator.getWant(), db).subtract(this.amount), db);
		target.getCreator().setConfirmedBalance(target.getWant(), target.getCreator().getConfirmedBalance(target.getWant(), db).subtract(this.price), db);	
		
		//CHECK IF ORDER IS FULFILLED
		if(initiator.isFulfilled())
		{
			//REMOVE FROM COMPLETED ORDERS
			db.getCompletedOrderMap().delete(initiator);
		}
		if(target.isFulfilled())
		{
			//DELETE TO COMPLETED ORDERS
			db.getCompletedOrderMap().delete(target);
		}
		
		//REVERSE FULFILLED
		initiator.setFulfilled(initiator.getFulfilled().subtract(this.price));
		target.setFulfilled(target.getFulfilled().subtract(this.amount));
		
		//UPDATE ORDERS
		db.getOrderMap().add(initiator);
		db.getOrderMap().add(target);
		
		//REMOVE FROM DATABASE
		db.getTradeMap().delete(this);
	}
	
}
