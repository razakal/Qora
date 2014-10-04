package gui.models;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.table.AbstractTableModel;

import utils.ObserverMessage;
import controller.Controller;
import network.Peer;

@SuppressWarnings("serial")
public class PeersTableModel extends AbstractTableModel implements Observer{

	private List<Peer> peers;
	
	private String[] columnNames = {"IP"};
	
	public PeersTableModel()
	{
		Controller.getInstance().addActivePeersObserver(this);
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
		if(peers == null)
		{
			return 0;
		}
		
		return peers.size();
	}

	@Override
	public Object getValueAt(int row, int column)
	{
		if(peers == null)
		{
			return null;
		}
		
		return peers.get(row).getAddress().getHostAddress();
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
		
		if(message.getType() == ObserverMessage.LIST_PEER_TYPE)
		{
			this.peers = (List<Peer>) message.getValue();
		
			this.fireTableDataChanged();
		}
	}

	public void removeObservers() 
	{
		Controller.getInstance().removeActivePeersObserver(this);
		
	}
}
