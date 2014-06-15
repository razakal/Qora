package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import qora.voting.Poll;
import utils.ObserverMessage;
import database.DatabaseSet;

@SuppressWarnings("serial")
public class PollsTableModel extends AbstractTableModel implements Observer
{
	private static final int COLUMN_NAME = 0;
	private static final int COLUMN_CREATOR = 1;
	public static final int COLUMN_VOTES = 2;
	
	private String[] columnNames = {"Name", "Creator", "Total Votes"};
	private List<Poll> polls;
	
	public PollsTableModel()
	{
		DatabaseSet.getInstance().getPollDatabase().addObserver(this);
	}
	
	public Poll getPoll(int row)
	{
		return this.polls.get(row);
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
		
		Poll poll = this.polls.get(row);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			String key = poll.getName();
			
			//CHECK IF ENDING ON A SPACE
			if(key.endsWith(" "))
			{
				key = key.substring(0, key.length()-1);
				key += ".";
			}
			
			return key;
		
		case COLUMN_CREATOR:
			
			return poll.getCreator().getAddress();
			
		case COLUMN_VOTES:
			
			return poll.getTotalVotes().toPlainString();
			
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
			this.polls = (List<Poll>) message.getValue();
				
			this.fireTableDataChanged();
		}
	}
	
	public void removeObservers() 
	{
		DatabaseSet.getInstance().getPollDatabase().deleteObserver(this);
	}
}
