package database;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;

import qora.block.Block;

import com.google.common.primitives.UnsignedBytes;

public class HeightMap extends DBMap<byte[], Integer> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Map<Integer,byte[]> heightIndex;
	
	public HeightMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public HeightMap(HeightMap parent) 
	{
		super(parent);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database){
		heightIndex = database.createTreeMap("block_height_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, heightIndex, new Fun.Function2<Integer, byte[], Integer>() {
			@Override
			public Integer run(byte[] arg0, Integer arg1) {
				// TODO Auto-generated method stub
				return arg1;
			}
		
		});
	}

	@Override
	protected Map<byte[], Integer> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("height")
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
	
	public int get(Block block)
	{
		return this.get(block.getSignature());
	}
	
	public byte[] getBlockByHeight(int height)
	{
		return heightIndex.get(height);
	}
	
	public void set(Block block, int height)
	{
		this.set(block.getSignature(), height);
	}
}