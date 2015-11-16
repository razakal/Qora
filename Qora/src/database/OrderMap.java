package database;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple4;

import qora.assets.Order;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.OrderSerializer;

public class OrderMap extends DBMap<BigInteger, Order> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap haveWantKeyMap;
	@SuppressWarnings("rawtypes")
	private BTreeMap wantHaveKeyMap;
	
	public OrderMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ORDER_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ORDER_TYPE);
		//this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ORDER_TYPE);
	}

	public OrderMap(OrderMap parent) 
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
	
	@SuppressWarnings("unchecked")
	private Map<BigInteger, Order> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<BigInteger, Order> map = database.createTreeMap("orders")
				.valueSerializer(new OrderSerializer())
				.makeOrGet();
		
		//HAVE/WANT KEY
		this.haveWantKeyMap = database.createTreeMap("orders_key_have_want")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.haveWantKeyMap, new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger, Order>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key, Order value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.getHave(), value.getWant(), value.getPrice(), key);
			}	
		});
		
		//HAVE/WANT KEY
		this.wantHaveKeyMap = database.createTreeMap("orders_key_want_have")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		//BIND HAVE/WANT KEY
		Bind.secondaryKey(map, this.wantHaveKeyMap, new Fun.Function2<Tuple4<Long, Long, BigDecimal, BigInteger>, BigInteger, Order>() {
			@Override
			public Tuple4<Long, Long, BigDecimal, BigInteger> run(BigInteger key, Order value) {
				return new Tuple4<Long, Long, BigDecimal, BigInteger>(value.getWant(), value.getHave(), value.getPrice(), key);
			}	
		});
		
				
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
		this.set(order.getId(), order);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<BigInteger> getKeys(long have, long want) {
		
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, want, null, null),
				Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
		
		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//GET ALL KEYS FOR FORK
			Collection<BigInteger> forkKeys = ((OrderMap) this.parent).getKeys(have, want);
			
			//COMBINE LISTS
			Set<BigInteger> combinedKeys = new TreeSet<BigInteger>(keys);
			combinedKeys.addAll(forkKeys);
			
			//DELETE DELETED
			for(BigInteger deleted: this.deleted)
			{
				combinedKeys.remove(deleted);
			}
			
			//CONVERT SET BACK TO COLLECTION
			keys = combinedKeys;
		}
		
		return keys;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<BigInteger> getKeysHave(long have) {
		
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, null, null, null),
				Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();

		return keys;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<BigInteger> getKeysWant(long want) {
		
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.wantHaveKeyMap).subMap(
				Fun.t4(want, null, null, null),
				Fun.t4(want, Fun.HI(), Fun.HI(), Fun.HI())).values();
		
		return keys;
	}
	
	public List<Order> getOrders(long haveWant) 
	{
		return getOrders(haveWant, false);
	}
	
	public List<Order> getOrders(long haveWant, boolean filter) 
	{
		Map<BigInteger, Boolean> orderKeys = new TreeMap<BigInteger, Boolean>();
		
		//FILTER ALL KEYS
		Collection<BigInteger> keys = this.getKeysHave(haveWant);
		
		for (BigInteger key : keys) {
			orderKeys.put(key, true);
		}
		
		keys = this.getKeysWant(haveWant);
		
		for (BigInteger key : keys) {
			orderKeys.put(key, true);
		}
		
		//GET ALL ORDERS FOR KEYS
		List<Order> orders = new ArrayList<Order>();

		for(Map.Entry<BigInteger, Boolean> orderKey : orderKeys.entrySet())
		{
			//Filters orders with unacceptably small amount. These orders have not worked
			if(filter){
				if(isExecutable(orderKey.getKey()))
					orders.add(this.get(orderKey.getKey()));
			}
			else
			{
				orders.add(this.get(orderKey.getKey()));
			}
		}

		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//RESORT ORDERS
			Collections.sort(orders);
		}

		//RETURN
		return orders;
	}

	public boolean isExecutable(BigInteger key) 
	{
		Order order = this.get(key);
		
		BigDecimal increment = order.calculateBuyIncrement(order, DBSet.getInstance());
		BigDecimal amount = order.getAmountLeft();
		amount = amount.subtract(amount.remainder(increment));
		return  (amount.compareTo(BigDecimal.ZERO) > 0);
	}
	
	public List<Order> getOrders(long have, long want) 
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = this.getKeys(have, want);
				
		//GET ALL ORDERS FOR KEYS
		List<Order> orders = new ArrayList<Order>();
		for(BigInteger key: keys)
		{
			orders.add(this.get(key));
		}
		
		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//RESORT ORDERS
			Collections.sort(orders);
		}
		
		//RETURN
		return orders;
	}
	
	public SortableList<BigInteger, Order> getOrdersSortableList(long have, long want)
	{
		//RETURN
		return getOrdersSortableList(have, want, false);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersSortableList(long have, long want, boolean filter)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, want, null, null),
				Fun.t4(have, want, Fun.HI(), Fun.HI())).values();
				
		//Filters orders with unacceptably small amount. These orders have not worked
		if(filter){
			List<BigInteger> keys2 = new ArrayList<BigInteger>();
			
			Iterator<BigInteger> iter = keys.iterator();
			while (iter.hasNext()) {
				BigInteger key = iter.next();
				if(isExecutable(key))
					keys2.add(key);
			}
			keys = keys2;
		}
		
		//RETURN
		return new SortableList<BigInteger, Order>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersHaveSortableList(long have)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(have, null, null, null),
				Fun.t4(have, Fun.HI(), Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<BigInteger, Order>(this, keys);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public SortableList<BigInteger, Order> getOrdersWantSortableList(long want)
	{
		//FILTER ALL KEYS
		Collection<BigInteger> keys = ((BTreeMap<Tuple4, BigInteger>) this.haveWantKeyMap).subMap(
				Fun.t4(null, want, null, null),
				Fun.t4(Fun.HI(), want, Fun.HI(), Fun.HI())).values();
		
		//RETURN
		return new SortableList<BigInteger, Order>(this, keys);
	}

	public void delete(Order order) 
	{
		this.delete(order.getId());
	}
}
