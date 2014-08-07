package gui.models;
import java.text.DateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import controller.Controller;
import database.BlockMap;
import database.SortableList;
import qora.block.Block;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class BlocksTableModel extends QoraTableModel<byte[], Block> implements Observer{

	public static final int COLUMN_HEIGHT = 0;
	public static final int COLUMN_TIMESTAMP = 1;
	public static final int COLUMN_GENERATOR = 2;
	public static final int COLUMN_BASETARGET = 3;
	public static final int COLUMN_TRANSACTIONS = 4;
	public static final int COLUMN_FEE = 5;
	
	private SortableList<byte[], Block> blocks;
	
	private String[] columnNames = {"Height", "Timestamp", "Generator", "Generating Balance", "Transactions", "Fee"};
	
	public BlocksTableModel()
	{
		Controller.getInstance().addObserver(this);
	}
	
	@Override
	public SortableList<byte[], Block> getSortableList() {
		return this.blocks;
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
		
		Block block = blocks.get(row).getB();
		
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{			
			if(this.blocks == null)
			{
				this.blocks = (SortableList<byte[], Block>) message.getValue();
				this.blocks.registerObserver();
				this.blocks.sort(BlockMap.HEIGHT_INDEX, true);
			}	
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE)
		{
			this.fireTableDataChanged();
		}
	}

	public void removeObservers() 
	{
		this.blocks.removeObserver();
		Controller.getInstance().deleteObserver(this);
	}
}
