package api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import controller.Controller;
import qora.account.Account;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import utils.Pair;

@Path("transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionsResource {

	@SuppressWarnings("unchecked")
	@GET
	public String getTransactions()
	{
		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		//GET TRANSACTIONS
		List<Pair<Account, Transaction>> transactions = Controller.getInstance().getLastTransactions();
		
		//ORGANIZE TRANSACTIONS
		Map<Account, List<Transaction>> orderedTransactions = new HashMap<Account, List<Transaction>>();
		for(Pair<Account, Transaction> transaction: transactions)
		{
			if(!orderedTransactions.containsKey(transaction.getA()))
			{
				orderedTransactions.put(transaction.getA(), new ArrayList<Transaction>());
			}
			
			orderedTransactions.get(transaction.getA()).add(transaction.getB());
		}
		
		//CREATE JSON OBJECT
		JSONArray orderedTransactionsJSON = new JSONArray();
		
		for(Account account: orderedTransactions.keySet())
		{
			JSONArray transactionsJSON = new JSONArray();
			for(Transaction transaction: orderedTransactions.get(account))
			{
				transactionsJSON.add(transaction.toJson());
			}
			
			JSONObject accountTransactionsJSON = new JSONObject();
			accountTransactionsJSON.put("account", account.getAddress());
			accountTransactionsJSON.put("transactions", transactionsJSON);
			orderedTransactionsJSON.add(accountTransactionsJSON);		
		}
		
		return orderedTransactionsJSON.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/{address}")
	public String getTransactions(@PathParam("address") String address)
	{
		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		//CHECK ADDRESS
		if(!Crypto.getInstance().isValidAddress(address))
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_ADDRESS);
		}
		
		//CHECK ACCOUNT IN WALLET
		Account account = Controller.getInstance().getAccountByAddress(address);	
		if(account == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_ADDRESS_NO_EXISTS);
		}
		
		JSONArray array = new JSONArray();
		for(Transaction transaction: Controller.getInstance().getLastTransactions(account))
		{
			array.add(transaction.toJson());
		}
		
		return array.toJSONString();
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("/network")
	public String getNetworkTransactions()
	{
		List<Transaction> transactions = Controller.getInstance().getUnconfirmedTransactions();
		JSONArray array = new JSONArray();
		
		for(Transaction transaction: transactions)
		{
			array.add(transaction.toJson());
		}
		
		return array.toJSONString();
	}
}
