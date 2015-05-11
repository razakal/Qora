package gui.at;


import gui.QoraRowSorter;
import gui.models.ATTableModel;
import gui.models.ATTxsTableModel;
import gui.models.AssetsTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;


@SuppressWarnings("serial")
public class ATTransactionsPanel extends JPanel
{
	private ATTxsTableModel atTxsTableModel;

	public ATTransactionsPanel()
	{
		this.setLayout(new GridBagLayout());
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		this.atTxsTableModel = new ATTxsTableModel();
		final JTable atsTable = new JTable(this.atTxsTableModel);
		
		
		//ASSETS SORTER
		Map<Integer, Integer> indexes = new TreeMap<Integer, Integer>();
		QoraRowSorter sorter = new QoraRowSorter(this.atTxsTableModel, indexes);
		atsTable.setRowSorter(sorter);
		
		GridBagConstraints tableGBC = new GridBagConstraints();
		tableGBC.insets = new Insets(0, 5, 5, 0);
		tableGBC.fill = GridBagConstraints.BOTH;  
		tableGBC.anchor = GridBagConstraints.NORTHWEST;
		tableGBC.weightx = 1;	
		tableGBC.weighty = 1;	
		tableGBC.gridwidth = 2;
		tableGBC.gridx = 0;	
		tableGBC.gridy = 1;	
		
		this.add(new JScrollPane(atsTable), tableGBC);
		
		
	}
}
