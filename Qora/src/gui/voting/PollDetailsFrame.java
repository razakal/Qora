package gui.voting;

import gui.Gui;
import gui.models.PollOptionsTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import qora.voting.Poll;

@SuppressWarnings("serial")
public class PollDetailsFrame extends JFrame
{
	private Poll poll;
	private JTable table;
	
	public PollDetailsFrame(Poll poll)
	{
		super("Qora - Poll Details");
		
		this.poll = poll;
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
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
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
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
