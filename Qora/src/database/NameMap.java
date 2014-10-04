package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import qora.naming.Name;

import database.DBSet;
import database.serializer.NameSerializer;

public class NameMap extends DBMap<String, Name> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public NameMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public NameMap(NameMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<String, Name> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("names")
				.valueSerializer(new NameSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<String, Name> getMemoryMap() 
	{
		return new HashMap<String, Name>();
	}

	@Override
	protected Name getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public boolean contains(Name name)
	{
		return this.contains(name.getName());
	}
	
	public void add(Name name)
	{
		this.set(name.getName(), name);
	}
	
	public void delete(Name name)
	{
		this.delete(name.getName());
	}
}
