package database;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import database.DBSet;

public class BalanceMap extends DBMap<Tuple2<String, Long>, BigDecimal> 
{
	public static final long QORA_KEY = 0l;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public BalanceMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public BalanceMap(BalanceMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<String, Long>, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("balances")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, Long>, BigDecimal> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, Long>, BigDecimal>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected BigDecimal getDefaultValue() 
	{
		return BigDecimal.ZERO.setScale(8);
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public void set(String address, BigDecimal value)
	{
		this.set(address, QORA_KEY, value);
	}
	
	public void set(String address, long key, BigDecimal value)
	{
		this.set(new Tuple2<String, Long>(address, key), value);
	}
	
	public BigDecimal get(String address)
	{
		return this.get(address, QORA_KEY);
	}
	
	public BigDecimal get(String address, long key)
	{
		return this.get(new Tuple2<String, Long>(address, key));
	}
}
