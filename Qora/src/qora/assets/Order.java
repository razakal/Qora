package qora.assets;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.List;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import database.DBSet;
import qora.account.Account;
import qora.crypto.Base58;
import qora.transaction.Transaction;

public class Order implements Comparable<Order> {
	
	private static final int ID_LENGTH = 64;
	private static final int CREATOR_LENGTH = 25;
	private static final int HAVE_LENGTH = 8;
	private static final int WANT_LENGTH = 8;
	private static final int AMOUNT_LENGTH = 12;
	private static final int FULFILLED_LENGTH = 12;
	private static final int PRICE_LENGTH = 12;
	private static final int TIMESTAMP_LENGTH = 8;
	private static final int BASE_LENGTH = ID_LENGTH + CREATOR_LENGTH + HAVE_LENGTH + WANT_LENGTH + AMOUNT_LENGTH + FULFILLED_LENGTH + PRICE_LENGTH + TIMESTAMP_LENGTH;
	
	private BigInteger id;
	private Account creator;
	private long have;
	private long want;
	private BigDecimal amount;
	private BigDecimal fulfilled;
	private BigDecimal price;
	private long timestamp;
	
	public Order(BigInteger id, Account creator, long have, long want, BigDecimal amount, BigDecimal price, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;
		this.amount = amount;
		this.fulfilled = BigDecimal.ZERO.setScale(8);
		this.price = price;
		this.timestamp = timestamp;
	}
	
	public Order(BigInteger id, Account creator, long have, long want, BigDecimal amount, BigDecimal fulfilled, BigDecimal price, long timestamp)
	{
		this.id = id;
		this.creator = creator;
		this.have = have;
		this.want = want;
		this.amount = amount;
		this.fulfilled = fulfilled;
		this.price = price;
		this.timestamp = timestamp;
	}
	
	//GETTERS/SETTERS
	
	public BigInteger getId()
	{
		return this.id;
	}

	public Account getCreator() 
	{
		return this.creator;
	}

	public long getHave() 
	{
		return this.have;
	}
	
	public Asset getHaveAsset() 
	{
		return this.getHaveAsset(DBSet.getInstance());
	}
	
	public Asset getHaveAsset(DBSet db)
	{
		return db.getAssetMap().get(this.have);
	}

	public long getWant() 
	{
		return this.want;
	}
	
	public Asset getWantAsset() 
	{
		return this.getWantAsset(DBSet.getInstance());
	}
	
	public Asset getWantAsset(DBSet db)
	{
		return db.getAssetMap().get(this.want);
	}

	public BigDecimal getAmount() 
	{
		return this.amount;
	}
	
	public BigDecimal getAmountLeft()
	{
		return this.amount.subtract(this.fulfilled);
	}

	public BigDecimal getPrice() 
	{
		return this.price;
	}
	
	public BigDecimal getFulfilled()
	{
		return this.fulfilled;
	}
	
	public long getTimestamp() 
	{
		return this.timestamp;
	}
	
	public void setFulfilled(BigDecimal fulfilled)
	{
		this.fulfilled = fulfilled;
	}
	
	public boolean isFulfilled()
	{
		return this.fulfilled.compareTo(this.amount) == 0;
	}
	
	public List<Trade> getInitiatedTrades()
	{
		return this.getInitiatedTrades(DBSet.getInstance());
	}
	
	public List<Trade> getInitiatedTrades(DBSet db)
	{
		return db.getTradeMap().getInitiatedTrades(this);
	}
	
	public boolean isConfirmed() 
	{
		return DBSet.getInstance().getOrderMap().contains(this.id) || DBSet.getInstance().getCompletedOrderMap().contains(this.id);
	}
	
	//PARSE/CONVERT
	
