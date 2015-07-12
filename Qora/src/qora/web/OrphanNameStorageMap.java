package qora.web;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;
import org.mapdb.DB.BTreeMapMaker;
import org.mapdb.Fun.Tuple2;

import database.DBMap;
import database.DBSet;

public class OrphanNameStorageMap extends DBMap<Tuple2<byte[], String>, Map<String, String>> {

	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();

	public OrphanNameStorageMap(DBSet databaseSet, DB database) {
		super(databaseSet, database);
	}

	public OrphanNameStorageMap(DBMap<Tuple2<byte[], String>, Map<String, String>> parent) {
		super(parent);
	}

	@Override
	protected Map<Tuple2<byte[], String>, Map<String, String>> getMap(DB database) {
		// OPEN MAP
		BTreeMapMaker createTreeMap = database.createTreeMap("OrphanNameStorageMap");
		return createTreeMap.makeOrGet();
	}

	@Override
	protected Map<Tuple2<byte[], String>, Map<String, String>> getMemoryMap() {
		return new HashMap<Tuple2<byte[], String>, Map<String, String>>();
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
	
	
	public void add(Tuple2<byte[], String> txAndName, String key, String value)
	{
		Map<String, String> keyValueMap = this.get(txAndName);
		if (keyValueMap == null) {
			keyValueMap = new HashMap<String, String>();
		}
		
		keyValueMap.put(key, value);
		
		this.set(txAndName, keyValueMap);
		
	}
	
	public void remove(Tuple2<byte[], String> txAndName)
	{
		this.remove(txAndName);
	}
	

}
