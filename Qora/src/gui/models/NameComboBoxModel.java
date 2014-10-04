package gui.models;

import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import org.mapdb.Fun.Tuple2;

import qora.naming.Name;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.SortableList;
import database.wallet.NameMap;

@SuppressWarnings("serial")
public class NameComboBoxModel extends DefaultComboBoxModel<Name> implements Observer {

	private SortableList<Tuple2<String, String>, Name> names;
	
	public NameComboBoxModel()
	{
		Controller.getInstance().addWalletListener(this);
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
			
			this.onDataChanged();
		}
		
		//CHECK IF LIST UPDATED
		if(message.getType() == ObserverMessage.ADD_NAME_TYPE || message.getType() == ObserverMessage.REMOVE_NAME_TYPE)
		{
			this.onDataChanged();
		}	
		
		/*9ObserverMessage message = (ObserverMessage) arg;
		
		if(message.getType() == ObserverMessage.LIST_NAME_TYPE)
		{
			//GET SELECTED ITEM
			Name selected = (Name) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			List<Pair<Account, Name>> names =  (List<Pair<Account, Name>>) message.getValue();
			for(Pair<Account, Name> name: names)
			{
				this.addElement(name.getB());
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				this.setSelectedItem(selected);
			}
		}*/
	}
	
	public void onDataChanged()
	{
		//GET SELECTED ITEM
		Name selected = (Name) this.getSelectedItem();
					
		//EMPTY LIST
		this.removeAllElements();
			
		//INSERT ALL ACCOUNTS
		for(Pair<Tuple2<String, String>, Name> name: this.names)
		{
			this.addElement(name.getB());
		}
			
		//RESET SELECTED ITEM
		if(this.getIndexOf(selected) != -1)
		{
			this.setSelectedItem(selected);
		}
	}
}