	public static Order parse(byte[] data) throws Exception
	{
		//CHECK IF CORRECT LENGTH
		if(data.length < BASE_LENGTH)
		{
			throw new Exception("Data does not match order length");
		}
		
		int position = 0;
		
		//READ ID
		byte[] idBytes = Arrays.copyOfRange(data, position, position + ID_LENGTH);
		BigInteger id = new BigInteger(idBytes);
		position += ID_LENGTH;
		
		//READ CREATOR
		byte[] creatorBytes = Arrays.copyOfRange(data, position, position + CREATOR_LENGTH);
		Account creator = new Account(Base58.encode(creatorBytes));
		position += CREATOR_LENGTH;
		
		//READ HAVE
		byte[] haveBytes = Arrays.copyOfRange(data, position, position + HAVE_LENGTH);
		long have = Longs.fromByteArray(haveBytes);	
		position += HAVE_LENGTH;
		
		//READ HAVE
		byte[] wantBytes = Arrays.copyOfRange(data, position, position + WANT_LENGTH);
		long want = Longs.fromByteArray(wantBytes);	
		position += WANT_LENGTH;
		
		//READ AMOUNT
		byte[] amountBytes = Arrays.copyOfRange(data, position, position + AMOUNT_LENGTH);
		BigDecimal amount = new BigDecimal(new BigInteger(amountBytes), 8);
		position += AMOUNT_LENGTH;		
		
		//READ FULFILLED
		byte[] fulfilledBytes = Arrays.copyOfRange(data, position, position + FULFILLED_LENGTH);
		BigDecimal fulfilled = new BigDecimal(new BigInteger(fulfilledBytes), 8);
		position += FULFILLED_LENGTH;		
		
		//READ PRICE
		byte[] priceBytes = Arrays.copyOfRange(data, position, position + PRICE_LENGTH);
		BigDecimal price = new BigDecimal(new BigInteger(priceBytes), 8);
		position += PRICE_LENGTH;	
		
		//READ TIMESTAMP
		byte[] timestampBytes = Arrays.copyOfRange(data, position, position + TIMESTAMP_LENGTH);
		long timestamp = Longs.fromByteArray(timestampBytes);	
		position += TIMESTAMP_LENGTH;
		
		return new Order(id, creator, have, want, amount, fulfilled, price, timestamp);
	}
	
	public byte[] toBytes()
	{
		byte[] data = new byte[0];
		
		//WRITE ID
		byte[] idBytes = this.id.toByteArray();
		byte[] fill = new byte[ID_LENGTH - idBytes.length];
		idBytes = Bytes.concat(fill, idBytes);
		data = Bytes.concat(data, idBytes);
		
		//WRITE CREATOR
		try
		{
			data = Bytes.concat(data , Base58.decode(this.creator.getAddress()));
		}
		catch(Exception e)
		{
			//DECODE EXCEPTION
		}
		
		//WRITE HAVE
		byte[] haveBytes = Longs.toByteArray(this.have);
		haveBytes = Bytes.ensureCapacity(haveBytes, HAVE_LENGTH, 0);
		data = Bytes.concat(data, haveBytes);
		
		//WRITE WANT
		byte[] wantBytes = Longs.toByteArray(this.want);
		wantBytes = Bytes.ensureCapacity(wantBytes, WANT_LENGTH, 0);
		data = Bytes.concat(data, wantBytes);
		
		//WRITE AMOUNT
		byte[] amountBytes = this.amount.unscaledValue().toByteArray();
		fill = new byte[AMOUNT_LENGTH - amountBytes.length];
		amountBytes = Bytes.concat(fill, amountBytes);
		data = Bytes.concat(data, amountBytes);
		
		//WRITE FULFILLED
		byte[] fulfilledBytes = this.fulfilled.unscaledValue().toByteArray();
		fill = new byte[FULFILLED_LENGTH - fulfilledBytes.length];
		fulfilledBytes = Bytes.concat(fill, fulfilledBytes);
		data = Bytes.concat(data, fulfilledBytes);
		
		//WRITE PRICE
		byte[] priceBytes = this.price.unscaledValue().toByteArray();
		fill = new byte[PRICE_LENGTH - priceBytes.length];
		priceBytes = Bytes.concat(fill, priceBytes);
		data = Bytes.concat(data, priceBytes);
		
		//WRITE TIMESTAMP
		byte[] timestampBytes = Longs.toByteArray(this.timestamp);
		timestampBytes = Bytes.ensureCapacity(timestampBytes, TIMESTAMP_LENGTH, 0);
		data = Bytes.concat(data, timestampBytes);
		
		return data;
	}
	
	public int getDataLength() 
	{
		return BASE_LENGTH;
	}
	
	//PROCESS/ORPHAN

