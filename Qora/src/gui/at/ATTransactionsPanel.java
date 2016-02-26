package gui.at;

import gui.QoraRowSorter;
import gui.models.ATTxsTableModel;
import lang.Lang;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import qora.account.Account;
import utils.MenuPopupUtil;
import utils.TableMenuPopupUtil;


@SuppressWarnings("serial")
public class ATTransactionsPanel extends JPanel
{
	private ATTxsTableModel atTxsTableModel;

	public ATTransactionsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.atTxsTableModel = new ATTxsTableModel();
		final JTable atsTable = new JTable(this.atTxsTableModel);
			
		//ASSETS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(ATTxsTableModel.COLUMN_HEIGHT, ATTxsTableModel.COLUMN_HEIGHT);
		QoraRowSorter sorter = new QoraRowSorter(this.atTxsTableModel, indexes);
		sorter.toggleSortOrder(ATTxsTableModel.COLUMN_HEIGHT);
		sorter.toggleSortOrder(ATTxsTableModel.COLUMN_HEIGHT);
		atsTable.setRowSorter(sorter);
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	

		
		JMenuItem copySender = new JMenuItem(Lang.getInstance().translate("Copy Sender"));
		copySender.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = atsTable.getSelectedRow();
				row = atsTable.convertRowIndexToModel(row);

				Account account = new Account(atTxsTableModel.getSender(row));

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copySender);
		JMenuItem copyRecipient = new JMenuItem(Lang.getInstance().translate("Copy Recipient"));
		copyRecipient.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = atsTable.getSelectedRow();
				row = atsTable.convertRowIndexToModel(row);

				Account account = new Account(atTxsTableModel.getRecipient(row));

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(account.getAddress());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyRecipient);
		JMenuItem copyAmount = new JMenuItem(Lang.getInstance().translate("Copy Amount"));
		copyAmount.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = atsTable.getSelectedRow();
				row = atsTable.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(atTxsTableModel.getAmount(row).toString());
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyAmount);
		
		JMenuItem copyMessage = new JMenuItem(Lang.getInstance().translate("Copy Message"));
		copyMessage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = atsTable.getSelectedRow();
				row = atsTable.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(atTxsTableModel.getMessage(row));
				clipboard.setContents(value, null);
			}
		});
		menu.add(copyMessage);
		TableMenuPopupUtil.installContextMenu(atsTable, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON

		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtSearch);
      	
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	
		this.add(new JScrollPane(atsTable), tableGBC);
		
	}
}
