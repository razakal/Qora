package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.account.Account;
import qora.voting.Poll;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class WalletPollsTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_ADDRESS = 1;
	public static final int COLUMN_TOTAL_VOTES = 2;
	private static final int COLUMN_CONFIRMED = 3;
	
	private String[] columnNames = {"Name", "Creator", "Total Votes", "Confirmed"};
	private List<Pair<Account, Poll>> polls;
	
	public WalletPollsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	public Poll getPoll(int row)
	{
		return polls.get(row).getB();
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
		 return this.polls.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.polls == null || row > this.polls.size() - 1 )
		{
			return null;
		}
		
		Poll poll = this.polls.get(row).getB();
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return poll.getName();
		
		case COLUMN_ADDRESS:
			
			return poll.getCreator().getAddress();
			
		case COLUMN_TOTAL_VOTES:
			
			return poll.getTotalVotes().toPlainString();
			
		case COLUMN_CONFIRMED:
			
			return poll.isConfirmed();
			
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
		
		if(message.getType() == ObserverMessage.LIST_POLL_TYPE)
		{			
			this.polls = (List<Pair<Account, Poll>>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}
}
