package gui.naming;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import gui.Gui;
import gui.models.WalletNameSalesTableModel;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.TableRowSorter;

import qora.naming.NameSale;
import utils.BigDecimalStringComparator;


public class NameExchangeTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private AllNameSalesPanel allNameSalesPanel;
	private WalletNameSalesTableModel walletNameSalesTableModel;
		
	@SuppressWarnings("unchecked")
	public NameExchangeTabPane()
	{
		super();
			
		//ALL NAME SALES
		this.allNameSalesPanel = new AllNameSalesPanel();
		this.addTab("All Names", this.allNameSalesPanel);
		
		//WALLET NAME SALES
		this.walletNameSalesTableModel = new WalletNameSalesTableModel();
		final JTable walletNameSalesTable = Gui.createSortableTable(this.walletNameSalesTableModel, 0);
		
		TableRowSorter<WalletNameSalesTableModel> sorter =  (TableRowSorter<WalletNameSalesTableModel>) walletNameSalesTable.getRowSorter();
		sorter.setComparator(WalletNameSalesTableModel.COLUMN_PRICE, new BigDecimalStringComparator());
			
		//MENU
		JPopupMenu walletNameSalesMenu = new JPopupMenu();	
		JMenuItem cancel = new JMenuItem("Cancel");
		cancel.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = walletNameSalesTable.getSelectedRow();
				row = walletNameSalesTable.convertRowIndexToModel(row);
									
				NameSale nameSale = walletNameSalesTableModel.getNameSale(row);
				new CancelSellNameFrame(nameSale);
			}
		});
		walletNameSalesMenu.add(cancel);
		
		walletNameSalesTable.setComponentPopupMenu(walletNameSalesMenu);
		walletNameSalesTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = walletNameSalesTable.rowAtPoint(p);
				walletNameSalesTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = walletNameSalesTable.convertRowIndexToModel(row);
					NameSale nameSale = walletNameSalesTableModel.getNameSale(row);
					new CancelSellNameFrame(nameSale);
				}
		     }
		});
		
		
		this.add("My Names", new JScrollPane(walletNameSalesTable));
		
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.allNameSalesPanel.removeObservers();
		
		this.walletNameSalesTableModel.removeObservers();
	}
	
}
