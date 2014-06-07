package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.naming.NameSale;
import utils.ObserverMessage;
import database.DatabaseSet;

@SuppressWarnings("serial")
public class NameSalesTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_OWNER = 1;
	public static final int COLUMN_PRICE = 2;
	
	private String[] columnNames = {"Name", "Seller", "Price"};
	private List<NameSale> nameSales;
	
	public NameSalesTableModel()
	{
		DatabaseSet.getInstance().getNameExchangeDatabase().addObserver(this);
	}
	
	public NameSale getNameSale(int row)
	{
		return this.nameSales.get(row);
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
		 return this.nameSales.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.nameSales == null || row > this.nameSales.size() - 1 )
		{
			return null;
		}
		
		NameSale nameSale = this.nameSales.get(row);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			String key = nameSale.getKey();
			
			//CHECK IF ENDING ON A SPACE
			if(key.endsWith(" "))
			{
				key = key.substring(0, key.length()-1);
				key += ".";
			}
			
			return key;
		
		case COLUMN_OWNER:
			
			return nameSale.getName().getOwner().getAddress();
			
		case COLUMN_PRICE:
			
			return nameSale.getAmount().toPlainString();
			
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
		
		if(message.getType() == ObserverMessage.LIST_NAME_SALE_TYPE)
		{			
			this.nameSales = (List<NameSale>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		DatabaseSet.getInstance().getNameExchangeDatabase().deleteObserver(this);
	}
}
