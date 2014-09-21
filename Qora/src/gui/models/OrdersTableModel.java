package gui.models;

import java.math.BigInteger;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import qora.assets.Asset;
import qora.assets.Order;
import utils.ObserverMessage;
import database.SortableList;

@SuppressWarnings("serial")
public class OrdersTableModel extends QoraTableModel<BigInteger, Order> implements Observer
{
	public static final int COLUMN_PRICE = 0;
	public static final int COLUMN_AMOUNT = 1;

	private SortableList<BigInteger, Order> orders;
	
	private String[] columnNames = {"Price", "Amount"};
	
	public OrdersTableModel(Asset have, Asset want)
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
