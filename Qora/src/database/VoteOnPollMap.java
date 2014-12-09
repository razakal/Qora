package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import qora.transaction.Transaction;

import com.google.common.primitives.UnsignedBytes;

import database.DBSet;

public class VoteOnPollMap extends DBMap<byte[], Integer> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public VoteOnPollMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public VoteOnPollMap(VoteOnPollMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<byte[], Integer> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("voteOnPollOrphanData")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Integer> getMemoryMap() 
	{
		return new TreeMap<byte[], Integer>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Integer getDefaultValue() 
	{
		return -1;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public Integer get(Transaction transaction)
	{
		return this.get(transaction.getSignature());
	}
	
	public void set(Transaction transaction, Integer value)
	{
		this.set(transaction.getSignature(), value);
	}
	
	public void delete(Transaction transaction)
	{
		this.delete(transaction.getSignature());
	}
}
