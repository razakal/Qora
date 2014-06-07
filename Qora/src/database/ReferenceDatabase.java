package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;

import qora.account.Account;

public class ReferenceDatabase {
	
	private ReferenceDatabase parent;
	private DatabaseSet databaseSet;	
	private Map<String, byte[]> referenceMap;
	private List<String> deleted;
	
	public ReferenceDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.referenceMap = database.getTreeMap("references");
	}
	
	public ReferenceDatabase(ReferenceDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.referenceMap = new HashMap<String, byte[]>();
	    this.deleted = new ArrayList<String>();
	}
	
	public List<byte[]> getAll()
	{
		List<byte[]> references= new ArrayList<byte[]>();
		for(String account: this.referenceMap.keySet())
		{
			references.add(this.getReference(new Account(account)));
		}
		
		return references;
	}
	
	public void setReference(Account account, byte[] reference)
	{
		try
		{
			//ADD CHILD INTO DB
			this.referenceMap.put(account.getAddress(), reference);
			
			if(this.deleted != null)
			{
				this.deleted.remove(account.getAddress());
			}
			
			//COMMIT
			if(this.databaseSet != null)
			{
				this.databaseSet.commit();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}			
	}
	
	public byte[] getReference(Account account)
	{
		try
		{
			//GET REFERENCE
			if(this.referenceMap.containsKey(account.getAddress()))
			{
				return this.referenceMap.get(account.getAddress());
			}
			else
			{
				if(deleted == null || !deleted.contains(account.getAddress()))
				{
					if(this.parent != null)
					{
						return this.parent.getReference(account);
					}
				}
			}
			
			return new byte[0];
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			//NO REFERENCE FOUND
			return new byte[0];
		}					
	}
	
	public void remove(Account account)
	{
		//REMOVE
		if(this.referenceMap.containsKey(account.getAddress()))
		{
			this.referenceMap.remove(account.getAddress());
		}
		
		if(this.deleted != null)
		{
			this.deleted.add(account.getAddress());
		}
		
		//COMMIT
		if(this.databaseSet != null)
		{
			this.databaseSet.commit();
		}
	}
}
