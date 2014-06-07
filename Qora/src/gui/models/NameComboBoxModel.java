package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import qora.account.Account;
import qora.naming.Name;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class NameComboBoxModel extends DefaultComboBoxModel<Name> implements Observer {

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
		}
	}
}
