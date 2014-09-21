package gui.models;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import controller.Controller;
import qora.assets.Order;
import qora.assets.Trade;
import utils.ObserverMessage;
import database.DBSet;
import database.SortableList;

@SuppressWarnings("serial")
public class OrderTradesTableModel extends QoraTableModel<Tuple2<BigInteger, BigInteger>, Trade> implements Observer
{
	public static final int COLUMN_TIMESTAMP = 0;
	public static final int COLUMN_TYPE = 1;
	public static final int COLUMN_PRICE = 2;
	public static final int COLUMN_AMOUNT = 3;
	public static final int COLUMN_TOTAL = 4;

	private SortableList<Tuple2<BigInteger, BigInteger>, Trade> trades;
	private Order order;
	
	private String[] columnNames = {"Timestamp", "Type", "Price", "Amount", "Total"};
	
	public OrderTradesTableModel(Order order)
	{
		this.order = order;
		this.trades = Controller.getInstance().getTrades(order);
		this.trades.registerObserver();
	}
	
	@Override
	public SortableList<Tuple2<BigInteger, BigInteger>, Trade> getSortableList() 
	{
		return this.trades;
	}
	
	public Trade getTrade(int row)
	{
		return this.trades.get(row).getB();
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
		return this.trades.size();
		
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.trades == null || row > this.trades.size() - 1 )
		{
			return null;
		}
		
		Trade trade = this.trades.get(row).getB();
		
		switch(column)
		{
		case COLUMN_TIMESTAMP:
			
			Date date = new Date(trade.getTimestamp());
			DateFormat format = DateFormat.getDateTimeInstance();
			return format.format(date);
			
		case COLUMN_TYPE:
			
			return trade.getInitiatorOrder(DBSet.getInstance()).getHave() == this.order.getHave() ? "Buy" : "Sell";
		
		case COLUMN_PRICE:
			
			return trade.getPrice().divide(trade.getAmount(), RoundingMode.FLOOR).setScale(8).toPlainString();
		
		case COLUMN_AMOUNT:
			
			return trade.getAmount().toPlainString();
			
		case COLUMN_TOTAL:
			
			return trade.getPrice().toPlainString();
			
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
		if(message.getType() == ObserverMessage.ADD_TRADE_TYPE || message.getType() == ObserverMessage.REMOVE_TRADE_TYPE)
		{
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		this.trades.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
