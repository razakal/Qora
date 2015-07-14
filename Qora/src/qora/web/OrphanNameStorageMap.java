package qora.web;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

import com.google.common.primitives.SignedBytes;

import database.DBMap;
import database.DBSet;

public class OrphanNameStorageMap extends DBMap<byte[], Map<String, String>> {

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public OrphanNameStorageMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public OrphanNameStorageMap(DBMap<byte[], Map<String, String>> parent) {
		super(parent);
	}

	@Override
	protected Map<byte[], Map<String, String>> getMap(DB database) {
		
		
		return   database.createTreeMap("OrphanNameStorageMap")
		            .comparator(SignedBytes.lexicographicalComparator())
		            .makeOrGet();
		
	}

	@Override
	protected Map<byte[], Map<String, String>> getMemoryMap() {
		return new HashMap<byte[], Map<String, String>>();
	}

	@Override
	protected Map<String, String> getDefaultValue() {
		return null;
	}


	@Override
	protected Map<Integer, Integer> getObservableData() {
		return this.observableData;
	}

	@Override
	protected void createIndexes(DB database) {}
	
	
	public void add(byte[] txAndName, String key, String value)
	{
		Map<String, String> keyValueMap = this.get(txAndName);
		if (keyValueMap == null) {
			keyValueMap = new HashMap<String, String>();
		}
		
		keyValueMap.put(key, value);
		
		this.set(txAndName, keyValueMap);
		
	}
	
	public void remove(byte[] txAndName)
	{
		this.remove(txAndName);
	}
	

}
