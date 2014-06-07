package database.wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.naming.Name;
import utils.Pair;

public class NamesDatabase {

	private static final String NAMES = "_names";
	
	private DB database;
	
	public NamesDatabase(WalletDatabase walletDatabase, DB database) 
	{
		this.database = database;
	}
	
	public List<Name> getNames(Account account)
	{
		List<Name> names = new ArrayList<Name>();
		
		try
		{
			//OPEN MAP 
			NavigableSet<byte[]> namesSet = this.database.createTreeSet(account.getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
			
			for(byte[] rawName: namesSet)
			{
				Name name = Name.Parse(rawName);
				
				//ADD TO LIST
				names.add(name);
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return names;
	}
	
	public List<Pair<Account, Name>> getNames(List<Account> accounts)
	{
		List<Pair<Account, Name>> names = new ArrayList<Pair<Account, Name>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					//OPEN MAP 
					NavigableSet<byte[]> namesSet = this.database.createTreeSet(account.getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
					
					for(byte[] rawName: namesSet)
					{
						Name name = Name.Parse(rawName);
						
						//ADD TO LIST
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
	
	public void delete(Name name) 
	{
		NavigableSet<byte[]> namesSet = this.database.createTreeSet(name.getOwner().getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		namesSet.remove(name.toBytes());	
	}
	
	public void delete(Account account, Name name) 
	{
		NavigableSet<byte[]> namesSet = this.database.createTreeSet(account.getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		namesSet.remove(name.toBytes());	
	}
	
	public void delete(Account account)
	{
		this.database.delete(account.getAddress() + NAMES);
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public void add(Name name) 
	{
		NavigableSet<byte[]> namesSet = this.database.createTreeSet(name.getOwner().getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		namesSet.add(name.toBytes());
	}

	public void addAll(Map<Account, List<Name>> names) 
	{
		//FOR EACH ACCOUNT
	    for(Account account: names.keySet())
	    {
	    	NavigableSet<byte[]> namesSet = this.database.createTreeSet(account.getAddress() + NAMES).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
	    	//FOR EACH BLOCK
	    	for(Name name: names.get(account))
	    	{
	    		namesSet.add(name.toBytes());
	    	}
	    }
	}

}
