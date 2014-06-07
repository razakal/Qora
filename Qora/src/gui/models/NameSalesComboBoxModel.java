package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import qora.account.Account;
import qora.naming.NameSale;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class NameSalesComboBoxModel extends DefaultComboBoxModel<NameSale> implements Observer {

	public NameSalesComboBoxModel()
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
		
		if(message.getType() == ObserverMessage.LIST_NAME_SALE_TYPE)
		{
			//GET SELECTED ITEM
			NameSale selected = (NameSale) this.getSelectedItem();
						
			//EMPTY LIST
			this.removeAllElements();
				
			//INSERT ALL ACCOUNTS
			List<Pair<Account, NameSale>> nameSales =  (List<Pair<Account, NameSale>>) message.getValue();
			for(Pair<Account, NameSale> nameSale: nameSales)
			{
				this.addElement(nameSale.getB());
			}
				
			//RESET SELECTED ITEM
			if(this.getIndexOf(selected) != -1)
			{
				this.setSelectedItem(selected);
			}
		}
	}
}
