package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.block.Block;

public class ChildDatabase {
	
	private BlockDatabase blockDatabase;
	private ChildDatabase parent;
	private DatabaseSet databaseSet;
	private Map<byte[], byte[]> childrenMap;
	
	public ChildDatabase(DatabaseSet databaseSet, DB database)
	{
		this.blockDatabase = databaseSet.getBlockDatabase();
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.childrenMap = database.createTreeMap("children")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}
	
	public ChildDatabase(BlockDatabase blockDatabase, ChildDatabase parent)
	{
		this.blockDatabase = blockDatabase;
		this.parent = parent;
	    
	    //OPEN MAP
	    this.childrenMap = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}
	
	
	public void setChild(Block parent, Block child)
	{
		try
		{
			//ADD CHILD INTO DB
			this.childrenMap.put(parent.getSignature(), child.getSignature());
			
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
	
	public Block getChild(Block parent)
	{
		try
		{
			//GET CHILD
			if(this.childrenMap.containsKey(parent.getSignature()))
			{
				byte[] childSignature = this.childrenMap.get(parent.getSignature());
				return this.blockDatabase.getBlock(childSignature);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getChild(parent);
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
}
