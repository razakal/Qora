package utils;

import qora.account.Account;
import qora.naming.Name;
import database.DBSet;
import database.NameMap;

public class NameUtils {

	public enum NameResult
	{
		OK("OK"), 
		NAME_NOT_REGISTERED("The name is not registered"), 
		NAME_WITH_SPACE("For security purposes sending payments to a name that starts or ends with spaces is forbidden."), 
		NAME_FOR_SALE("For security purposes sending payments to a name that can be purchased through name exchange is disabled.");
		
		private String statusMessage;

		private NameResult(String statusMessage)
		{
			this.statusMessage = statusMessage;
			
		}

		public String getStatusMessage() {
			return statusMessage;
		}

	}
	
	public static Pair<Account, NameResult> nameToAdress(String name)
	{
		NameMap names = DBSet.getInstance().getNameMap();
		//NAME NOT REGISTERED?
		if( !names.contains(name))
		{
			
			return new Pair<Account, NameUtils.NameResult>(null, NameResult.NAME_NOT_REGISTERED);
			
		}
			
			//NAME STARTS OR ENDS WITH SPACE?
			if(name.startsWith(" ") || name.endsWith(" "))
			{
				return new Pair<Account, NameUtils.NameResult>(null, NameResult.NAME_WITH_SPACE);
			}
			
			//NAME FOR SALE?
			if(	DBSet.getInstance().getNameExchangeMap().contains(name))
			{
				return new Pair<Account, NameUtils.NameResult>(null, NameResult.NAME_FOR_SALE);
			}
			
			
			//LOOKUP ADDRESS FOR NAME
			Name lookupName = names.get(name);
			String recipientAddress = lookupName.getOwner().getAddress();
			Account recipient = new Account(recipientAddress);
			return new Pair<Account, NameUtils.NameResult>(recipient, NameResult.OK);
	}
}
