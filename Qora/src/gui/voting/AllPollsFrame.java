package gui.voting;

import gui.Gui;
import gui.models.PollsTableModel;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.TableRowSorter;

import qora.voting.Poll;
import utils.BigDecimalStringComparator;

@SuppressWarnings("serial")
public class AllPollsFrame extends JFrame{

	private PollsTableModel pollsTableModel;
	
	@SuppressWarnings("unchecked")
	public AllPollsFrame() 
	{
		//CREATE FRAME
		super("Qora - All Polls");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//LAYOUT
		this.setLayout(new GridBagLayout());
		
		//PADDING
		((JComponent) this.getContentPane()).setBorder(new EmptyBorder(5, 5, 5, 5));
		
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
		this.pollsTableModel = new PollsTableModel();
		final JTable pollsTable = Gui.createSortableTable(this.pollsTableModel, 0);
		
		TableRowSorter<PollsTableModel> sorter =  (TableRowSorter<PollsTableModel>) pollsTable.getRowSorter();
		sorter.setComparator(PollsTableModel.COLUMN_VOTES, new BigDecimalStringComparator());

		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
		     }
		});
		
		pollsTable.addMouseListener(new MouseAdapter() 
		{
			@Override
			public void mousePressed(MouseEvent e) 
			{
				Point p = e.getPoint();
				int row = pollsTable.rowAtPoint(p);
				pollsTable.setRowSelectionInterval(row, row);
				
				if(e.getClickCount() == 2)
				{
					row = pollsTable.convertRowIndexToModel(row);
					Poll poll = pollsTableModel.getPoll(row);
					new PollFrame(poll);
				}
		     }
		});
		
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
				RowFilter<PollsTableModel, Object> rowFilter = RowFilter
						.regexFilter(search, 0);

				// GET ROW SORTER
				TableRowSorter<PollsTableModel> rowSorter = (TableRowSorter<PollsTableModel>) pollsTable.getRowSorter();

				// SET FILTER
				rowSorter.setRowFilter(rowFilter);

			}
		});

		this.add(new JLabel("search:"), searchLabelGBC);
		this.add(txtSearch, searchGBC);
		this.add(new JScrollPane(pollsTable), tableGBC);

		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//REMOVE OBSERVERS/HANLDERS
            	pollsTableModel.removeObservers();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
