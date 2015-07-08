package utils;

import java.util.Comparator;

import qora.transaction.Transaction;

public class TransactionTimestampComparator implements Comparator<Transaction> {
	
	@Override
	public int compare(Transaction one, Transaction two) 
	{
		if(one.getTimestamp() < two.getTimestamp())
			return -1;
		else if(one.getTimestamp() > two.getTimestamp()) 
			return 1;
		else
			return 0;
	}
}