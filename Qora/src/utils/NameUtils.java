package utils;

import api.ApiErrorFactory;
import qora.account.Account;
import qora.naming.Name;
import database.DBSet;
import database.NameMap;

public class NameUtils {

	public enum NameResult
	{
		OK("OK", "OK", 0), 
		NAME_NOT_REGISTERED("The name is not registered", 
				"Invalid address or name not registered!", 
				ApiErrorFactory.ERROR_NAME_NOT_REGISTERED), 
		NAME_WITH_SPACE("For security purposes sending payments to a name that starts or ends with spaces is forbidden.", 
				"Name Payments with trailing or leading spaces are not allowed!",
				ApiErrorFactory.ERROR_NAME_WITH_SPACE), 
		NAME_FOR_SALE("For security purposes sending payments to a name that can be purchased through name exchange is disabled.", 
				"Payments with names that are for sale are not allowed!",
				ApiErrorFactory.ERROR_NAME_FOR_SALE);
		
		private String statusMessage;
		private String shortStatusMessage;
		private int errorcode;

		private NameResult(String statusMessage, String shortStatusMessage, int errorcode)
		{
			this.statusMessage = statusMessage;
			this.shortStatusMessage = shortStatusMessage;
			this.errorcode = errorcode;
			
		}

		public String getStatusMessage() {
			return statusMessage;
		}
		
		public String getShortStatusMessage() {
			return shortStatusMessage;
		}
		
		public int getErrorCode() {
			return errorcode;
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
