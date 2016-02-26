package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.voting.Poll;
import utils.ObserverMessage;
import controller.Controller;
import database.SortableList;
import database.wallet.PollMap;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletPollsTableModel extends QoraTableModel<Tuple2<String, String>, Poll> implements Observer
{
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_ADDRESS = 1;
	public static final int COLUMN_TOTAL_VOTES = 2;
	private static final int COLUMN_CONFIRMED = 3;
	
	private SortableList<Tuple2<String, String>, Poll> polls;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Creator", "Total Votes", "Confirmed"});
	
	public WalletPollsTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Poll> getSortableList() {
		return polls;
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
		
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_POLL_TYPE)
		{
			if(this.polls == null)
			{
				this.polls = (SortableList<Tuple2<String, String>, Poll>) message.getValue();
				this.polls.registerObserver();
				this.polls.sort(PollMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_POLL_TYPE || message.getType() == ObserverMessage.REMOVE_POLL_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
