package gui.models;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import controller.Controller;
import qora.block.Block;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class BlocksTableModel extends AbstractTableModel implements Observer{

	public static final int COLUMN_HEIGHT = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_GENERATOR = 2;
	public static final int COLUMN_BASETARGET = 3;
	public static final int COLUMN_TRANSACTIONS = 4;
	public static final int COLUMN_FEE = 5;
	
	private List<Block> blocks;
	
	private String[] columnNames = {"Height", "Timestamp", "Generator", "Generating Balance", "Transactions", "Fee"};
	
	public BlocksTableModel()
	{
		Controller.getInstance().addObserver(this);
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
		if(blocks == null)
		{
			return 0;
		}
		
		return blocks.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(blocks == null || blocks.size() - 1 < row)
		{
			return null;
		}
		
		Block block = blocks.get(row);
		
		switch(column)
		{
		case COLUMN_HEIGHT:
			
			return block.getHeight();
			
		case COLUMN_TIMESTAMP:
			
			Date date = new Date(block.getTimestamp());
			DateFormat format = DateFormat.getDateTimeInstance();
			return format.format(date);
			
		case COLUMN_GENERATOR:
			
			return block.getGenerator().getAddress();
			
		case COLUMN_BASETARGET:
			
			return block.getGeneratingBalance();
			
		case COLUMN_TRANSACTIONS:
			
			return block.getTransactionCount();
			
		case COLUMN_FEE:	
			
			return block.getTotalFee().toPlainString();
			
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
		
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{			
			this.blocks = (List<Block>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}

	public void removeObservers() 
	{
		Controller.getInstance().deleteObserver(this);
	}
}
