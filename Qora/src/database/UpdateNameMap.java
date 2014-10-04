package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import qora.naming.Name;
import qora.transaction.Transaction;

import com.google.common.primitives.UnsignedBytes;

import database.DBSet;
import database.serializer.NameSerializer;

public class UpdateNameMap extends DBMap<byte[], Name> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public UpdateNameMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public UpdateNameMap(UpdateNameMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Name> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("updateNameOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.valueSerializer(new NameSerializer())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Name> getMemoryMap() 
	{
		return new TreeMap<byte[], Name>(UnsignedBytes.lexicographicalComparator());
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
	
	public Name get(Transaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(Transaction transaction, Name name)
	{
		this.set(transaction.getSignature(), name);
	}
	
	public void delete(Transaction transaction)
	{
		this.delete(transaction.getSignature());
	}
}
