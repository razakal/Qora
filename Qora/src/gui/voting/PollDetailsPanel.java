package gui.voting;

import gui.Gui;
import gui.models.PollOptionsTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableRowSorter;

import qora.voting.Poll;
import utils.BigDecimalStringComparator;

@SuppressWarnings("serial")
public class PollDetailsPanel extends JPanel
{
	private Poll poll;
	private JTable table;
	
	@SuppressWarnings("unchecked")
	public PollDetailsPanel(Poll poll)
	{
		this.poll = poll;
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.weightx = 1;	
		detailGBC.gridwidth = 2;
		detailGBC.gridx = 1;		
		
		//LABEL CREATOR
		labelGBC.gridy = 1;
		JLabel creatorLabel = new JLabel("Creator:");
		this.add(creatorLabel, labelGBC);
		
		//CREATOR
		detailGBC.gridy = 1;
		JTextField creator = new JTextField(poll.getCreator().getAddress());
		creator.setEditable(false);
		this.add(creator, detailGBC);
		
		//LABEL NAME
		labelGBC.gridy = 2;
		JLabel nameLabel = new JLabel("Name:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 2;
		JTextField name = new JTextField(poll.getName());
		name.setEditable(false);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 3;
		JLabel descriptionLabel = new JLabel("Description:");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = 3;
		JTextArea txtAreaDescription = new JTextArea(poll.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);		
		
		//LABEL OPTIONS
		labelGBC.gridy = 4;
		JLabel optionsLabel = new JLabel("Options:");
		this.add(optionsLabel, labelGBC);
		
		//OPTIONS
		detailGBC.gridy = 4;
		PollOptionsTableModel pollOptionsTableModel = new PollOptionsTableModel(poll);
		table = Gui.createSortableTable(pollOptionsTableModel, 0);
		
		TableRowSorter<PollOptionsTableModel> sorter =  (TableRowSorter<PollOptionsTableModel>) table.getRowSorter();
		sorter.setComparator(PollOptionsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());
		
		this.add(new JScrollPane(table), detailGBC);
		
		//ADD EXCHANGE BUTTON
		detailGBC.gridy = 5;
		JButton allButton = new JButton("Vote");
		allButton.setPreferredSize(new Dimension(100, 25));
		allButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onVoteClick();
			}
		});	
		this.add(allButton, detailGBC);
		
		//PACK
		this.setVisible(true);
	}
	
	public void onVoteClick()
	{
		//GET SELECTED OPTION
		int row = this.table.getSelectedRow();
		if(row == -1)
		{
			row = 0;
		}
		row = this.table.convertRowIndexToModel(row);
		
		new VoteFrame(this.poll, row);
	}
}
