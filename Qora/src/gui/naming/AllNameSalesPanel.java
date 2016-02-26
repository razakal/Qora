package gui.naming;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import gui.QoraRowSorter;
import gui.models.NameSalesTableModel;
import lang.Lang;

import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import database.NameExchangeMap;
import qora.naming.NameSale;

@SuppressWarnings("serial")
public class AllNameSalesPanel extends JPanel {
	private NameSalesTableModel nameSalesTableModel;

	public AllNameSalesPanel() {
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//SEACH LABEL GBC
		GridBagConstraints searchLabelGBC = new GridBagConstraints();
		searchLabelGBC.insets = new Insets(0, 5, 5, 0);
		searchLabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchLabelGBC.anchor = GridBagConstraints.NORTHWEST;
		searchLabelGBC.weightx = 0;	
		searchLabelGBC.gridwidth = 1;
		searchLabelGBC.gridx = 0;
		searchLabelGBC.gridy = 0;
		
		//SEACH GBC
		GridBagConstraints searchGBC = new GridBagConstraints();
		searchGBC.insets = new Insets(0, 5, 5, 0);
		searchGBC.fill = GridBagConstraints.HORIZONTAL;   
		searchGBC.anchor = GridBagConstraints.NORTHWEST;
		searchGBC.weightx = 1;	
		searchGBC.gridwidth = 1;
		searchGBC.gridx = 1;
		searchGBC.gridy = 0;
		
		//TABLE GBC
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	
		
		//CREATE TABLE
		this.nameSalesTableModel = new NameSalesTableModel();
		final JTable nameSalesTable = new JTable(this.nameSalesTableModel);
		
		//NAMESALES SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(NameSalesTableModel.COLUMN_NAME, NameExchangeMap.DEFAULT_INDEX);
		indexes.put(NameSalesTableModel.COLUMN_PRICE, NameExchangeMap.AMOUNT_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(this.nameSalesTableModel, indexes);
		nameSalesTable.setRowSorter(sorter);
		
		//CREATE SEARCH FIELD
		final JTextField txtSearch = new JTextField();

		// UPDATE FILTER ON TEXT CHANGE
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
				nameSalesTableModel.getSortableList().setFilter(search);
				nameSalesTableModel.fireTableDataChanged();
			}
		});

		// MENU
		JPopupMenu nameSalesMenu = new JPopupMenu();
		JMenuItem buy = new JMenuItem(Lang.getInstance().translate("Buy"));
		buy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int row = nameSalesTable.getSelectedRow();
				row = nameSalesTable.convertRowIndexToModel(row);

				NameSale nameSale = nameSalesTableModel.getNameSale(row);
				new BuyNameFrame(nameSale);
			}
		});
		nameSalesMenu.add(buy);

		nameSalesTable.setComponentPopupMenu(nameSalesMenu);
		nameSalesTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				Point p = e.getPoint();
				int row = nameSalesTable.rowAtPoint(p);
				nameSalesTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = nameSalesTable.convertRowIndexToModel(row);
					NameSale nameSale = nameSalesTableModel.getNameSale(row);
					new BuyNameFrame(nameSale);
				}
			}
		});

		this.add(new JLabel(Lang.getInstance().translate("search:")), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(nameSalesTable), tableGBC);
	}

	public void removeObservers() {
		this.nameSalesTableModel.removeObservers();
	}
}
