package utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.naming.Name;
import qora.transaction.Transaction;
import qora.transaction.UpdateNameTransaction;
import webserver.WebResource;
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
	
	
	public static List<Pair<String, String>> getWebsitesByValue(String searchvalue)
	{
		return getWebsitesbyValueInternal(searchvalue);

		
		
	}
	
	
	
	
	public static  List<Pair<String, String>> getNamesContainingWebsites() {
		return getWebsitesbyValueInternal(null);
	}
	
	
	@SuppressWarnings("unchecked")
	public static JSONObject getJsonForNameOpt( Name name)
	{
		
		String rawNameValue = null;
		//CHECK UNCONFIRMED ONLY FOR OWN NAMES
		if(Controller.getInstance().getName(name.getName()) != null)
		{
			List<Transaction> accountTransactions = getOwnUnconfirmedTX();
			
			for (Transaction transaction : accountTransactions) {
				
				if(transaction.getType() == Transaction.UPDATE_NAME_TRANSACTION )
				{
					UpdateNameTransaction updateNameTx =	(UpdateNameTransaction) transaction;
					if(updateNameTx.getName().getName().equals(name.getName()))
					{
						rawNameValue =  updateNameTx.getName().getValue();
						break;
					}
					
				}
			}
		}
		
		
		if(rawNameValue == null)
		{
			rawNameValue = name.getValue();
		}
		
		String decompressedNameValue = GZIP.webDecompress(rawNameValue);
		
		JSONObject jsonValue;
		//THIS SIGNIFICANTLY INCREASES SPEED!
		if(!decompressedNameValue.startsWith("{"))
		{
			jsonValue = new JSONObject();
			jsonValue.put(Qorakeys.DEFAULT.toString(), decompressedNameValue);
			return jsonValue;
		}
		
		try {
			jsonValue = (JSONObject) JSONValue.parse(decompressedNameValue);
			
			if(jsonValue == null)
			{
				jsonValue = new JSONObject();
				jsonValue.put(Qorakeys.DEFAULT.toString(), decompressedNameValue);
			}
			
			return jsonValue;
			
		} catch (Exception e) {
//			no valid json
			
			jsonValue = new JSONObject();
			jsonValue.put(Qorakeys.DEFAULT.toString(), decompressedNameValue);
			return jsonValue;
		}
		
		
		
	}


	public static List<Transaction> getOwnUnconfirmedTX() {
		List<Transaction> transactions = DBSet.getInstance().getTransactionMap().getTransactions();
		List<Transaction> accountTransactions = new ArrayList<Transaction>();
			
		for(Transaction transaction: transactions)
		{
			if(Controller.getInstance().getAccounts().contains(transaction.getCreator()))
			{
				accountTransactions.add(transaction);
			}
		}
			
		//SORT THEM BY TIMESTAMP
		Collections.sort(accountTransactions, new TransactionTimestampComparator());
		return accountTransactions;
	}


	public static List<Pair<String, String>> getWebsitesbyValueInternal( String searchValueOpt) {
		
		
		List<Pair<String, String>> results = new ArrayList<Pair<String,String>>();

		NameMap nameMap = DBSet.getInstance().getNameMap();

		Set<String> keys = nameMap.getKeys();

		for (String key : keys) {
			String value = nameMap.get(key).getValue();

			value = GZIP.webDecompress(value);
			value = WebResource.injectValues(value);

				try {
					if(value.startsWith("{"))
					{
						JSONObject jsonObject = (JSONObject) JSONValue.parse(value);
						if(jsonObject.containsKey(Qorakeys.WEBSITE.getKeyname()))
						{
							String websitevalue = (String) jsonObject.get(Qorakeys.WEBSITE.getKeyname());
							if(websitevalue.contains(Qorakeys.WEBSITE.getKeyname()) && websitevalue.matches("<inj.*key=."+Qorakeys.WEBSITE.getKeyname()+ ".*>"))
							{
//								Another website is injected into this page, to avoid both in the searchengine we don't allow this!
								continue;
							}
							websitevalue =	WebResource.injectValues(websitevalue);
							if(searchValueOpt == null)
							{
								results.add(new Pair<String,String>(key,websitevalue));
							}else
							{
								if(websitevalue.toLowerCase().contains(searchValueOpt.toLowerCase()))
								{
									results.add(new Pair<String,String>(key,websitevalue));
								}
								
							}
						}
					}
				} catch (Exception e) {
					// no valid json no website key
				}

		}

		
		return results;
	}
}
