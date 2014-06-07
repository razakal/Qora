package gui.models;

import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import qora.account.Account;
import controller.Controller;

@SuppressWarnings("serial")
public class AccountsComboBoxModel extends DefaultComboBoxModel<Account> implements Observer {

	public AccountsComboBoxModel()
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
	
	public synchronized void syncUpdate(Observable o, Object arg)
	{
		//GET SELECTED ITEM
		Account selected = (Account) this.getSelectedItem();
					
		//EMPTY LIST
		this.removeAllElements();
			
		//INSERT ALL ACCOUNTS
		List<Account> accounts = Controller.getInstance().getAccounts();
		synchronized(accounts)
		{
	 		for(Account account: Controller.getInstance().getAccounts())
			{
				this.addElement(account);
			}
		}
			
		//RESET SELECTED ITEM
		if(this.getIndexOf(selected) != -1)
		{
			this.setSelectedItem(selected);
		}
	}
}
