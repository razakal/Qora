package gui.models;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import controller.Controller;
import qora.account.Account;
import qora.transaction.Transaction;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class WalletTransactionsTableModel extends AbstractTableModel implements Observer {
	
	public static final int COLUMN_CONFIRMATIONS = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_TYPE = 2;
	public static final int COLUMN_ADDRESS = 3;
	public static final int COLUMN_AMOUNT = 4;
	
	private List<Pair<Account, Transaction>> transactions;
	
	private String[] columnNames = {"Confirmations", "Timestamp", "Type", "Address", "Amount"};
	private String[] transactionTypes = {"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name purchase", "Poll Creation", "Poll Vote"};

	public WalletTransactionsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	public Transaction getTransaction(int row)
	{
		return transactions.get(row).getB();
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
		if(this.transactions == null)
		{
			return 0;
		}
		
		return this.transactions.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.transactions == null || this.transactions.size() -1 < row)
		{
			return null;
		}
		
		Pair<Account, Transaction> transaction = this.transactions.get(row);
		
		switch(column)
		{
		case COLUMN_CONFIRMATIONS:
			
			return transaction.getB().getConfirmations();
			
		case COLUMN_TIMESTAMP:
			
			Date date = new Date(transaction.getB().getTimestamp());
			DateFormat format = DateFormat.getDateTimeInstance();
			return format.format(date);
			
		case COLUMN_TYPE:
			
			return this.transactionTypes[transaction.getB().getType()];
			
		case COLUMN_ADDRESS:
			
			return transaction.getA().getAddress();
			
		case COLUMN_AMOUNT:
			
			return transaction.getB().getAmount(transaction.getA()).toPlainString();			
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
	
	@SuppressWarnings("unchecked")
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE)
		{
			this.transactions = (List<Pair<Account, Transaction>>) message.getValue();
			this.fireTableDataChanged();		
		}	
	}
}
