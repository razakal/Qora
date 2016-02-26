package gui.models;

import java.util.Observable;
import java.util.Observer;

import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import qora.naming.Name;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.SortableList;
import database.wallet.NameMap;
import lang.Lang;

@SuppressWarnings("serial")
public class WalletNamesTableModel extends QoraTableModel<Tuple2<String, String>, Name> implements Observer
{
	public static final int COLUMN_NAME = 0;
	public static final int COLUMN_ADDRESS = 1;
	private static final int COLUMN_CONFIRMED = 2;
	
	private SortableList<Tuple2<String, String>, Name> names;
	
	private String[] columnNames = Lang.getInstance().translate(new String[]{"Name", "Owner", "Confirmed"});
	
	public WalletNamesTableModel()
	{
		Controller.getInstance().addWalletListener(this);
	}
	
	@Override
	public SortableList<Tuple2<String, String>, Name> getSortableList() {
		return this.names;
	}
	
	public Name getName(int row)
	{
		Pair<Tuple2<String, String>, Name> namepair = this.names.get(row);
		if(!namepair.getA().a.equalsIgnoreCase(namepair.getB().getOwner().getAddress()))
		{
			//inconsistency, owner was not updated correctly
			Name name = new Name(new Account(namepair.getA().a), namepair.getB().getName(), namepair.getB().getValue());
			return name;
		}
		return this.names.get(row).getB();
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
		 return this.names.size();
	}

	@Override
	public Object getValueAt(int row, int column) 
	{
		if(this.names == null || row > this.names.size() - 1 )
		{
			return null;
		}
		
		Name name = getName(row);
		
		switch(column)
		{
		case COLUMN_NAME:
			
			return name.getName();
		
		case COLUMN_ADDRESS:
			
			return name.getOwner().getAddress();
			
		case COLUMN_CONFIRMED:
			
			return name.isConfirmed();
			
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
		if(message.getType() == ObserverMessage.LIST_NAME_TYPE)
		{
			if(this.names == null)
			{
				this.names = (SortableList<Tuple2<String, String>, Name>) message.getValue();
				this.names.registerObserver();
				this.names.sort(NameMap.NAME_INDEX);
			}
			
			this.fireTableDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_NAME_TYPE || message.getType() == ObserverMessage.REMOVE_NAME_TYPE)
		{
			this.fireTableDataChanged();
		}	
	}
}
