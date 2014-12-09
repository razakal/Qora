package database;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;

import qora.assets.Order;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.OrderSerializer;

public class CompletedOrderMap extends DBMap<BigInteger, Order> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public CompletedOrderMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
	}

	public CompletedOrderMap(CompletedOrderMap parent) 
	{
		super(parent);
	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<BigInteger, Order> getMap(DB database) 
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<BigInteger, Order> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.openMap(database);
	}
	
	private Map<BigInteger, Order> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<BigInteger, Order> map = database.createTreeMap("completedorders")
				.valueSerializer(new OrderSerializer())
				.makeOrGet();
		
		//RETURN
		return map;
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

	public void add(Order order)
	{
		this.set(order.getId(), order);
	}

	public void delete(Order order) 
	{
		this.delete(order.getId());
	}
}
