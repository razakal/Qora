package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.transaction.Transaction;

public class BuyNameDatabase {
	
	private BuyNameDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<byte[], String> orphanDataMap;	
	
	public BuyNameDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.orphanDataMap = database.createTreeMap("buyNameOrphanData")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}
	
	public BuyNameDatabase(BuyNameDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.orphanDataMap = new TreeMap<byte[], String>(UnsignedBytes.lexicographicalComparator());
	}
	
	public void setOrphanData(Transaction transaction, Account account)
	{
		try
		{
			//ADD NAME INTO DB
			this.orphanDataMap.put(transaction.getSignature(), account.getAddress());
			
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
	
	public Account getOrphanData(Transaction transaction)
	{
		try
		{
			if(this.orphanDataMap.containsKey(transaction.getSignature()))
			{
				String address = this.orphanDataMap.get(transaction.getSignature());
				return new Account(address);
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
