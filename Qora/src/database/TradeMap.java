package database;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import qora.assets.Order;
import qora.assets.Trade;
import database.DBSet;
import database.serializer.TradeSerializer;

public class TradeMap extends DBMap<Tuple2<BigInteger, BigInteger>, Trade>
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public TradeMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public TradeMap(TradeMap parent) 
	{
		super(parent);

	}

	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<BigInteger, BigInteger>, Trade> getMap(DB database) 
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<Tuple2<BigInteger, BigInteger>, Trade> getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.openMap(database);
	}
	
	private Map<Tuple2<BigInteger, BigInteger>, Trade> openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<Tuple2<BigInteger, BigInteger>, Trade> map = database.createTreeMap("trades")
				.valueSerializer(new TradeSerializer())
				.makeOrGet();
		
		//RETURN
		return map;
	}

	@Override
	protected Trade getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public void add(Trade trade) 
	{
		this.set(new Tuple2<BigInteger, BigInteger>(trade.getInitiator(), trade.getTarget()), trade);	
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Collection<Tuple2> getKeys(Order order) {
		
		Map uncastedMap = this.map;
		
		//FILTER ALL KEYS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, Order>) uncastedMap).subMap(
				Fun.t2(order.getId(), null),
				Fun.t2(order.getId(), Fun.HI)).keySet();
		
		//IF THIS IS A FORK
		if(this.parent != null)
		{
			//GET ALL KEYS FOR FORK
			Collection<Tuple2> forkKeys = ((TradeMap) this.parent).getKeys(order);
			
			//COMBINE LISTS
			Set<Tuple2> combinedKeys = new TreeSet<Tuple2>(keys);
			combinedKeys.addAll(forkKeys);
			
			//DELETE DELETED
			for(Tuple2 deleted: this.deleted)
			{
				combinedKeys.remove(deleted);
			}
			
			//CONVERT SET BACK TO COLLECTION
			keys = combinedKeys;
		}
		
		return keys;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Trade> getTrades(Order order) 
	{
		//FILTER ALL TRADES
		Collection<Tuple2> keys = this.getKeys(order);
		
		//GET ALL TRADES FOR KEYS
		List<Trade> trades = new ArrayList<Trade>();
		for(Tuple2 key: keys)
		{
			trades.add(this.get(key));
		}
		
		//RETURN
		return trades;
	}

	public void delete(Trade trade) 
	{
		this.delete(new Tuple2<BigInteger, BigInteger>(trade.getInitiator(), trade.getTarget()));
	}
}
