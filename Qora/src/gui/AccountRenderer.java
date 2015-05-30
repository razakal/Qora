package gui;

import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;

import qora.account.Account;

public class AccountRenderer implements ListCellRenderer<Account> 
{
	private DefaultListCellRenderer defaultRenderer;
	private long key;

	public AccountRenderer(long key)
	{
		this.defaultRenderer = new DefaultListCellRenderer();
		this.key = key;
	}
	
	public void setAsset(long key)
	{
		this.key = key;
	}
	
	@SuppressWarnings("rawtypes")
	public Component getListCellRendererComponent(JList list, Account value, int index, boolean isSelected, boolean cellHasFocus) 
	{
		JLabel renderer = (JLabel) this.defaultRenderer.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		
		if(value != null)
		{
			renderer.setText(value.toString(this.key));
		}
		
		return renderer;
	}
}