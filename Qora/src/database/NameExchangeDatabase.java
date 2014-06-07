package database;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.DB;

import qora.naming.NameSale;
import utils.NameSalesList;
import utils.ObserverMessage;

public class NameExchangeDatabase extends Observable {
	
	private NameExchangeDatabase parent;
	private DatabaseSet databaseSet;
	private Map<String, BigDecimal> salesMap;
	private List<String> deleted;
	
	public NameExchangeDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
		this.salesMap = database.getTreeMap("sales");

	}
	
	public NameExchangeDatabase(NameExchangeDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.salesMap = new HashMap<String, BigDecimal>();	    
	    deleted = new ArrayList<String>();
	}
	
	private List<String> getKeys()
	{
		//GET ALL KEYS
		List<String> keyList = new ArrayList<String>();
		
		for(String key: this.salesMap.keySet())
		{
			keyList.add(key);
		}
		
		return keyList;
	}
	
	public List<NameSale> getNameSales()
	{
		try
		{
			//GET ALL TRANSACTIONS IN MAP
			List<String> keyList = this.getKeys();
			
			if(deleted == null)
			{
				if(this.parent != null)
				{
					keyList.addAll(this.parent.getKeys());
					
					//TODO REMOVE DUPLICATES
				}
			}
			
			//RETURN
			return new NameSalesList(keyList);
		}
		catch(Exception e)
		{
			e.printStackTrace();
			return new ArrayList<NameSale>();
		}		
	}
	
	public boolean containsName(String name)
	{
		if(this.salesMap.containsKey(name))
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
	
	public boolean containsNameSale(NameSale nameSale)
	{
		if(this.salesMap.containsKey(nameSale.getKey()))
		{
			return true;
		}
		else
		{
			if(deleted == null || !deleted.contains(nameSale.getKey()))
			{
				if(this.parent != null)
				{
					return this.parent.containsNameSale(nameSale);
				}
			}
		}
			
		return false;
	}

	public void addNameSale(NameSale nameSale) 
	{
		try
		{
			//ADD NAME INTO DB
			this.salesMap.put(nameSale.getKey(), nameSale.getAmount());
			
			if(this.deleted != null)
			{
				this.deleted.remove(nameSale.getKey());
			}
			
			//NOTIFY
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.ADD_NAME_SALE_TYPE, nameSale));
			
			this.setChanged();
			this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
			
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
	
	public void delete(String name)
	{
		try
		{
			//REMOVE
			if(this.salesMap.containsKey(name))
			{
				BigDecimal amount = this.salesMap.remove(name);
				NameSale nameSale = new NameSale(name, amount);
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, nameSale));
				
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
			}						
			
			if(this.deleted != null)
			{
				this.deleted.add(name);
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
	
	public void deleteNameSale(NameSale nameSale) 
	{
		try
		{
			//REMOVE
			if(this.salesMap.containsKey(nameSale.getKey()))
			{
				this.salesMap.remove(nameSale.getKey());
				
				//NOTIFY
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.REMOVE_NAME_SALE_TYPE, nameSale));
				
				this.setChanged();
				this.notifyObservers(new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
			}
			
			if(this.deleted != null)
			{
				this.deleted.add(nameSale.getKey());
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
	
	public NameSale getNameSale(String name)
	{
		try
		{
			//GET CHILD
			if(this.salesMap.containsKey(name))
			{
				BigDecimal amount = this.salesMap.get(name);
				return new NameSale(name, amount);
			}
			else
			{
				if(deleted == null || !deleted.contains(name))
				{
					if(this.parent != null)
					{
						return this.parent.getNameSale(name);
					}
				}
			}
			
			return null;
		}
		catch(Exception e)
		{
			//NO CHILD FOUND
			return null;
		}			
	}
	
	//OBSERVER
	
	@Override
	public void addObserver(Observer o) 
	{
		//ADD OBSERVER
		super.addObserver(o);	
		
		o.update(null, new ObserverMessage(ObserverMessage.LIST_NAME_SALE_TYPE, this.getNameSales()));
	}
	
	
}
