package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.account.Account;
import qora.naming.Name;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class NamingServiceTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_ADDRESS = 1;
	private static final int COLUMN_CONFIRMED = 2;
	
	private String[] columnNames = {"Name", "Owner", "Confirmed"};
	private List<Pair<Account, Name>> names;
	
	public NamingServiceTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	public Name getName(int row)
	{
		return names.get(row).getB();
	}
	
	@Override
	public int getColumnCount() 
	{
		return this.columnNames.length;
	}
	
	@Override
	public String getColumnName(int index) 
	{
		return this.columnNames[index];
	}

	@Override
	public int getRowCount() 
	{
		 return this.names.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.names == null || row > this.names.size() - 1 )
		{
			return null;
		}
		
		Name name = this.names.get(row).getB();
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return name.getName();
		
		case COLUMN_ADDRESS:
			
			return name.getOwner().getAddress();
			
		case COLUMN_CONFIRMED:
			
			return name.isConfirmed();
			
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
		
		if(message.getType() == ObserverMessage.LIST_NAME_TYPE)
		{			
			this.names = (List<Pair<Account, Name>>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}
}
