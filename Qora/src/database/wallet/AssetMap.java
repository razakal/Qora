package database.wallet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.BTreeMap;

import qora.account.Account;
import qora.assets.Asset;
import utils.ObserverMessage;
import utils.Pair;
import database.DBMap;
import database.serializer.AssetSerializer;
public class AssetMap extends DBMap<Tuple2<String, String>, Asset>
{
	public static final int NAME_INDEX = 1;
	public static final int CREATOR_INDEX = 2;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public AssetMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_ASSET_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_ASSET_TYPE);
	}

	public AssetMap(AssetMap parent) 
	{
		super(parent);
	}
	
	//@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//NAME INDEX
		/*NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("polls_index_name")
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
		});*/
	}

	@Override
	protected Map<Tuple2<String, String>, Asset> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("asset")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.valueSerializer(new AssetSerializer())
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, Asset> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, Asset>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected Asset getDefaultValue() 
	{
		return null;
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<Asset> get(Account account)
	{
		List<Asset> assets = new ArrayList<Asset>();
		
		try
		{
			Map<Tuple2<String, String>, Asset> accountAssets = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			//GET ITERATOR
			Iterator<Asset> iterator = accountAssets.values().iterator();
			
			while(iterator.hasNext())
			{
				assets.add(iterator.next());
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return assets;
	}
	
	public List<Pair<Account, Asset>> get(List<Account> accounts)
	{
		List<Pair<Account, Asset>> assets = new ArrayList<Pair<Account, Asset>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<Asset> accountAssets = get(account);
					for(Asset asset: accountAssets)
					{
						assets.add(new Pair<Account, Asset>(account, asset));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return assets;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL POLLS THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, Asset> accountAssets = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountAssets.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(Asset asset)
	{
		this.delete(asset.getOwner(), asset);
	}
	
	public void delete(Account account, Asset asset) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), new String(asset.getReference())));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(Asset asset)
	{
		return this.set(new Tuple2<String, String>(asset.getOwner().getAddress(), new String(asset.getReference())), asset);
	}
	
	public void addAll(Map<Account, List<Asset>> assets)
	{
		//FOR EACH ACCOUNT
	    for(Account account: assets.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(Asset asset: assets.get(account))
	    	{
	    		this.add(asset);
	    	}
	    }
	}
}
