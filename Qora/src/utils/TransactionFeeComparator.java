package utils;

import java.util.Comparator;

import qora.transaction.Transaction;

public class TransactionFeeComparator implements Comparator<Transaction> {
	
	@Override
	public int compare(Transaction one, Transaction two) 
	{
		return two.feePerByte().compareTo(one.feePerByte());
	}
}