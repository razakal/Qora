package qora.web;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;

import database.DBMap;
import database.DBSet;

public class NameStorageMap extends DBMap<String, Map<String,String>> {

	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public NameStorageMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public NameStorageMap(DBMap<String, Map<String, String>> parent) {
		super(parent);
	}

	@Override
	protected Map<String, Map<String, String>> getMap(DB database) {
		//OPEN MAP
				BTreeMapMaker createTreeMap = database.createTreeMap("NameStorageMap");
				return createTreeMap
						.makeOrGet();
	}

	@Override
	protected Map<String, Map<String, String>> getMemoryMap() {
		return new HashMap<String, Map<String,String>>();
	}

	@Override
	protected Map<String, String> getDefaultValue() {
		return null;
	}

	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	protected void createIndexes(DB database){}
	
	public boolean contains(String name)
	{
		return this.contains(name);
	}
	
	public void add(String name, String key, String value)
	{
		Map<String, String> keyValueMap = this.get(name);
		if(keyValueMap == null)
		{
			keyValueMap = new HashMap<String, String>();
		}
		
		keyValueMap.put(key, value);
		
		this.set(name, keyValueMap);
	}
	
	public String getOpt(String name, String key)
	{
		Map<String, String> keyValueMap = this.get(name);
		if(keyValueMap == null)
		{
			return null;
		}
		
		return keyValueMap.get(key);
	}

}
