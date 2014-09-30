package gui.models;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import qora.assets.Asset;
import qora.assets.Order;
import utils.ObserverMessage;
import database.SortableList;

@SuppressWarnings("serial")
public class BuyOrdersTableModel extends QoraTableModel<BigInteger, Order> implements Observer
{
	public static final int COLUMN_BUYING_PRICE = 0;
	public static final int COLUMN_BUYING_AMOUNT = 1;
	public static final int COLUMN_PRICE = 2;
	public static final int COLUMN_AMOUNT = 3;

	private SortableList<BigInteger, Order> orders;
	
	private String[] columnNames = {"Buying Price", "Buying Amount", "Price", "Amount"};
	
	public BuyOrdersTableModel(Asset have, Asset want)
	{
		Controller.getInstance().addObserver(this);
		this.orders = Controller.getInstance().getOrders(have, want);
		this.orders.registerObserver();
	}
	
	@Override
	public SortableList<BigInteger, Order> getSortableList() 
	{
		return this.orders;
	}
	
	public Order getOrder(int row)
	{
		return this.orders.get(row).getB();
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
		return this.orders.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.orders == null || row > this.orders.size() - 1 )
		{
			return null;
		}
		
		Order order = this.orders.get(row).getB();
		
		switch(column)
		{
		case COLUMN_BUYING_PRICE:
			
			return BigDecimal.ONE.setScale(8).divide(order.getPrice(), 8, RoundingMode.DOWN).toPlainString();
			
		case COLUMN_BUYING_AMOUNT:
			
			return order.getPrice().multiply(order.getAmountLeft()).setScale(8, RoundingMode.DOWN).toPlainString();
		
		case COLUMN_PRICE:
			
			return order.getPrice().toPlainString();
		
		case COLUMN_AMOUNT:
			
			return order.getAmountLeft().toPlainString();
			
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
		ObserverMessage message = (ObserverMessage) arg;
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_ORDER_TYPE || message.getType() == ObserverMessage.REMOVE_ORDER_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.orders.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
