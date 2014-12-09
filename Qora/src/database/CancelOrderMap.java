package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import qora.assets.Order;
import qora.transaction.CancelOrderTransaction;

import com.google.common.primitives.UnsignedBytes;

import database.DBSet;
import database.serializer.OrderSerializer;

public class CancelOrderMap extends DBMap<byte[], Order> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public CancelOrderMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public CancelOrderMap(CancelOrderMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Order> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("cancelOrderOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.valueSerializer(new OrderSerializer())
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Order> getMemoryMap() 
	{
		return new TreeMap<byte[], Order>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Order getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public void delete(CancelOrderTransaction transaction) {
		this.delete(transaction.getSignature());
	}
	
	public Order get(CancelOrderTransaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(CancelOrderTransaction transaction, Order value)
	{
		this.set(transaction.getSignature(), value);
	}
}
