package database;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.BTreeMap;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;

import utils.ObserverMessage;
import at.AT_Transaction;
import database.serializer.ATTransactionSerializer;



public class ATTransactionMap extends DBMap< Tuple2<Integer, Integer> ,  AT_Transaction > { 
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	@SuppressWarnings("rawtypes")
	private NavigableSet senderKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet recipientKey;
	
	public ATTransactionMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);

		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_AT_TX_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_AT_TX);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_AT_TXS);
	}

	public ATTransactionMap(ATTransactionMap parent) 
	{
		super(parent);

	}
	
	protected void createIndexes(DB database){}

	@Override
	protected Map<Tuple2<Integer, Integer>, AT_Transaction> getMap(DB database) 
	{
		//OPEN MAP
		return this.openMap(database);
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, AT_Transaction>  getMemoryMap() 
	{
		DB database = DBMaker.newMemoryDB().make();
		
		//OPEN MAP
		return this.openMap(database);
	}
	
	@SuppressWarnings("unchecked")
	private Map<Tuple2<Integer, Integer>, AT_Transaction>  openMap(DB database)
	{
		//OPEN MAP
		BTreeMap<Tuple2<Integer, Integer>, AT_Transaction> map = database.createTreeMap("at_txs")
				.valueSerializer(new ATTransactionSerializer())
				.makeOrGet();
		
		this.senderKey = database.createTreeSet("sender_at_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.senderKey, new Fun.Function2<String, Tuple2<Integer,Integer>, AT_Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, AT_Transaction val) {
				// TODO Auto-generated method stub
				return val.getSender();
			}
		});
		
		this.recipientKey = database.createTreeSet("recipient_at_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey(map, this.recipientKey, new Fun.Function2<String, Tuple2<Integer,Integer>, AT_Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, AT_Transaction val) {
				return val.getRecipient();
			}
		});
		
		//RETURN
		return map;
	}
	

	@Override
	protected AT_Transaction getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	public boolean add(Integer blockHeight, int seq , AT_Transaction atTx )
	{
		atTx.setBlockHeight(blockHeight);
		atTx.setSeq(seq);
		return this.set(new Tuple2<Integer, Integer>(blockHeight, seq), atTx);
	}
	
	public DBMap<Tuple2<Integer, Integer>, AT_Transaction> getParent()
	{
		return this.parent;
	}
	

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Integer height)
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL ATS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction>) map).subMap(
				Fun.t2(height, null ),
				Fun.t2(height , Fun.HI() ) ).keySet();
		
		//DELETE
		for(Tuple2 key: keys) {
			this.delete(key);
		}
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void deleteAllAfterHeight(Integer height)
	{
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL ATS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction> ) map).subMap(
				Fun.t2(height, null),
				Fun.t2(Fun.HI() , Fun.HI() ) ).keySet();
		
		//DELETE
		for(Tuple2 key: keys) {
			this.delete(key);
		}
	}
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> getATTransactions(Integer  height)
	{
		LinkedHashMap< Tuple2<Integer, Integer> , AT_Transaction> txs = new LinkedHashMap<>();
		BTreeMap map = (BTreeMap) this.map;
		
		//FILTER ALL ATS
		Collection<Tuple2> keys = ((BTreeMap<Tuple2, AT_Transaction>) map).subMap(
				Fun.t2(height, null ),
				Fun.t2(height , Fun.HI() ) ).keySet();
		
		
		for ( Tuple2 key: keys )
		{
			txs.put(  key ,  this.map.get(key) );
		}
		return txs;
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<AT_Transaction> getATTransactionsBySender(String sender)
	{
		Iterable keys = Fun.filter(this.senderKey,sender);
		Iterator iter = keys.iterator();

		List<AT_Transaction> ats = new ArrayList<>();
		while ( iter.hasNext() )
		{
			ats.add(this.map.get(iter.next()));
		}
		
		return ats;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<AT_Transaction> getATTransactionsByRecipient(String recipient)
	{
		Iterable keys = Fun.filter(this.recipientKey,recipient);
		Iterator iter = keys.iterator();

		List<AT_Transaction> ats = new ArrayList<>();
		while ( iter.hasNext() )
		{
			ats.add(this.map.get(iter.next()));
		}
		
		return ats;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Tuple2<Integer,Integer > getNextATTransaction(Integer height, Integer seq, String recipient)
	{
		Iterable keys = Fun.filter(this.recipientKey,recipient);
		Iterator iter = keys.iterator();
		int prevKey = height;
		while ( iter.hasNext() )
		{
			Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
			if ( key.a >= height )
			{
					if (key.a != prevKey)
					{
						seq = 0;
					}
					prevKey = key.a;
					if ( key.b >= seq )
						return key;
			}
		}
		return null;
	}
	

	
	

	
}
