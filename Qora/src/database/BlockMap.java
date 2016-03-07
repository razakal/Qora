package database;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.mapdb.Atomic.Var;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Serializer;
import org.mapdb.Fun.Tuple2;
import org.mapdb.Fun.Tuple2Comparator;

import com.google.common.primitives.UnsignedBytes;

import qora.block.Block;
import utils.Converter;
import utils.ObserverMessage;
import utils.ReverseComparator;
import database.DBSet;
import database.serializer.BlockSerializer;

public class BlockMap extends DBMap<byte[], Block> 
{
	public static final int HEIGHT_INDEX = 1;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	private Var<byte[]> lastBlockVar;
	private byte[] lastBlockSignature;
	
	private Var<Boolean> processingVar;
	private Boolean processing;
	
	private BTreeMap<Tuple2<String, String>, byte[]> generatorMap;
	
	public BlockMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_BLOCK_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_BLOCK_TYPE);
		
		//LAST BLOCK
		if(database.exists("lastBlock"))
		{
			this.lastBlockVar = database.getAtomicVar("lastBlock");
		}
		else
		{
			this.lastBlockVar = database.createAtomicVar("lastBlock", new byte[0], Serializer.BYTE_ARRAY);
		}
		this.lastBlockSignature = this.lastBlockVar.get();
		
		//PROCESSING
		if(database.exists("processingBlock"))
		{
			this.processingVar = database.getAtomicVar("processingBlock");
		}
		else
		{
			this.processingVar = database.createAtomicVar("processingBlock", false, Serializer.BOOLEAN);
		}
		
		this.processing = this.processingVar.get();
	}

	public BlockMap(BlockMap parent) 
	{
		super(parent);
		
		this.lastBlockSignature = parent.getLastBlockSignature();
		this.processing = parent.isProcessing();
	}
	
	@SuppressWarnings({"unchecked", "rawtypes"})
	protected void createIndexes(DB database)
	{
		//HEIGHT INDEX
		Tuple2Comparator<Integer, byte[]> comparator = new Fun.Tuple2Comparator<Integer, byte[]>(Fun.COMPARATOR, UnsignedBytes.lexicographicalComparator());
		NavigableSet<Tuple2<Integer, byte[]>> heightIndex = database.createTreeSet("blocks_index_height")
				.comparator(comparator)
				.makeOrGet();
		
		NavigableSet<Tuple2<Integer, byte[]>> descendingHeightIndex = database.createTreeSet("blocks_index_height_descending")
				.comparator(new ReverseComparator(comparator))
				.makeOrGet();
		
		createIndex(HEIGHT_INDEX, heightIndex, descendingHeightIndex, new Fun.Function2<Integer, byte[], Block>() {
		   	@Override
		    public Integer run(byte[] key, Block value) {
		   		return value.getHeight();
		    }
		});
		
		generatorMap = database.createTreeMap("generators_index").makeOrGet();
		
		Bind.secondaryKey((BTreeMap)this.map, generatorMap, new Fun.Function2<Tuple2<String, String>, byte[], Block>() {
			@Override
			public Tuple2<String, String> run(byte[] b, Block block) {
				return new Tuple2<String, String>(block.getGenerator().getAddress(), Converter.toHex(block.getSignature()));
			}
		});
	}

	@Override
	protected Map<byte[], Block> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("blocks")
				.keySerializer(BTreeKeySerializer.BASIC)
				.comparator(UnsignedBytes.lexicographicalComparator())
				.valueSerializer(new BlockSerializer())
				.valuesOutsideNodesEnable()
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<byte[], Block> getMemoryMap() 
	{
		return new TreeMap<byte[], Block>(UnsignedBytes.lexicographicalComparator());
	}

	@Override
	protected Block getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public void setLastBlock(Block block) 
	{		
		if(this.lastBlockVar != null)
		{
			this.lastBlockVar.set(block.getSignature());
		}
		
		this.lastBlockSignature = block.getSignature();
	}
	
	public Block getLastBlock()
	{
		return this.get(this.getLastBlockSignature());
	}
	
	public byte[] getLastBlockSignature()
	{
		return this.lastBlockSignature;
	}
	
	public boolean isProcessing() 
	{
		if(this.processing != null)
		{
			return this.processing.booleanValue();
		}
		
		return false;
	}
	
	public void setProcessing(boolean processing)
	{
		if(this.processingVar != null)
		{
			this.processingVar.set(processing);
		}
		
		this.processing = processing;
	}
	
	public void add(Block block)
	{
		this.set(block.getSignature(), block);
	}
	
	public void delete(Block block)
	{
		this.delete(block.getSignature());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Collection<byte[]> getGeneratorBlocks(String address)
	{
		Collection<byte[]> blocks = ((BTreeMap)(this.generatorMap))
				.subMap(Fun.t2(address, null), Fun.t2(address,Fun.HI())).values();
		
		return blocks;
	}
	
}
