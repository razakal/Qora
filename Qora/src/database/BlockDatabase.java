package database;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.Map;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.block.Block;
import qora.block.BlockFactory;

public class BlockDatabase {
	
	private static final byte[] LAST_BLOCK = new byte[]{1, 2};
	
	private BlockDatabase parent;
	private DatabaseSet databaseSet;
	private Map<byte[], byte[]> blockMap;
	
	public BlockDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.blockMap = database.createTreeMap("blocks")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.valuesOutsideNodesEnable()
				.makeOrGet();
	}
	
	public BlockDatabase(BlockDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.blockMap = new TreeMap<byte[], byte[]>(UnsignedBytes.lexicographicalComparator());
	}
	
	public List<Block> getAll()
	{
		List<Block> blocks = new ArrayList<Block>();
		for(byte[] signature: this.blockMap.keySet())
		{
			blocks.add(this.getBlock(signature));
		}
		
		return blocks;
	}
	
	public void addBlock(Block block)
	{
		try
		{
			//ADD BLOCK INTO DB
			this.blockMap.put(block.getSignature(), block.toBytes());
			
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
	
	public Block getBlock(byte[] signature)
	{
		try
		{
			if(this.blockMap.containsKey(signature))
			{
				return BlockFactory.getInstance().parse(this.blockMap.get(signature));
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getBlock(signature);
				}
			}
			
			return null;
		}
		catch(Exception e)
		{
			//NO BLOCK FOUND
			return null;
		}	
	}

	public void deleteBlock(Block block) 
	{
		try
		{
			//REMOVE
			if(this.blockMap.containsKey(block.getSignature()))
			{
				this.blockMap.remove(block.getSignature());
			}
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			//NO BLOCK FOUND
		}		
	}
	
	public boolean containsBlock(byte[] signature)
	{
		if(this.blockMap.containsKey(signature))
		{
			return true;
		}
		else
		{
			if(this.parent != null)
			{
				return this.parent.containsBlock(signature);
			}
		}
		
		return false;
	}
	
	public void setLastBlock(Block block) 
	{		
		try
		{
			//UPDATE LAST BLOCK
			this.blockMap.put(LAST_BLOCK, block.getSignature());
			
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
	
	public Block getLastBlock()
	{
		if(this.blockMap.containsKey(LAST_BLOCK))
		{
			byte[] signature = this.blockMap.get(LAST_BLOCK);
			return this.getBlock(signature);
		}
		else
		{
			if(this.parent != null)
			{
				return parent.getLastBlock();
			}
		}
		
		return null;
	}
	
	public byte[] getLastBlockSignature()
	{
		if(this.blockMap.containsKey(LAST_BLOCK))
		{
			return this.blockMap.get(LAST_BLOCK);
		}
		else
		{
			if(this.parent != null)
			{
				return parent.getLastBlockSignature();
			}
		}
		
		return null;
	}
}
