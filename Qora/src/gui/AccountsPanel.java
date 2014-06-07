package gui;

import gui.models.AccountsTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableRowSorter;

import qora.account.Account;
import utils.BigDecimalStringComparator;
import controller.Controller;

@SuppressWarnings("serial")
public class AccountsPanel extends JPanel
{
	@SuppressWarnings("unchecked")
	public AccountsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.fill = GridBagConstraints.BOTH; 
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;
		tableGBC.weighty = 1;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 0;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 0);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;	
		buttonGBC.gridy = 1;	
		
		//TABLE
		final AccountsTableModel tableModel = new AccountsTableModel();
		final JTable table = Gui.createSortableTable(tableModel, 1);
		
		TableRowSorter<AccountsTableModel> sorter =  (TableRowSorter<AccountsTableModel>) table.getRowSorter();
		sorter.setComparator(AccountsTableModel.COLUMN_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_CONFIRMED_BALANCE, new BigDecimalStringComparator());
		sorter.setComparator(AccountsTableModel.COLUMN_CONFIRMED_BALANCE, new BigDecimalStringComparator());
		
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	
		
		JMenuItem copyAddress = new JMenuItem("Copy Address");
		copyAddress.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyAddress);
				
		JMenuItem copyBalance = new JMenuItem("Copy Balance");
		copyBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getUnconfirmedBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyBalance);
		
		JMenuItem copyConfirmedBalance = new JMenuItem("Copy Confirmed Balance");
		copyConfirmedBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getConfirmedBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyConfirmedBalance);
		
		JMenuItem copyGeneratingBalance = new JMenuItem("Copy Generating Balance");
		copyGeneratingBalance.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Account account = tableModel.getAccount(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getGeneratingBalance().toPlainString());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyGeneratingBalance);
		
		table.setComponentPopupMenu(menu);
		table.addMouseListener(new MouseAdapter() 
		{
		     @Override
		     public void mousePressed(MouseEvent e) 
		     {
		        Point p = e.getPoint();
		        int row = table.rowAtPoint(p);
		        table.setRowSelectionInterval(row, row);
		     }
		});
		
		//ADD ACCOUNTS TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		//ADD TOTAL BALANCE
		final JLabel totalBalance = new JLabel("Confirmed Balance: " + tableModel.getTotalBalance().toPlainString());
		this.add(totalBalance, buttonGBC);
		
		//ON TABLE CHANGE
		table.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent arg0) {
				totalBalance.setText("Confirmed Balance: " + tableModel.getTotalBalance().toPlainString());				
			}		
		});
		
		//ADD NEW ACCOUNT BUTTON
		buttonGBC.gridy++;
		JButton newButton = new JButton("New account");
		newButton.setPreferredSize(new Dimension(150, 25));
		newButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onNewClick();
		    }
		});	
		this.add(newButton, buttonGBC);
	}
	
	public void onNewClick()
	{
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
				return;
			}
		}
		
		//GENERATE NEW ACCOUNT
		Controller.getInstance().generateNewAccount();
	}
}
