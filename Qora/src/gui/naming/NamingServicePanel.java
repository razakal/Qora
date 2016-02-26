package gui.naming;

import gui.QoraRowSorter;
import gui.models.WalletNamesTableModel;
import lang.Lang;

import java.awt.Dimension;
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

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import database.wallet.NameMap;
import qora.naming.Name;

@SuppressWarnings("serial")
public class NamingServicePanel extends JPanel
{
	public NamingServicePanel()
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
		tableGBC.gridwidth = 10;
		tableGBC.gridx = 0;	
		tableGBC.gridy= 0;	
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(10, 0, 0, 10);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridx = 0;	
		buttonGBC.gridy = 1;	
		
		//NAMES
		final WalletNamesTableModel namesModel = new WalletNamesTableModel();
		final JTable namesTable = new JTable(namesModel);
		
		//NAMES SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		indexes.put(WalletNamesTableModel.COLUMN_NAME, NameMap.NAME_INDEX);
		indexes.put(WalletNamesTableModel.COLUMN_ADDRESS, NameMap.OWNER_INDEX);
		QoraRowSorter sorter = new QoraRowSorter(namesModel, indexes);
		namesTable.setRowSorter(sorter);
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = namesTable.getColumnModel().getColumn(2);
		confirmedColumn.setCellRenderer(namesTable.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	
		JMenuItem update = new JMenuItem(Lang.getInstance().translate("Update"));
		update.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = namesTable.getSelectedRow();
				row = namesTable.convertRowIndexToModel(row);
							
				Name name = namesModel.getName(row);
				new UpdateNameFrame(name);
			}
		});
		menu.add(update);
		
		JMenuItem sell = new JMenuItem(Lang.getInstance().translate("Sell"));
		sell.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = namesTable.getSelectedRow();
				row = namesTable.convertRowIndexToModel(row);
							
				Name name = namesModel.getName(row);
				new SellNameFrame(name);
			}
		});
		menu.add(sell);
		
		JMenuItem details = new JMenuItem(Lang.getInstance().translate("Details"));
		details.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = namesTable.getSelectedRow();
				row = namesTable.convertRowIndexToModel(row);
							
				Name name = namesModel.getName(row);
				new NameDetailsFrame(name);
			}
		});
		menu.add(details);
		
		namesTable.setComponentPopupMenu(menu);
		namesTable.addMouseListener(new MouseAdapter() 
		{
		     @Override
		     public void mousePressed(MouseEvent e) 
		     {
		        Point p = e.getPoint();
		        int row = namesTable.rowAtPoint(p);
		        namesTable.setRowSelectionInterval(row, row);
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(namesTable), tableGBC);
			
		//ADD REGISTER BUTTON
		JButton registerButton = new JButton(Lang.getInstance().translate("Register"));
		registerButton.setPreferredSize(new Dimension(100, 25));
		registerButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onRegisterClick();
		    }
		});	
		this.add(registerButton, buttonGBC);
		
		//ADD EXCHANGE BUTTON
		buttonGBC.gridx = 1;
		JButton exchangeButton = new JButton(Lang.getInstance().translate("Exchange"));
		exchangeButton.setPreferredSize(new Dimension(100, 25));
		exchangeButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onExchangeClick();
			}
		});	
		this.add(exchangeButton, buttonGBC);
	}
	
	public void onRegisterClick()
	{
		new RegisterNameFrame();
	}
	
	public void onExchangeClick()
	{
		new NameExchangeFrame();
	}
}
