package database;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.mapdb.DB;

import qora.naming.Name;

public class NameDatabase {
	
	private NameDatabase parent;
	private DatabaseSet databaseSet;
	private Map<String, byte[]> namesMap;
	private List<String> deleted;
	
	public NameDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.namesMap = database.getTreeMap("names");
	}
	
	public NameDatabase(NameDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.namesMap = new HashMap<String, byte[]>();
	    this.deleted = new ArrayList<String>();
	}
	
	public Name getName(String nameName)
	{
		try
		{
			if(this.namesMap.containsKey(nameName))
			{
				return Name.Parse(this.namesMap.get(nameName));
			}
			else
			{
				if(deleted == null || !deleted.contains(nameName))
				{
					if(this.parent != null)
					{
						return this.parent.getName(nameName);
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
	
	public List<Name> getNames()
	{
		try
		{
			//GET ALL TRANSACTIONS IN MAP
			List<Name> names = new ArrayList<Name>();
			
			for(byte[] rawName: this.namesMap.values())
			{
				Name name = Name.Parse(rawName);
				names.add(name);
			}
			
			if(deleted == null)
			{
				if(this.parent != null)
				{
					names.addAll(this.parent.getNames());
					
					//TODO REMOVE DUPLICATES
				}
			}
			
			//RETURN
			return names;
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new ArrayList<Name>();
		}		
	}
	
	public boolean containsName(String name)
	{
		if(name == null)
		{
			return false;
		}
		
		if(this.namesMap.containsKey(name))
		{
			return true;
		}
		else
		{
			if(deleted == null || !deleted.contains(name))
			{
				if(this.parent != null)
				{
					return this.parent.containsName(name);
				}
			}
		}
			
		return false;
	}
	
	public boolean containsName(Name name)
	{
		if(name == null)
		{
			return false;
		}
		
		if(this.namesMap.containsKey(name.getName()))
		{
			return true;
		}
		else
		{
			if(deleted == null || !deleted.contains(name.getName()))
			{
				if(this.parent != null)
				{
					return this.parent.containsName(name);
				}
			}
		}
			
		return false;
	}

	public void addName(Name name) 
	{
		try
		{
			//ADD NAME INTO DB
			this.namesMap.put(name.getName(), name.toBytes());
			
			if(this.deleted != null)
			{
				this.deleted.remove(name.getName());
			}
			
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
	
	public void deleteName(Name name) 
	{
		try
		{
			//REMOVE
			if(this.namesMap.containsKey(name.getName()))
			{
				this.namesMap.remove(name.getName());
			}
			
			if(this.deleted != null)
			{
				this.deleted.add(name.getName());
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
}
