package gui.models;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import controller.Controller;
import qora.transaction.Transaction;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class TransactionsTableModel extends AbstractTableModel implements Observer {
	
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_FEE = 2;
	
	private List<Transaction> transactions;
	
	private String[] columnNames = {"Timestamp", "Type", "Fee"};
	private String[] transactionTypes = {"", "Genesis", "Payment", "Name Registration", "Name Update", "Name Sale", "Cancel Name Sale", "Name Purchase"};

	public TransactionsTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	public Transaction getTransaction(int row)
	{
		return transactions.get(row);
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
		
		Transaction transaction = this.transactions.get(row);
		
		switch(column)
		{
		case COLUMN_TIMESTAMP:
			
			Date date = new Date(transaction.getTimestamp());
			DateFormat format = DateFormat.getDateTimeInstance();
			return format.format(date);
			
		case COLUMN_TYPE:
			
			return this.transactionTypes[transaction.getType()];
			
		case COLUMN_FEE:
			
			return transaction.getFee().toPlainString();		
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
			this.transactions = (List<Transaction>) message.getValue();
			this.fireTableDataChanged();		
		}	
	}

	public void removeObservers() 
	{
		Controller.getInstance().deleteObserver(this);		
	}
	
}
