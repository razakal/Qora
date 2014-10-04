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
import qora.naming.Name;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBMap;
import database.serializer.NameSerializer;

public class NameMap extends DBMap<Tuple2<String, String>, Name>
{
	public static final int NAME_INDEX = 1;
	public static final int OWNER_INDEX = 2;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public NameMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_NAME_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_NAME_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_NAME_TYPE);
	}

	public NameMap(NameMap parent) 
	{
		super(parent);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//NAME INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("names_index_name")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("names_index_name_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, Name>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Name value) {
		   		return value.getName();
		    }
		});
		
		//OWNER INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> ownerIndex = database.createTreeSet("names_index_owner")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingOwnerIndex = database.createTreeSet("names_index_owner_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(OWNER_INDEX, ownerIndex, descendingOwnerIndex, new Fun.Function2<String, Tuple2<String, String>, Name>() {
		   	@Override
		    public String run(Tuple2<String, String> key, Name value) {
		   		return value.getOwner().getAddress();
		    }
		});
	}

	@Override
	protected Map<Tuple2<String, String>, Name> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("names")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new NameSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, Name> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, Name>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Name getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Name> get(Account account)
	{
		List<Name> names = new ArrayList<Name>();
		
		try
		{
			Map<Tuple2<String, String>, Name> accountNames = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<Name> iterator = accountNames.values().iterator();
			
			while(iterator.hasNext())
			{
				names.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return names;
	}
	
	public List<Pair<Account, Name>> get(List<Account> accounts)
	{
		List<Pair<Account, Name>> names = new ArrayList<Pair<Account, Name>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<Name> accountNames = get(account);
					for(Name name: accountNames)
					{
						names.add(new Pair<Account, Name>(account, name));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return names;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL NAMES THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, Name> accountNames = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountNames.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(Name name)
	{
		this.delete(name.getOwner(), name);
	}
	
	public void delete(Account account, Name name) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), name.getName()));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(Name name)
	{
		return this.set(new Tuple2<String, String>(name.getOwner().getAddress(), name.getName()), name);
	}
	
	public void addAll(Map<Account, List<Name>> names)
	{
		//FOR EACH ACCOUNT
	    for(Account account: names.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(Name name: names.get(account))
	    	{
	    		this.add(name);
	    	}
	    }
	}
}
