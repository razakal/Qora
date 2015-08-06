package webserver.wrapper;

import java.math.BigDecimal;

import qora.account.Account;

/**
 * Web respresentation of an Account for read only usage with pebble
 * @author Skerberus
 *
 */
public class WebAccount {
	
	private final String address;
	private final BigDecimal balance;

	public WebAccount(Account account) {
		
		address = account.getAddress();
		balance = account.getBalance(0);
		// TODO Auto-generated constructor stub
	}

	public BigDecimal getUnconfirmedBalance() {
		return balance;
	}

	public String getAddress() {
		return address;
	}

}
