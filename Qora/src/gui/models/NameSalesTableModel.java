package gui.models;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import qora.naming.NameSale;
import utils.NumberAsString;
import utils.ObserverMessage;
import utils.Pair;
import database.DBSet;
import database.SortableList;
import lang.Lang;

@SuppressWarnings("serial")
public class NameSalesTableModel extends QoraTableModel<String, BigDecimal> implements Observer
{
	public static final int COLUMN_NAME = 0;
	private static final int COLUMN_OWNER = 1;
	public static final int COLUMN_PRICE = 2;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Seller", "Price"});
	private SortableList<String, BigDecimal> nameSales;
	
	public NameSalesTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<String, BigDecimal> getSortableList() {
		return this.nameSales;
	}
	
	public NameSale getNameSale(int row)
	{
		Pair<String, BigDecimal> pair = this.nameSales.get(row);
		return new NameSale(pair.getA(), pair.getB());
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
		
		NameSale nameSale = this.getNameSale(row);
		
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
			
			return NumberAsString.getInstance().numberAsString(nameSale.getAmount());
			
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_NAME_SALE_TYPE)
		{			
			if(this.nameSales == null)
			{
				this.nameSales = (SortableList<String, BigDecimal>) message.getValue();
				this.nameSales.registerObserver();
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_NAME_SALE_TYPE || message.getType() == ObserverMessage.REMOVE_NAME_SALE_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.nameSales.removeObserver();
		DBSet.getInstance().getNameExchangeMap().deleteObserver(this);
	}
}
