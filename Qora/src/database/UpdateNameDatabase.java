package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.naming.Name;
import qora.transaction.Transaction;

public class UpdateNameDatabase {
	
	private UpdateNameDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<byte[], byte[]> orphanDataMap;	
	
	public UpdateNameDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.orphanDataMap = database.createTreeMap("updateNameOrphanData")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}
	
	public UpdateNameDatabase(UpdateNameDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.orphanDataMap = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}
	
	public void setOrphanData(Transaction transaction, Name name)
	{
		try
		{
			//ADD NAME INTO DB
			this.orphanDataMap.put(transaction.getSignature(), name.toBytes());
			
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
	
	public Name getOrphanData(Transaction transaction)
	{
		try
		{
			if(this.orphanDataMap.containsKey(transaction.getSignature()))
			{
				byte[] rawName = this.orphanDataMap.get(transaction.getSignature());
				return Name.Parse(rawName);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getOrphanData(transaction);
				}
			}
			
			return null;
		}
		catch(Exception e)
		{
			//NO CHILD FOUND
			return null;
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
