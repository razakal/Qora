package gui.models;

import java.math.BigDecimal;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultComboBoxModel;

import org.mapdb.Fun.Tuple2;

import qora.account.Account;
import utils.ObserverMessage;
import utils.Pair;
import controller.Controller;
import database.SortableList;

@SuppressWarnings("serial")
public class BalancesComboBoxModel extends DefaultComboBoxModel<Pair<Tuple2<String, Long>, BigDecimal>> implements Observer {

	private SortableList<Tuple2<String, Long>, BigDecimal> balances;

	public BalancesComboBoxModel(Account account)
	{
		Controller.getInstance().addObserver(this);
		this.balances = Controller.getInstance().getBalances(account);
		this.balances.registerObserver();
		
		this.update();
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
	
	private synchronized void syncUpdate(Observable o, Object arg)
	{
		ObserverMessage message = (ObserverMessage) arg;
		
		if(( message.getType() == ObserverMessage.NETWORK_STATUS && (int) message.getValue() == Controller.STATUS_OK )
				||	(Controller.getInstance().getStatus() == Controller.STATUS_OK && (message.getType() == ObserverMessage.ADD_BALANCE_TYPE || message.getType() == ObserverMessage.REMOVE_BALANCE_TYPE)))
		{
			this.update();
		}
	}
	
	private void update()
	{
		//GET SELECTED ITEM
		//Account selected = (Account) this.getSelectedItem();
					
		//EMPTY LIST
		this.removeAllElements();
			
		//INSERT ALL ACCOUNTS
		for(int i=0; i<this.balances.size(); i++)
		{
			this.addElement(this.balances.get(i));
		}
			
		//RESET SELECTED ITEM
		/*if(this.getIndexOf(selected) != -1)
		{
			this.setSelectedItem(selected);
		}*/
	}
	
	public void removeObservers()
	{
		Controller.getInstance().deleteObserver(this);
		this.balances.removeObserver();
	}
}
