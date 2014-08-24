package database;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple4;

import qora.assets.Order;
import database.DBSet;
import database.serializer.OrderSerializer;

public class OrderMap extends DBMap<BigInteger, Order> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private BTreeMap haveWantKeyMap;
	
	public OrderMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
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

	public void delete(Order order) 
	{
		this.delete(order.getId());
	}
}
