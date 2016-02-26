package gui.naming;

import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Map;
import java.util.TreeMap;

import gui.QoraRowSorter;
import gui.models.WalletNameSalesTableModel;
import lang.Lang;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;

import database.wallet.NameSaleMap;
import qora.naming.NameSale;


public class NameExchangeTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private AllNameSalesPanel allNameSalesPanel;
	private WalletNameSalesTableModel walletNameSalesTableModel;
		
	public NameExchangeTabPane()
	{
		super();
			
		//ALL NAME SALES
		this.allNameSalesPanel = new AllNameSalesPanel();
		this.addTab(Lang.getInstance().translate("All Names"), this.allNameSalesPanel);
		
		//NAME SALES
		this.walletNameSalesTableModel = new WalletNameSalesTableModel();
		final JTable walletNameSalesTable = new JTable(this.walletNameSalesTableModel);
			
		//NAME SALE SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletNameSalesTableModel.COLUMN_NAME, NameSaleMap.NAME_INDEX);
		indexes.put(WalletNameSalesTableModel.COLUMN_OWNER, NameSaleMap.SELLER_INDEX);
		indexes.put(WalletNameSalesTableModel.COLUMN_PRICE, NameSaleMap.AMOUNT_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(this.walletNameSalesTableModel, indexes);
		walletNameSalesTable.setRowSorter(sorter);
			
		//MENU
		JPopupMenu walletNameSalesMenu = new JPopupMenu();	
		JMenuItem cancel = new JMenuItem(Lang.getInstance().translate("Cancel"));
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
		
		
		this.add(Lang.getInstance().translate("My Names"), new JScrollPane(walletNameSalesTable));
		
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.allNameSalesPanel.removeObservers();
		
		this.walletNameSalesTableModel.removeObservers();
	}
	
}
