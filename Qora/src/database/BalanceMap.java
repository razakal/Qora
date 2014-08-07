package database;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import database.DBSet;

public class BalanceMap extends DBMap<String, BigDecimal> 
{
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
	protected Map<String, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.getTreeMap("balances");
	}

	@Override
	protected Map<String, BigDecimal> getMemoryMap() 
	{
		return new HashMap<String, BigDecimal>();
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
}
