package database.wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.BTreeMap;

import qora.account.Account;
import qora.voting.Poll;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBMap;
import database.serializer.PollSerializer;

public class PollMap extends DBMap<Tuple2<String, String>, Poll>
{
	public static final int NAME_INDEX = 1;
	public static final int CREATOR_INDEX = 2;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public PollMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_POLL_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_POLL_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_POLL_TYPE);
	}

	public PollMap(PollMap parent) 
	{
		super(parent);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//NAME INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("polls_index_name")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("polls_index_name_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Poll value) {
		   		return value.getName();
		    }
		});
		
		//CREATOR INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> creatorIndex = database.createTreeSet("polls_index_creator")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingCreatorIndex = database.createTreeSet("polls_index_creator_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(CREATOR_INDEX, creatorIndex, descendingCreatorIndex, new Fun.Function2<String, Tuple2<String, String>, Poll>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Poll poll) {
		   		return key.a;
		    }
		});
	}

	@Override
	protected Map<Tuple2<String, String>, Poll> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("polls")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new PollSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, Poll> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, Poll>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Poll getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Poll> get(Account account)
	{
		List<Poll> polls = new ArrayList<Poll>();
		
		try
		{
			Map<Tuple2<String, String>, Poll> accountPolls = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<Poll> iterator = accountPolls.values().iterator();
			
			while(iterator.hasNext())
			{
				polls.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return polls;
	}
	
	public List<Pair<Account, Poll>> get(List<Account> accounts)
	{
		List<Pair<Account, Poll>> polls = new ArrayList<Pair<Account, Poll>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<Poll> accountPolls = get(account);
					for(Poll poll: accountPolls)
					{
						polls.add(new Pair<Account, Poll>(account, poll));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return polls;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, Poll> accountPolls = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountPolls.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(Poll poll)
	{
		this.delete(poll.getCreator(), poll);
	}
	
	public void delete(Account account, Poll poll) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), poll.getName()));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(Poll poll)
	{
		return this.set(new Tuple2<String, String>(poll.getCreator().getAddress(), poll.getName()), poll);
	}
	
	public void addAll(Map<Account, List<Poll>> polls)
	{
		//FOR EACH ACCOUNT
	    for(Account account: polls.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(Poll poll: polls.get(account))
	    	{
	    		this.add(poll);
	    	}
	    }
	}
}
