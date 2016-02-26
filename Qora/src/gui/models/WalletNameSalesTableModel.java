package gui.models;
import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import database.SortableList;
import database.wallet.NameSaleMap;
import lang.Lang;
import qora.account.Account;
import qora.naming.NameSale;
import utils.ObserverMessage;
import utils.Pair;

@SuppressWarnings("serial")
public class WalletNameSalesTableModel extends QoraTableModel<Tuple2<String, String>, BigDecimal> implements Observer{

	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_OWNER = 1;
	public static final int COLUMN_PRICE = 2;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Seller", "Price"});
	
	private SortableList<Tuple2<String, String>, BigDecimal> nameSales;
	
	public WalletNameSalesTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, BigDecimal> getSortableList() {
		return this.nameSales;
	}
	
	public NameSale getNameSale(int row)
	{
		Pair<Tuple2<String, String>, BigDecimal> data = this.nameSales.get(row);
		return new NameSale(data.getA().b, data.getB());
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
		
		Pair<Tuple2<String, String>, BigDecimal> data = this.nameSales.get(row);
		NameSale nameSale = new NameSale(data.getA().b, data.getB());
		Account account = new Account(data.getA().a);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return nameSale.getKey();
			
		case COLUMN_OWNER:
			 
			return account.getAddress();
			
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_NAME_SALE_TYPE)
		{
			if(this.nameSales == null)
			{
				this.nameSales = (SortableList<Tuple2<String, String>, BigDecimal>) message.getValue();
				this.nameSales.registerObserver();
				this.nameSales.sort(NameSaleMap.NAME_INDEX);
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
		Controller.getInstance().deleteWalletObserver(this);
	}
}
