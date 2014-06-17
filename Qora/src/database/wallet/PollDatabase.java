package database.wallet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.DB;

import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.voting.Poll;
import utils.Pair;

public class PollDatabase {

	private static final String POLLS = "_POLLS";
	
	private DB database;
	
	public PollDatabase(WalletDatabase walletDatabase, DB database) 
	{
		this.database = database;
	}
	
	public List<Poll> getPolls(Account account)
	{
		List<Poll> polls = new ArrayList<Poll>();
		
		try
		{
			//OPEN MAP 
			NavigableSet<byte[]> pollsSet = this.database.createTreeSet(account.getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
			
			for(byte[] rawPoll: pollsSet)
			{
				Poll poll = Poll.parse(rawPoll);
				
				//ADD TO LIST
				polls.add(poll);
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return polls;
	}
	
	public List<Pair<Account, Poll>> getPolls(List<Account> accounts)
	{
		List<Pair<Account, Poll>> names = new ArrayList<Pair<Account, Poll>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					//OPEN MAP 
					NavigableSet<byte[]> pollsSet = this.database.createTreeSet(account.getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
					
					for(byte[] rawPoll: pollsSet)
					{
						Poll poll = Poll.parse(rawPoll);
						
						//ADD TO LIST
						names.add(new Pair<Account, Poll>(account, poll));
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
	
	public void delete(Poll poll) 
	{
		NavigableSet<byte[]> namesSet = this.database.createTreeSet(poll.getCreator().getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		namesSet.remove(poll.toBytes());	
	}
	
	public void delete(Account account, Poll poll) 
	{
		NavigableSet<byte[]> namesSet = this.database.createTreeSet(account.getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		namesSet.remove(poll.toBytes());	
	}
	
	public void delete(Account account)
	{
		this.database.delete(account.getAddress() + POLLS);
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public void add(Poll poll) 
	{
		NavigableSet<byte[]> pollsSet = this.database.createTreeSet(poll.getCreator().getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
		pollsSet.add(poll.toBytes());
	}
	
	public void update(Poll poll) {
		
		//DELETE PREVIOUS POLL WITH SAME NAME
		for(Poll pollb: this.getPolls(poll.getCreator()))
		{
			if(pollb.getName().equals(poll.getName()))
			{
				this.delete(pollb);
			}
		}
		
		//ADD NEW POLL
		this.add(poll);
		
	}

	public void addAll(Map<Account, List<Poll>> polls) 
	{
		//FOR EACH ACCOUNT
	    for(Account account: polls.keySet())
	    {
	    	NavigableSet<byte[]> pollsSet = this.database.createTreeSet(account.getAddress() + POLLS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		   
	    	//FOR EACH BLOCK
	    	for(Poll poll: polls.get(account))
	    	{
	    		pollsSet.add(poll.toBytes());
	    	}
	    }
	}

}
