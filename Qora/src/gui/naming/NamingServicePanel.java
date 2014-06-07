package gui.naming;

import gui.Gui;
import gui.models.NamingServiceTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

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
		
		//TABLE
		final NamingServiceTableModel nameModel = new NamingServiceTableModel();
		final JTable table = Gui.createSortableTable(nameModel, 0);
		
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(2);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
		
		//MENU
		JPopupMenu menu = new JPopupMenu();	
		JMenuItem update = new JMenuItem("Update");
		update.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
							
				Name name = nameModel.getName(row);
				new UpdateNameFrame(name);
			}
		});
		menu.add(update);
		
		JMenuItem sell = new JMenuItem("Sell");
		sell.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
							
				Name name = nameModel.getName(row);
				new SellNameFrame(name);
			}
		});
		menu.add(sell);
		
		JMenuItem details = new JMenuItem("Details");
		details.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
							
				Name name = nameModel.getName(row);
				new NameDetailsFrame(name);
			}
		});
		menu.add(details);
		
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
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
			
		//ADD REGISTER BUTTON
		JButton registerButton = new JButton("Register");
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
		JButton exchangeButton = new JButton("Exchange");
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
