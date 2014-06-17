package gui.models;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import qora.account.Account;
import qora.voting.PollOption;
import utils.Pair;

@SuppressWarnings("serial")
public class VotesTableModel extends AbstractTableModel
{
	private static final int COLUMN_ADDRESS = 0;
	private static final int COLUMN_OPTION = 1;
	public static final int COLUMN_VOTES = 2;
	
	private String[] columnNames = {"Address", "Option", "Votes"};
	private List<Pair<Account, PollOption>> votes;
	
	public VotesTableModel(List<Pair<Account, PollOption>> votes)
	{
		this.votes = votes;
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
		 return this.votes.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.votes == null || row > this.votes.size() - 1 )
		{
			return null;
		}
		
		Pair<Account, PollOption> vote = this.votes.get(row);
		
		switch(column)
		{
		case COLUMN_ADDRESS:
			
			return vote.getA().getAddress();
		
		case COLUMN_OPTION:
			
			return vote.getB().getName();
			
		case COLUMN_VOTES:
			
			return vote.getA().getConfirmedBalance().toPlainString();
			
		}
		
		return null;
	}
}
