package gui.models;

import java.math.BigDecimal;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.account.Account;
import controller.Controller;

@SuppressWarnings("serial")
public class AccountsTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_ADDRESS = 0;
	public static final int COLUMN_BALANCE = 1;
	public static final int COLUMN_CONFIRMED_BALANCE = 2;
	public static final int COLUMN_GENERATING_BALANCE = 3;
	
	private String[] columnNames = {"Address", "Balance", "Confirmed Balance", "Generating Balance"};
	private List<Account> accounts;
	
	public AccountsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	public Account getAccount(int row)
	{
		return accounts.get(row);
	}
	
	@Override
	public int getColumnCount() 
	{
		return columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		 return this.accounts.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.accounts == null || row > this.accounts.size() - 1 )
		{
			return null;
		}
		
		Account account = this.accounts.get(row);
		
		switch(column)
		{
		case COLUMN_ADDRESS:
			
			return account.getAddress();
			
		case COLUMN_BALANCE:
			
			return account.getBalance(0).toPlainString();
			
		case COLUMN_CONFIRMED_BALANCE:
			
			return account.getConfirmedBalance().toPlainString();	
			
		case COLUMN_GENERATING_BALANCE:
			
			return account.getGeneratingBalance().toPlainString();	
			
		}
		
		return null;
	}

	@Override
	public void update(Observable o, Object arg) 
	{
		try
		{
			this.syncUpdate(o, arg);
		}
		catch(Exception e)
		{
			//GUI ERROR
		}
	}
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		this.accounts = Controller.getInstance().getAccounts();
		this.fireTableDataChanged();	
	}

	public BigDecimal getTotalBalance() 
	{
		BigDecimal totalBalance = BigDecimal.ZERO.setScale(8);
		
		for(Account account: this.accounts)
		{
			totalBalance = totalBalance.add(account.getConfirmedBalance());
		}
		
		return totalBalance;
	}
}
