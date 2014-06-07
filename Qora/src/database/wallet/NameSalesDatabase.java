package database.wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.naming.NameSale;
import utils.Pair;

public class NameSalesDatabase {

	private static final String NAME_SALES = "_nameSales";

	private DB database;
	
	public NameSalesDatabase(WalletDatabase walletDatabase, DB database) 
	{
		this.database = database;
	}
	
	public List<NameSale> getNameSales(Account account)
	{
		List<NameSale> nameSales = new ArrayList<NameSale>();
		
		try
		{
			//OPEN MAP 
			NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(account.getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
			
			for(byte[] rawNameSale: nameSalesSet)
			{
				NameSale nameSale = NameSale.Parse(rawNameSale);
				
				//ADD TO LIST
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
	
	public List<Pair<Account, NameSale>> getNameSales(List<Account> accounts)
	{
		List<Pair<Account, NameSale>> nameSales = new ArrayList<Pair<Account, NameSale>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					//OPEN MAP 
					NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(account.getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
					
					for(byte[] rawNameSale: nameSalesSet)
					{
						NameSale nameSale = NameSale.Parse(rawNameSale);
						
						//ADD TO LIST
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
	
	public void delete(NameSale nameSale) 
	{
		NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(nameSale.getName().getOwner().getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		nameSalesSet.remove(nameSale.toBytes());	
	}
	
	public void delete(Account account, NameSale nameSale) 
	{
		NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(account.getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		nameSalesSet.remove(nameSale.toBytes());	
	}
	
	public void delete(Account account)
	{
		this.database.delete(account.getAddress() + NAME_SALES);
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public void add(NameSale nameSale) 
	{
		NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(nameSale.getName().getOwner().getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		nameSalesSet.add(nameSale.toBytes());
	}

	public void addAll(Map<Account, List<NameSale>> nameSales) 
	{
		//FOR EACH ACCOUNT
	    for(Account account: nameSales.keySet())
	    {
	    	NavigableSet<byte[]> nameSalesSet = this.database.createTreeSet(account.getAddress() + NAME_SALES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
	    	//FOR EACH BLOCK
	    	for(NameSale nameSale: nameSales.get(account))
	    	{
	    		nameSalesSet.add(nameSale.toBytes());
	    	}
	    }
	}
}
