package database.wallet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;

import org.mapdb.DB;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;
import com.google.common.primitives.UnsignedBytes;

import qora.account.Account;
import qora.transaction.Transaction;
import qora.transaction.TransactionFactory;
import utils.Pair;

public class TransactionsDatabase {

	private static final String TRANSACTIONS = "_transactions";

	private DB database;
	
	public TransactionsDatabase(WalletDatabase walletDatabase, DB database) 
	{
		this.database = database;
	}
	
	public List<Transaction> getLastTransactions(Account account)
	{
		List<Transaction> transactions = new ArrayList<Transaction>();
		
		try
		{
			//OPEN MAP 
			NavigableSet<byte[]> transactionsSet = this.database.createTreeSet(account.getAddress() + TRANSACTIONS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
			Iterator<byte[]> iterator = transactionsSet.descendingIterator();
			
			for(int i=0; i<50 && iterator.hasNext(); i++)
			{
				//GET TRANSACTION
				byte[] bytes = iterator.next();
				byte[] rawTransaction = Arrays.copyOfRange(bytes, Transaction.TIMESTAMP_LENGTH, bytes.length);
				Transaction transaction = TransactionFactory.getInstance().parse(rawTransaction);
				
				//ADD TO LIST
				transactions.add(transaction);
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return transactions;
	}
	
	public List<Pair<Account, Transaction>> getLastTransactions(List<Account> accounts)
	{
		List<Pair<Account, Transaction>> transactions = new ArrayList<Pair<Account, Transaction>>();
		
		try
		{
			//FOR EACH ACCOUNTS
			synchronized(accounts)
			{
				for(Account account: accounts)
				{
					//OPEN MAP 
					NavigableSet<byte[]> transactionsSet = this.database.createTreeSet(account.getAddress() + TRANSACTIONS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
					Iterator<byte[]> iterator = transactionsSet.descendingIterator();
					
					for(int i=0; i<50 && iterator.hasNext(); i++)
					{
						//GET TRANSACTION
						byte[] bytes = iterator.next();
						byte[] rawTransaction = Arrays.copyOfRange(bytes, Transaction.TIMESTAMP_LENGTH, bytes.length);
						Transaction transaction = TransactionFactory.getInstance().parse(rawTransaction);
						
						//ADD TO LIST
						transactions.add(new Pair<Account, Transaction>(account, transaction));
					}
				}
			}
		}
		catch(Exception e)
		{
			//ERROR
			e.printStackTrace();
		}
		
		return transactions;
	}
	
	public void delete(Account account)
	{
		this.database.delete(account.getAddress() + TRANSACTIONS);
	}
	
	public void deleteAll(List<Account> accounts)
	{
		for(Account account: accounts)
		{
			this.delete(account);
		}
	}
	
	public boolean add(Account account, Transaction transaction)
	{
		NavigableSet<byte[]> transactionsSet = this.database.createTreeSet(account.getAddress() + TRANSACTIONS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
		
		byte[] rawTransaction = transaction.toBytes();
		byte[] timestampBytes = Longs.toByteArray(transaction.getTimestamp());
		byte[] bytes = Bytes.concat(timestampBytes, rawTransaction);
		
		boolean added = transactionsSet.add(bytes);
		
		return added;
	}
	
	public void addAll(Map<Account, List<Transaction>> transactions)
	{
		//FOR EACH ACCOUNT
	    for(Account account: transactions.keySet())
	    {
	    	NavigableSet<byte[]> transactionsMap = this.database.createTreeSet(account.getAddress() + TRANSACTIONS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
	    	
	    	//FOR EACH TRANSACTION
	    	for(Transaction transaction: transactions.get(account))
	    	{
	    		byte[] rawTransaction = transaction.toBytes();
	    		byte[] timestampBytes = Longs.toByteArray(transaction.getTimestamp());
	    		byte[] bytes = Bytes.concat(timestampBytes, rawTransaction);   		
	    		transactionsMap.add(bytes);
	    	}
	    }
	}
	

	public void delete(Account account, Transaction transaction) 
	{
		byte[] timestampBytes = Longs.toByteArray(transaction.getTimestamp());
		byte[] remove = Bytes.concat(timestampBytes, transaction.toBytes());   		
		
		//OPEN MAP
		NavigableSet<byte[]> transactionsMap = this.database.createTreeSet(account.getAddress() + TRANSACTIONS).comparator(UnsignedBytes.lexicographicalComparator()).makeOrGet();
    	
		//DELETE
		transactionsMap.remove(remove);
	}
	
}
