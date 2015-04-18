package utils;

import java.util.Comparator;

import qora.account.Account;


public class AccountBalanceComparator implements Comparator<Account> {

	@Override
	public int compare(Account o1, Account o2) {
		return o1.getBalance(1).compareTo(o2.getBalance(1));
	}

}
