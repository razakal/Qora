package utils;

import java.util.Comparator;

import qora.transaction.Transaction;

public class TransactionTimestampComparator implements Comparator<Transaction> {
	
	@Override
	public int compare(Transaction one, Transaction two) 
	{
		return (int) (one.getTimestamp() - two.getTimestamp());
	}
}