	public void process(DBSet db, Transaction transaction) 
	{
		//REMOVE HAVE
		this.creator.setConfirmedBalance(this.have, this.creator.getConfirmedBalance(this.have, db).subtract(this.amount), db);
		
		//ADD ORDER TO DATABASE
		db.getOrderMap().add(this.copy());
		
		//GET ALL ORDERS(WANT, HAVE) LOWEST PRICE FIRST
		List<Order> orders = db.getOrderMap().getOrders(this.want, this.have);
		
		//TRY AND COMPLETE ORDERS
		boolean completedOrder = true;
		int i = 0;
		while(completedOrder && i < orders.size())
		{
			//RESET COMPLETED
			completedOrder = false;
			
			//GET ORDER
			Order order = orders.get(i);
			
			//CALCULATE BUYING PRICE
			BigDecimal buyingPrice = BigDecimal.ONE.setScale(8).divide(order.getPrice(), RoundingMode.DOWN);
			
			//CHECK IF OWNERS OF BOTH ORDER ARE NOT THE SAME
	
				//CHECK IF BUYING PRICE IS HIGHER OR EQUAL THEN OUR SELLING PRICE
				if(buyingPrice.compareTo(this.price) >= 0)
				{
					//CALCULATE THE MAXIMUM AMOUNT WE COULD BUY
					BigDecimal amount = order.getAmountLeft();
					amount = amount.min(this.getAmountLeft().multiply(BigDecimal.ONE.setScale(8).divide(order.getPrice(), RoundingMode.DOWN)).setScale(8, RoundingMode.DOWN));
									
					//CHECK IF WE CAN BUY ANYTHING
					if(amount.compareTo(BigDecimal.ZERO) > 0)
					{
						//CALCULATE THE INCREMENTS AT WHICH WE HAVE TO BUY
						BigDecimal increment = this.calculateBuyIncrement(order, db);
						
						//CALCULATE THE AMOUNT WE CAN BUY
						amount = amount.subtract(amount.remainder(increment));
						
						//CALCULATE THE PRICE WE HAVE TO PAY
						BigDecimal price = amount.multiply(order.getPrice()).setScale(8);
						
						//CHECK IF AMOUNT AFTER ROUNDING IS NOT ZERO
						if(amount.compareTo(BigDecimal.ZERO) > 0)
						{
							//CREATE TRADE
							Trade trade = new Trade(this.getId(), order.getId(), amount, price, transaction.getTimestamp());
							trade.process(db);
							this.fulfilled = this.fulfilled.add(price);
						}
						
						//COMPLETED ORDER
						completedOrder = true;
					}
				}
			
			//INCREMENT I
			i++;
		}	
	}
	
	public void orphan(DBSet db) {
		
		//ORPHAN TRADES
		for(Trade trade: this.getInitiatedTrades(db))
		{
			trade.orphan(db);
		}
		
		//REMOVE ORDER FROM DATABASE
		db.getOrderMap().delete(this);	
		
		//REMOVE HAVE
		this.creator.setConfirmedBalance(this.have, this.creator.getConfirmedBalance(this.have, db).add(this.amount), db);
	}
	
	public BigDecimal calculateBuyIncrement(Order order, DBSet db)
	{
		BigInteger multiplier = BigInteger.valueOf(100000000l);
		
		//CALCULATE THE MINIMUM INCREMENT AT WHICH I CAN BUY USING GCD
		BigInteger haveAmount = BigInteger.ONE.multiply(multiplier);
		BigInteger priceAmount = order.getPrice().multiply(new BigDecimal(multiplier)).toBigInteger();
		BigInteger gcd = haveAmount.gcd(priceAmount);
		haveAmount = haveAmount.divide(gcd);
		priceAmount = priceAmount.divide(gcd);
		
		//CALCULATE GCD IN COMBINATION WITH DIVISIBILITY
		if(this.getWantAsset(db).isDivisible())
		{
			haveAmount = haveAmount.multiply(multiplier);
		}
		if(this.getHaveAsset(db).isDivisible())
		{
			priceAmount = priceAmount.multiply(multiplier);
		}
		gcd = haveAmount.gcd(priceAmount);
		
		//CALCULATE THE INCREMENT AT WHICH WE HAVE TO BUY
		BigDecimal increment = new BigDecimal(haveAmount.divide(gcd));
		if(this.getWantAsset(db).isDivisible())
		{
			increment = increment.divide(new BigDecimal(multiplier));
		}
		
		//RETURN
		return increment;
	}

	//COMPARE
	
	@Override
	public int compareTo(Order order) 
	{	
		//COMPARE ONLY BY PRICE
		return this.price.compareTo(order.getPrice());	
	}
	
	//COPY
	
	public Order copy() 
	{	
		try 
		{
			return parse(this.toBytes());
		} 
		catch (Exception e) 
		{
			return null;
		}	
	}
}
