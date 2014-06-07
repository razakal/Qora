package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.block.Block;
import qora.transaction.Transaction;

public class TransactionParentDatabase {
	
	private BlockDatabase blockDatabase;
	private TransactionParentDatabase parent;
	private DatabaseSet databaseSet;
	private Map<byte[], byte[]> parentsMap;
	
	public TransactionParentDatabase(DatabaseSet databaseSet, DB database)
	{
		this.blockDatabase = databaseSet.getBlockDatabase();
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.parentsMap = database.createTreeMap("transactionParents")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}
	
	public TransactionParentDatabase(BlockDatabase blockDatabase, TransactionParentDatabase parent)
	{
		this.blockDatabase = blockDatabase;
		this.parent = parent;
	    
	    //OPEN MAP
	    this.parentsMap = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}
	
	
	public void setParent(Transaction transaction, Block parent)
	{
		try
		{
			//ADD CHILD INTO DB
			this.parentsMap.put(transaction.getSignature(), parent.getSignature());
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}			
	}
	
	public Block getParent(byte[] transaction)
	{
		try
		{
			//GET CHILD
			if(this.parentsMap.containsKey(transaction))
			{
				byte[] parentSignature = this.parentsMap.get(transaction);
				return this.blockDatabase.getBlock(parentSignature);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getParent(transaction);
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
	
	public Block getParent(Transaction transaction)
	{
		try
		{
			//GET CHILD
			if(this.parentsMap.containsKey(transaction.getSignature()))
			{
				byte[] parentSignature = this.parentsMap.get(transaction.getSignature());
				return this.blockDatabase.getBlock(parentSignature);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getParent(transaction);
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
	
	public byte[] getParentSignature(Transaction transaction)
	{
		try
		{
			//GET CHILD
			if(this.parentsMap.containsKey(transaction.getSignature()))
			{
				return this.parentsMap.get(transaction.getSignature());
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getParentSignature(transaction);
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
	
	public boolean contains(Transaction transaction)
	{
		try
		{
			//GET CHILD
			if(this.parentsMap.containsKey(transaction.getSignature()))
			{
				return true;
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.contains(transaction);
				}
			}
			
			return false;
		}
		catch(Exception e)
		{
			//NO CHILD FOUND
			return false;
		}	
	}
	
}
