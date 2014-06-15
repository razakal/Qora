package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.DB;

import qora.voting.Poll;
import utils.ObserverMessage;

public class PollDatabase extends Observable {
	
	private PollDatabase parent;
	private DatabaseSet databaseSet;
	private Map<String, byte[]> pollMap;
	private List<String> deleted;
	
	public PollDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.pollMap = database.getTreeMap("polls");
	}
	
	public PollDatabase(PollDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.pollMap = new HashMap<String, byte[]>();
	    this.deleted = new ArrayList<String>();
	}
	
	public Poll getPoll(String name)
	{
		try
		{
			if(this.pollMap.containsKey(name))
			{
				return Poll.parse(this.pollMap.get(name));
			}
			else
			{
				if(deleted == null || !deleted.contains(name))
				{
					if(this.parent != null)
					{
						return this.parent.getPoll(name);
					}
				}
			}
			
			return null;
		}
		catch(Exception e)
		{
			//NO BLOCK FOUND
			return null;
		}	
	}
	
	public List<Poll> getPolls()
	{
		try
		{
			//GET ALL TRANSACTIONS IN MAP
			List<Poll> polls = new ArrayList<Poll>();
			
			for(byte[] rawPoll: this.pollMap.values())
			{
				Poll name = Poll.parse(rawPoll);
				polls.add(name);
			}
			
			if(deleted == null)
			{
				if(this.parent != null)
				{
					polls.addAll(this.parent.getPolls());
					
					//TODO REMOVE DUPLICATES
				}
			}
			
			//RETURN
			return polls;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new ArrayList<Poll>();
		}		
	}
	
	public boolean containsPoll(String name)
	{
		if(name == null)
		{
			return false;
		}
		
		if(this.pollMap.containsKey(name))
		{
			return true;
		}
		else
		{
			if(deleted == null || !deleted.contains(name))
			{
				if(this.parent != null)
				{
					return this.parent.containsPoll(name);
				}
			}
		}
			
		return false;
	}
	
	public boolean containsPoll(Poll poll)
	{
		if(poll == null)
		{
			return false;
		}
		
		if(this.pollMap.containsKey(poll.getName()))
		{
			return true;
		}
		else
		{
			if(deleted == null || !deleted.contains(poll.getName()))
			{
				if(this.parent != null)
				{
					return this.parent.containsPoll(poll);
				}
			}
		}
			
		return false;
	}

	public void addPoll(Poll poll) 
	{
		try
		{
			//ADD NAME INTO DB
			this.pollMap.put(poll.getName(), poll.toBytes());
			
			if(this.deleted != null)
			{
				this.deleted.remove(poll.getName());
			}
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_POLL_TYPE, poll));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace(System.out);
		}	
	}
	
	public void deletePoll(Poll poll) 
	{
		try
		{
			//REMOVE
			if(this.pollMap.containsKey(poll.getName()))
			{
				this.pollMap.remove(poll.getName());
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_POLL_TYPE, poll));
				
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
			}
			
			if(this.deleted != null)
			{
				this.deleted.add(poll.getName());
			}
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			//NO NAME FOUND
		}		
	}
	
	//OBSERVER
	
	@Override
	public void addObserver(Observer o) 
	{
		//ADD OBSERVER
		super.addObserver(o);	
			
		o.update(null, new ObserverMessage(ObserverMessage.LIST_POLL_TYPE, this.getPolls()));
	}
}
