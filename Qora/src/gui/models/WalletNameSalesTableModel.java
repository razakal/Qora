package gui.models;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import controller.Controller;
import qora.account.Account;
import qora.naming.NameSale;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class WalletNameSalesTableModel extends AbstractTableModel implements Observer{

	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_OWNER = 1;
	public static final int COLUMN_PRICE = 2;
	
	private String[] columnNames = {"Name", "Seller", "Price"};
	
	private List<Pair<Account, NameSale>> nameSales;
	
	public WalletNameSalesTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	public NameSale getNameSale(int row)
	{
		return nameSales.get(row).getB();
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
		if(nameSales == null)
		{
			return 0;
		}
		
		return nameSales.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(nameSales == null || nameSales.size() - 1 < row)
		{
			return null;
		}
		
		Pair<Account, NameSale> pair = nameSales.get(row);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return pair.getB().getKey();
			
		case COLUMN_OWNER:
			 
			return pair.getA().getAddress();
			
		case COLUMN_PRICE:
			
			return pair.getB().getAmount().toPlainString();
			
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
			this.nameSales = (List<Pair<Account, NameSale>>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		Controller.getInstance().deleteWalletObserver(this);
	}
}
