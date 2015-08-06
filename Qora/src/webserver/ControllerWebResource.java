package webserver;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import controller.Controller;
import qora.account.Account;
import qora.naming.Name;
import webserver.wrapper.WebAccount;
import webserver.wrapper.WebName;

/**
 * Used to get wallet specific information using pebble (read only)
 * @author Skerberus
 *
 */
public class ControllerWebResource {

private static ControllerWebResource instance = new ControllerWebResource();
	
	public static ControllerWebResource getInstance()
	{
		if ( instance == null )
		{
			instance = new ControllerWebResource();
		}
		return instance;
	}
	
	
	// we need to use string because of pebble here instead of boolean
	public List<WebName> getNames(String removeZeroBalance)
	{
		List<WebName> results = new ArrayList<>();
		List<Name> myNames = new ArrayList<Name>(Controller.getInstance()
				.getNamesAsList());
		for (Name name : myNames) {
			if(Boolean.valueOf(removeZeroBalance))
			{
				if (name.getOwner().getBalance(0).compareTo(BigDecimal.ZERO) > 0) {
					results.add(new WebName(name));
				}
				
			}else
			{
				results.add(new WebName(name));
			}
		}
		return results;
	}
	
	
	public List<WebAccount> getAccounts(boolean removeZeroBalance)
	{
		List<WebAccount> results = new ArrayList<>();
		
		if (Controller.getInstance().doesWalletDatabaseExists()) {
			ArrayList<Account> realAccs = new ArrayList<Account>(Controller.getInstance()
					.getAccounts());
			
			for (Account account : realAccs) {
				if(removeZeroBalance)
				{
					if (account.getBalance(0).compareTo(BigDecimal.ZERO) > 0) {
						results.add(new WebAccount(account));
					}
				}else
				{
					results.add(new WebAccount(account));
				}
			}
			
		} 
		
		return results;
		
	}
	
}
