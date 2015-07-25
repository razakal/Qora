package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import qora.block.Block;
import database.DBMap;

public class BlocksOfAddressMap extends DBMap<Tuple2<String, String>, byte[]> 
{
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public BlocksOfAddressMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
	}

	public BlocksOfAddressMap(BlocksOfAddressMap parent) 
	{
		super(parent);
	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<String, String>, byte[]> getMap(DB database) 
	{
		//OPEN MAP
		
		return database.createTreeMap("blocksofaddress")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, byte[]> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, byte[]>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected byte[] getDefaultValue() 
	{
		return new byte[0];
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	public boolean add(String address, Block block)
	{
		return this.set(new Tuple2<String, String>(address, new String(block.getSignature())), block.getSignature());
	}
	
	public void remove(String address, Block block)
	{
		this.delete(new Tuple2<String, String>(address, new String(block.getSignature())));
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<byte[]> get(String address, int limit)
	{
		List<byte[]> signblocks = new ArrayList<byte[]>();
		
		try
		{
			Map<Tuple2<String, String>, byte[]> accountBlocks = ((BTreeMap) this.map).subMap(
					Fun.t2(address, null),
					Fun.t2(address, Fun.HI()));
			
			//GET ITERATOR
			Iterator<byte[]> iterator = accountBlocks.values().iterator();
			
			//RETURN {LIMIT} TRANSACTIONS
			int counter = 0;
			while(iterator.hasNext() && ((counter < limit) || limit == -1))
			{
				signblocks.add(iterator.next());
				counter++;
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return signblocks;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public boolean isBlock(byte[] blockSignature)
	{
		//String address, 
		String strBlockSignature = new String(blockSignature);
		
		try
		{
			Map<Tuple2<String, String>, byte[]> blocks = ((BTreeMap) this.map).subMap(
					Fun.t2(null, strBlockSignature),
					Fun.t2(Fun.HI(), strBlockSignature));
			
			return blocks.size()>0;	
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return false;
	}
}

