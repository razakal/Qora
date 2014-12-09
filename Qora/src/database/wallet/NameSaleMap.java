package database.wallet;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableSet;
import java.util.TreeMap;

import org.mapdb.BTreeKeySerializer;
import org.mapdb.DB;
import org.mapdb.Fun;
import org.mapdb.Fun.Tuple2;
import org.mapdb.BTreeMap;

import qora.account.Account;
import qora.naming.NameSale;
import utils.ObserverMessage;
import utils.Pair;
import utils.ReverseComparator;
import database.DBMap;

public class NameSaleMap extends DBMap<Tuple2<String, String>, BigDecimal>
{
	public static final int NAME_INDEX = 1;
	public static final int SELLER_INDEX = 2;
	public static final int AMOUNT_INDEX = 3;
	
	private Map<Integer, Integer> observableData = new HashMap<Integer, Integer>();
	
	public NameSaleMap(WalletDatabase walletDatabase, DB database)
	{
		super(walletDatabase, database);
		
		this.observableData.put(DBMap.NOTIFY_ADD, ObserverMessage.ADD_NAME_SALE_TYPE);
		this.observableData.put(DBMap.NOTIFY_REMOVE, ObserverMessage.REMOVE_NAME_SALE_TYPE);
		this.observableData.put(DBMap.NOTIFY_LIST, ObserverMessage.LIST_NAME_SALE_TYPE);
	}

	public NameSaleMap(NameSaleMap parent) 
	{
		super(parent);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected void createIndexes(DB database)
	{
		//NAME INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> nameIndex = database.createTreeSet("namesales_index_name")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingNameIndex = database.createTreeSet("namesales_index_name_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(NAME_INDEX, nameIndex, descendingNameIndex, new Fun.Function2<String, Tuple2<String, String>, BigDecimal>() {
		   	@Override
		    public String run(Tuple2<String, String> key, BigDecimal value) {
		   		return key.b;
		    }
		});
		
		//SELLER INDEX
		NavigableSet<Tuple2<String, Tuple2<String, String>>> ownerIndex = database.createTreeSet("namesales_index_seller")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
		
		NavigableSet<Tuple2<String, Tuple2<String, String>>> descendingOwnerIndex = database.createTreeSet("namesales_index_seller_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
		
		createIndex(SELLER_INDEX, ownerIndex, descendingOwnerIndex, new Fun.Function2<String, Tuple2<String, String>, BigDecimal>() {
		   	@Override
		    public String run(Tuple2<String, String> key, BigDecimal value) {
		   		return key.a;
		    }
		});
		
		//AMOUNT INDEX
		NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> amountIndex = database.createTreeSet("namesales_index_amount")
				.comparator(Fun.COMPARATOR)
				.makeOrGet();
			
		NavigableSet<Tuple2<BigDecimal, Tuple2<String, String>>> descendingAmountIndex = database.createTreeSet("namesales_index_amount_descending")
				.comparator(new ReverseComparator(Fun.COMPARATOR))
				.makeOrGet();
				
		createIndex(SELLER_INDEX, amountIndex, descendingAmountIndex, new Fun.Function2<BigDecimal, Tuple2<String, String>, BigDecimal>() {
		   	@Override
		    public BigDecimal run(Tuple2<String, String> key, BigDecimal value) {
		   		return value;
		    }
		});
	}

	@Override
	protected Map<Tuple2<String, String>, BigDecimal> getMap(DB database) 
	{
		//OPEN MAP
		return database.createTreeMap("namesales")
				.keySerializer(BTreeKeySerializer.TUPLE2)
				.counterEnable()
				.makeOrGet();
	}

	@Override
	protected Map<Tuple2<String, String>, BigDecimal> getMemoryMap() 
	{
		return new TreeMap<Tuple2<String, String>, BigDecimal>(Fun.TUPLE2_COMPARATOR);
	}

	@Override
	protected BigDecimal getDefaultValue() 
	{
		return BigDecimal.ZERO.setScale(8);
	}
	
	@Override
	protected Map<Integer, Integer> getObservableData() 
	{
		return this.observableData;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List<NameSale> get(Account account)
	{
		List<NameSale> nameSales = new ArrayList<NameSale>();
		
		try
		{
			Map<Tuple2<String, String>, BigDecimal> accountNames = ((BTreeMap) this.map).subMap(
					Fun.t2(account.getAddress(), null),
					Fun.t2(account.getAddress(), Fun.HI()));
			
			for(Entry<Tuple2<String, String>, BigDecimal> entry: accountNames.entrySet())
			{
				NameSale nameSale = new NameSale(entry.getKey().b, entry.getValue());
				nameSales.add(nameSale);
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return nameSales;
	}
	
	public List<Pair<Account, NameSale>> get(List<Account> accounts)
	{
		List<Pair<Account, NameSale>> nameSales = new ArrayList<Pair<Account, NameSale>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					List<NameSale> accountNameSales = get(account);
					for(NameSale nameSale: accountNameSales)
					{
						nameSales.add(new Pair<Account, NameSale>(account, nameSale));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return nameSales;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public void delete(Account account)
	{
		//GET ALL NAMES THAT BELONG TO THAT ADDRESS
		Map<Tuple2<String, String>, BigDecimal> accountNameSales = ((BTreeMap) this.map).subMap(
				Fun.t2(account.getAddress(), null),
				Fun.t2(account.getAddress(), Fun.HI()));
		
		//DELETE NAMES
		for(Tuple2<String, String> key: accountNameSales.keySet())
		{
			this.delete(key);
		}
	}
	
	public void delete(NameSale nameSale)
	{
		this.delete(nameSale.getName().getOwner(), nameSale);
	}
	
	public void delete(Account account, NameSale nameSale) 
	{
		this.delete(new Tuple2<String, String>(account.getAddress(), nameSale.getKey()));	
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(NameSale nameSale)
	{
		return this.set(new Tuple2<String, String>(nameSale.getName().getOwner().getAddress(), nameSale.getKey()), nameSale.getAmount());
	}
	
	public void addAll(Map<Account, List<NameSale>> nameSales)
	{
		//FOR EACH ACCOUNT
	    for(Account account: nameSales.keySet())
	    {
	    	//FOR EACH TRANSACTION
	    	for(NameSale nameSale: nameSales.get(account))
	    	{
	    		this.add(nameSale);
	    	}
	    }
	}
}
