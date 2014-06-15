package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;
import qora.transaction.Transaction;

public class VoteOnPollDatabase {
	
	private VoteOnPollDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<byte[], Integer> orphanDataMap;	
	
	public VoteOnPollDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.orphanDataMap = database.createTreeMap("voteOnPollOrphanData")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}
	
	public VoteOnPollDatabase(VoteOnPollDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.orphanDataMap = new TreeMap<byte[], Integer>(UnsignedBytes.lexicographicalComparator());
	}
	
	public void setOrphanData(Transaction transaction, int previousOption)
	{
		try
		{
			//ADD NAME INTO DB
			this.orphanDataMap.put(transaction.getSignature(), previousOption);
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}	
	}
	
	public int getOrphanData(Transaction transaction)
	{
		try
		{
			if(this.orphanDataMap.containsKey(transaction.getSignature()))
			{
				return this.orphanDataMap.get(transaction.getSignature());
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getOrphanData(transaction);
				}
			}
			
			return -1;
		}
		catch(Exception e)
		{
			//NO CHILD FOUND
			return -1;
		}			
	}
	
	public void remove(Transaction transaction)
	{
		//REMOVE TRANSACTION FROM 0 CONFIRMS
		this.orphanDataMap.remove(transaction.getSignature());
		
		//COMMIT
		if(this.databaseSet != null)
		{
			this.databaseSet.commit();
		}		
	}
}
