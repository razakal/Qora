package database.wallet;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import qora.assets.Order;
import utils.ObserverMessage;
import database.DBMap;
import database.IDB;
import database.serializer.OrderSerializer;

public class OrderMap extends DBMap<Tuple2<String, BigInteger>, Order> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public OrderMap(IDB databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
	}

	public OrderMap(OrderMap parent) 
	{
		super(parent);

	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<String, BigInteger>, Order> getMap(DB database) 
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<Tuple2<String, BigInteger>, Order> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.openMap(database);
	}
	
	private Map<Tuple2<String, BigInteger>, Order> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<Tuple2<String, BigInteger>, Order> map = database.createTreeMap("orders")
				//.keySerializer(BTreeKeySerializer.TUPLE2)
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

	public void add(Order order) {
		this.set(new Tuple2<String, BigInteger>(order.getCreator().getAddress(), order.getId()), order);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL ORDERS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, BigInteger>, Order> accountOrders = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, BigInteger> key: accountOrders.keySet())
		{
			this.delete(key);
		}
	}

	public void delete(Order order) 
	{
		this.delete(new Tuple2<String, BigInteger>(order.getCreator().getAddress(), order.getId()));
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public void addAll(Map<Account, List<Order>> orders)
	{
		//FOR EACH ACCOUNT
	    for(Account account: orders.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(Order order: orders.get(account))
	    	{
	    		this.add(order);
	    	}
	    }
	}
}
