package database;

import java.util.HashMap;
import java.util.Map;

import org.mapdb.Atomic;
import org.mapdb.DB;

import qora.assets.Asset;
import utils.ObserverMessage;
import database.DBSet;
import database.serializer.AssetSerializer;

public class AssetMap extends DBMap<Long, Asset> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Atomic.Long atomicKey;
	private long key;
	
	public AssetMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.atomicKey = database.getAtomicLong("assets_key");
		this.key = this.atomicKey.get();
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ASSET_TYPE);
	}

	public AssetMap(AssetMap parent) 
	{
		super(parent);
		
		this.key = this.getKey();
	}
	
	protected long getKey()
	{
		return this.key;
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Long, Asset> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("assets")
				.valueSerializer(new AssetSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<Long, Asset> getMemoryMap() 
	{
		return new HashMap<Long, Asset>();
	}

	@Override
	protected Asset getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public long add(Asset asset)
	{
		//INCREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.incrementAndGet();
		}
		
		//INCREMENT KEY
		this.key++;
		
		//INSERT WITH NEW KEY
		this.set(this.key, asset);
		
		//RETURN KEY
		return this.key;
	}
	
	
	public void delete(long key)
	{
		super.delete(key);
		
		//DECREMENT ATOMIC KEY IF EXISTS
		if(this.atomicKey != null)
		{
			this.atomicKey.decrementAndGet();
		}
		
		//DECREMENT KEY
		 this.key = key - 1;
	}
}
