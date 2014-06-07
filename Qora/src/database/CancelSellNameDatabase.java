package database;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.naming.NameSale;
import qora.transaction.Transaction;

public class CancelSellNameDatabase {
	
	private CancelSellNameDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<byte[], BigDecimal> orphanDataMap;	
	
	public CancelSellNameDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.orphanDataMap = database.createTreeMap("cancelNameOrphanData")
			.keySerializer(BTreeKeySerializer.BASIC)
			.comparator(UnsignedBytes.lexicographicalComparator())
			.makeOrGet();
	}
	
	public CancelSellNameDatabase(CancelSellNameDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.orphanDataMap = new TreeMap<byte[], BigDecimal>(UnsignedBytes.lexicographicalComparator());
	}
	
	public void setOrphanData(Transaction transaction, NameSale nameSale)
	{
		try
		{
			//ADD NAME INTO DB
			this.orphanDataMap.put(transaction.getSignature(), nameSale.getAmount());
			
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
	
	public BigDecimal getOrphanData(Transaction transaction)
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
