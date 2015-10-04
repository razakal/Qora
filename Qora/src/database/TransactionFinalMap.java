package database;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.apache.commons.lang3.ArrayUtils;
import org.mapdb.BTreeKeySerializer;
import org.mapdb.Bind;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.BTreeMap;

import at.AT_Transaction;
import qora.account.Account;
import qora.transaction.GenesisTransaction;
import qora.transaction.Transaction;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBMap;
import database.serializer.TransactionSerializer;

public class TransactionFinalMap extends DBMap<Tuple2<Integer, Integer>, Transaction>
{
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	@SuppressWarnings("rawtypes")
	private NavigableSet senderKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet recipientKey;
	@SuppressWarnings("rawtypes")
	private NavigableSet typeKey;
	
	public TransactionFinalMap(DBSet databaseSet, DB database)
	{
		super(databaseSet, database);
		
		/*this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_TRANSACTION_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_TRANSACTION_TYPE);*/
	}

	public TransactionFinalMap(TransactionFinalMap parent) 
	{
		super(parent);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		this.senderKey = database.createTreeSet("sender_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKey((BTreeMap)map, this.senderKey, new Fun.Function2<String, Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String run(Tuple2<Integer, Integer> key, Transaction val) {
				// TODO Auto-generated method stub
				if ( val instanceof GenesisTransaction )
					return "genesis";
				return val.getCreator().getAddress();
			}
		});
		
		this.recipientKey = database.createTreeSet("recipient_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys((BTreeMap)map, this.recipientKey, new Fun.Function2<String[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public String[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<String> recps = new ArrayList<String>();
				for ( Account acc : val.getInvolvedAccounts())
				{
					if ( val.getAmount(acc).compareTo( BigDecimal.ZERO) > 0 )
					{
						recps.add(acc.getAddress());
					}
				}
				String[] ret = new String[ recps.size() ];
				ret = recps.toArray( ret );
				return ret;
			}
		});
		

		
		this.typeKey = database.createTreeSet("address_type_txs")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		Bind.secondaryKeys((BTreeMap)map, this.typeKey, new Fun.Function2<Tuple2<String, Integer>[], Tuple2<Integer,Integer>, Transaction>(){
			@Override
			public Tuple2<String, Integer>[] run(Tuple2<Integer, Integer> key, Transaction val) {
				List<Tuple2<String, Integer>> recps = new ArrayList<Tuple2<String, Integer>>();
				Integer type = val.getType();
				for ( Account acc : val.getInvolvedAccounts())
				{
						recps.add(new Tuple2<String, Integer>(acc.getAddress(),type));
					
				}
				//Tuple2<Integer, String>[] ret = (Tuple2<Integer, String>[]) new Object[ recps.size() ];
				Tuple2<String, Integer>[] ret = (Tuple2<String, Integer>[]) Array.newInstance(Fun.Tuple2.class,recps.size());
				ret = recps.toArray( ret );
				return ret;
			}
		});
		
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("final_transactions")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new TransactionSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<Integer, Integer>, Transaction> getMemoryMap() 
	{
		return new TreeMap<Tuple2<Integer, Integer>, Transaction>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Transaction getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Integer height)
	{
		//GET ALL TRANSACTIONS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<Integer, Integer>, Transaction> heightTransactions = ((BTreeMap) this.map).subMap(
				Fun.t2(height, null),
				Fun.t2(height, Fun.HI()));
		
		//DELETE TRANSACTIONS
		for(Tuple2<Integer, Integer> key: heightTransactions.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(Integer height, Integer seq)
	{
		this.delete(new Tuple2<Integer, Integer>(height, seq));
	}
	
	
	public boolean add(Integer height, Integer seq, Transaction transaction)
	{
		return this.set(new Tuple2<Integer, Integer>(height, seq), transaction);
	}
	
	public Transaction getTransaction(Integer height, Integer seq)
	{
		Transaction tx = this.get(new Tuple2<Integer,Integer>(height, seq));
		if ( this.parent != null )
		{
			if ( tx == null )
			{
				return this.parent.get(new Tuple2<Integer,Integer>(height, seq));
			}
		}
		return tx;
	}
	
	public List<Transaction> getTransactionsByRecipient(String address)
	{
		return getTransactionsByRecipient(address, 0);
	}
	
	public List<Transaction> getTransactionsByRecipient(String address, int limit)
	{
		Iterable keys = Fun.filter(this.recipientKey, address);
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		return txs;
	}
	
	public List<Transaction> getTransactionsBySender(String address)
	{
		return getTransactionsByRecipient(address, 0);
	}
	
	public List<Transaction> getTransactionsBySender(String address, int limit)
	{
		Iterable keys = Fun.filter(this.senderKey, address);
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		return txs;
	}
	
	public List<Transaction> getTransactionsByTypeAndAddress(String address, Integer type, int limit)
	{
		Iterable keys = Fun.filter(this.typeKey, new Tuple2<String, Integer>(address, type));
		Iterator iter = keys.iterator();

		List<Transaction> txs = new ArrayList<>();
		int counter=0;
		while ( iter.hasNext() && (limit ==0 || counter<limit) )
		{
			txs.add(this.map.get(iter.next()));
			counter++;
		}
		
		return txs;
	}

	public Tuple2<Integer, Integer> getTransactionsAfterTimestamp(int startHeight, int numOfTx,
			String address) {
		Iterable keys = Fun.filter(this.recipientKey, address);
		Iterator iter = keys.iterator();
		int prevKey = startHeight;
		while ( iter.hasNext() )
		{
			Tuple2<Integer, Integer> key = (Tuple2<Integer, Integer>) iter.next();
			if ( key.a >= startHeight )
			{
					if (key.a != prevKey)
					{
						numOfTx = 0;
					}
					prevKey = key.a;
					if ( key.b > numOfTx)
						return key;
			}
		}
		
		return null;
		
		
	}

	public DBMap<Tuple2<Integer, Integer>, Transaction> getParent() {
		// TODO Auto-generated method stub
		return this.parent;
	}
}
