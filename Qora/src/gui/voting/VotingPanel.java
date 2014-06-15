package gui.voting;


import gui.Gui;
import gui.models.WalletPollsTableModel;

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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import qora.voting.Poll;

@SuppressWarnings("serial")
public class VotingPanel extends JPanel
{
	public VotingPanel()
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
		final WalletPollsTableModel nameModel = new WalletPollsTableModel();
		final JTable table = Gui.createSortableTable(nameModel, 0);
				
		//CHECKBOX FOR CONFIRMED
		TableColumn confirmedColumn = table.getColumnModel().getColumn(3);
		confirmedColumn.setCellRenderer(table.getDefaultRenderer(Boolean.class));
				
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
		
		table.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = table.rowAtPoint(p);
				table.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = table.convertRowIndexToModel(row);
					Poll poll = nameModel.getPoll(row);
					new PollDetailsFrame(poll);
				}
		     }
		});
		
		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(table), tableGBC);
		
		
		
		//ADD REGISTER BUTTON
		JButton createButton = new JButton("Create Poll");
		createButton.setPreferredSize(new Dimension(100, 25));
		createButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onCreateClick();
		    }
		});	
		this.add(createButton, buttonGBC);
		
		//ADD EXCHANGE BUTTON
		buttonGBC.gridx = 1;
		JButton allButton = new JButton("All Polls");
		allButton.setPreferredSize(new Dimension(100, 25));
		allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAllClick();
			}
		});	
		this.add(allButton, buttonGBC);
	}
	
	public void onCreateClick()
	{
		new CreatePollFrame();
	}
	
	public void onAllClick()
	{
		new AllPollsFrame();
	}
}
