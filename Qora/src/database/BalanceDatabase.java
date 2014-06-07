package database;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.mapdb.DB;

public class BalanceDatabase {
	
	private BalanceDatabase parent;
	private DatabaseSet databaseSet;
	private Map<String, BigDecimal> balanceMap;
	
	public BalanceDatabase(DatabaseSet databaseSet, DB database)
	{
		this.databaseSet = databaseSet;
		
		//OPEN MAP
	    this.balanceMap = database.getTreeMap("balances");
	}
	
	public BalanceDatabase(BalanceDatabase parent)
	{
		this.parent = parent;
	    
	    //OPEN MAP
	    this.balanceMap = new HashMap<String, BigDecimal>();
	}
	
	public void setBalance(String address, BigDecimal amount)
	{
		try
		{
			//SET BALANCE OF ADDRESS
			this.balanceMap.put(address, amount);
			
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
	
	public BigDecimal getBalance(String address)
	{
		try
		{
			if(this.balanceMap.containsKey(address))
			{
				//GET BALANCE OF ADDRESS
				return this.balanceMap.get(address);
			}
			else
			{
				if(this.parent != null)
				{
					return this.parent.getBalance(address);
				}
			}
			
			return BigDecimal.ZERO.setScale(8);
		}
		catch(Exception e)
		{
			//ACCOUNT NOT KNOWN SO BALANCE IS 0
			return BigDecimal.ZERO.setScale(8);
		}			
	}
}
