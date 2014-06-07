package database;

import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.block.Block;

public class HeightDatabase {
	
	private HeightDatabase parent;
	private DatabaseSet databaseSet;
	private Map<byte[], Integer> heightMap;
	
	public HeightDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.heightMap = database.createTreeMap("height")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.makeOrGet();
	}
	
	public HeightDatabase(HeightDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.heightMap = new TreeMap<byte[], Integer>(UnsignedBytes.lexicographicalComparator());
	}
	
	public void setHeight(Block block, int height)
	{
		try
		{
			//ADD HEIGHT INTO DB
			this.heightMap.put(block.getSignature(), height);
			
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
	
	public int getHeight(Block block)
	{
		try
		{
			//GET HEIGHT
			if(this.heightMap.containsKey(block.getSignature()))
			{
				return this.heightMap.get(block.getSignature());
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getHeight(block);
				}
			}
			
			return -1;
		}
		catch(Exception e)
		{
			//ACCOUNT NOT KNOWN SO BALANCE IS 0
			return -1;
		}			
	}
	
	public int getHeightBySignature(byte[] signature)
	{
		try
		{
			//GET HEIGHT
			if(this.heightMap.containsKey(signature))
			{
				return this.heightMap.get(signature);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getHeightBySignature(signature);
				}
			}
			
			return -1;
		}
		catch(Exception e)
		{
			//ACCOUNT NOT KNOWN SO BALANCE IS 0
			return -1;
		}			
	}
}
