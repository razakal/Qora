package gui.at;


import gui.Gui;
import gui.models.ATTxsTableModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import qora.account.Account;
import utils.IntegerComparator;
import utils.LongComparator;
import utils.MenuPopupUtil;
import utils.StringComparator;
import utils.TableMenuPopupUtil;


@SuppressWarnings("serial")
public class ATTransactionsPanel extends JPanel
{
	private ATTxsTableModel atTxsTableModel;

	@SuppressWarnings("unchecked")
	public ATTransactionsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.atTxsTableModel = new ATTxsTableModel();
		final JTable atsTable = Gui.createSortableTable(atTxsTableModel, 1);
		final TableRowSorter<ATTxsTableModel> sorter =  (TableRowSorter<ATTxsTableModel>) atsTable.getRowSorter();
		sorter.setComparator(ATTxsTableModel.COLUMN_HEIGHT, new IntegerComparator());
		sorter.setComparator(ATTxsTableModel.COLUMN_SENDER, new StringComparator());
		sorter.setComparator(ATTxsTableModel.COLUMN_RECIPIENT, new StringComparator());
		sorter.setComparator(ATTxsTableModel.COLUMN_AMOUNT, new LongComparator());
		sorter.setComparator(ATTxsTableModel.COLUMN_MESSAGE, new StringComparator());
		sorter.setComparator(ATTxsTableModel.COLUMN_SEQUENCE, new StringComparator());
	
		//SEARCH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 0;

		//SEARCH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 0;
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	

		JMenuItem copySender = new JMenuItem("Copy Sender");
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

		JMenuItem copyRecipient = new JMenuItem("Copy Recipient");
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

		JMenuItem copyAmount = new JMenuItem("Copy Amount");
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
		
		JMenuItem copyMessage = new JMenuItem("Copy Message");
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

		txtSearch.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				onChange();
			}

			public void removeUpdate(DocumentEvent e) {
				onChange();
			}

			public void insertUpdate(DocumentEvent e) {
				onChange();
			}

			public void onChange() {

				// GET VALUE
				String search = txtSearch.getText();

				// SET FILTER
				sorter.setRowFilter(RowFilter.regexFilter(search));
				atTxsTableModel.fireTableDataChanged();
			}
		});
		
		//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtSearch);
      	
		this.add(new JLabel("search:"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		
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
