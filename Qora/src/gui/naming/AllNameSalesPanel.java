package gui.naming;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import gui.Gui;
import gui.models.NameSalesTableModel;

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

import qora.naming.NameSale;
import utils.BigDecimalStringComparator;

@SuppressWarnings("serial")
public class AllNameSalesPanel extends JPanel {
	private NameSalesTableModel nameSalesTableModel;

	@SuppressWarnings("unchecked")
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
		final JTable nameSalesTable = Gui.createSortableTable(this.nameSalesTableModel, 0);
		
		TableRowSorter<NameSalesTableModel> sorter =  (TableRowSorter<NameSalesTableModel>) nameSalesTable.getRowSorter();
		sorter.setComparator(NameSalesTableModel.COLUMN_PRICE, new BigDecimalStringComparator());

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

				// FILTER
				RowFilter<NameSalesTableModel, Object> rowFilter = RowFilter
						.regexFilter(search, 0);

				// GET ROW SORTER
				TableRowSorter<NameSalesTableModel> rowSorter = (TableRowSorter<NameSalesTableModel>) nameSalesTable.getRowSorter();

				// SET FILTER
				rowSorter.setRowFilter(rowFilter);

			}
		});

		// MENU
		JPopupMenu nameSalesMenu = new JPopupMenu();
		JMenuItem buy = new JMenuItem("Buy");
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

		this.add(new JLabel("search:"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(nameSalesTable), tableGBC);
	}

	public void removeObservers() {
		this.nameSalesTableModel.removeObservers();
	}
}
