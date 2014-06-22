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
import qora.crypto.Base58;
import qora.crypto.Crypto;
import qora.transaction.Transaction;
import utils.Pair;

@Path("transactions")
@Produces(MediaType.APPLICATION_JSON)
public class TransactionsResource {

	@GET
	public String getTransactions()
	{
		return this.getTransactionsLimited(50);
	}
	
	@GET
	@Path("/{address}")
	public String getTransactions(@PathParam("address") String address)
	{
		return this.getTransactionsLimited(address, 50);
	}
	
	@GET
	@Path("address/{address}")
	public String getTransactionsTwo(@PathParam("address") String address)
	{
		return this.getTransactions(address);
	}
	
	@SuppressWarnings("unchecked")
	@GET
	@Path("limit/{limit}")
	public String getTransactionsLimited(@PathParam("limit") int limit)
	{
		//CHECK IF WALLET EXISTS
		if(!Controller.getInstance().doesWalletExists())
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_WALLET_NO_EXISTS);
		}
		
		//GET TRANSACTIONS
		List<Pair<Account, Transaction>> transactions = Controller.getInstance().getLastTransactions(limit);
		
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
	@Path("address/{address}/limit/{limit}")
	public String getTransactionsLimited(@PathParam("address") String address, @PathParam("limit") int limit)
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
		for(Transaction transaction: Controller.getInstance().getLastTransactions(account, limit))
		{
			array.add(transaction.toJson());
		}
		
		return array.toJSONString();
	}
	
	@GET
	@Path("signature/{signature}")
	public String getTransactionsBySignature(@PathParam("signature") String signature) throws Exception
	{
		//DECODE SIGNATURE
		byte[] signatureBytes;
		try
		{
			signatureBytes = Base58.decode(signature);
		}
		catch(Exception e)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_INVALID_SIGNATURE);
		}
		
		//GET TRANSACTION
		Transaction transaction = Controller.getInstance().getTransaction(signatureBytes);
		
		//CHECK IF TRANSACTION EXISTS
		if(transaction == null)
		{
			throw ApiErrorFactory.getInstance().createError(ApiErrorFactory.ERROR_TRANSACTION_NO_EXISTS);
		}
		
		return transaction.toJson().toJSONString();
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
