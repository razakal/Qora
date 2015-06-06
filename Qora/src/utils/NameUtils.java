package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import qora.account.Account;
import qora.naming.Name;
import api.ApiErrorFactory;
import controller.Controller;
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
	
	
	public static List<String> getNamesByValue(String searchvalue, boolean removeInjected)
	{
		return getNamesbyValueInternal(removeInjected, searchvalue);

		
		
	}
	
	
	
	
	public static List<String> getNamesContainingWebsites(boolean removeInjected) {
		return getNamesbyValueInternal(removeInjected, null);
	}


	public static List<String> getNamesbyValueInternal(boolean removeInjected, String searchValueOpt) {
		List<String> websites = new ArrayList<String>();
		List<String> injected = new ArrayList<String>();

		NameMap nameMap = DBSet.getInstance().getNameMap();

		Set<String> keys = nameMap.getKeys();
		Pattern pattern = Pattern.compile("(?i)<inj>(.*?)</inj>");

		for (String string : keys) {
			String value = nameMap.get(string).getValue();

			value = GZIP.webDecompress(value);

			// PROCESSING TAG INJ
			Matcher matcher = pattern.matcher(value);
			while (matcher.find()) {
				String group = matcher.group(1);
				Name nameinj = Controller.getInstance().getName(group);
				injected.add(group);
				value = value.replace(matcher.group(),
						GZIP.webDecompress(nameinj.getValue().toString()));
			}

			if(searchValueOpt == null)
			{
				if (value.toLowerCase().contains("html")
						|| value.toLowerCase().contains("iframe")
						|| value.toLowerCase().contains("<a href=")
						|| value.toLowerCase().contains("<script>")
						|| value.toLowerCase().contains("<table>")
						|| value.toLowerCase().contains("<b>")
						|| value.toLowerCase().contains("<font>")
						|| value.toLowerCase().contains("<pre>")) {
					websites.add(string);
				}
			}else
			{
				if (value.matches(".*" + Pattern.quote(searchValueOpt) + ".*"))
				{
					websites.add(string);
				}
			}

		}

		if(removeInjected)
		{
			websites.removeAll(injected);
		}
		
		return websites;
	}
}
