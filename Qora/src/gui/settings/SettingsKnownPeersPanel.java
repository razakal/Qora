package gui.settings;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import gui.models.KnownPeersTableModel;
import lang.Lang;
import utils.IPAddressFormatValidator;
import utils.TableMenuPopupUtil;

@SuppressWarnings("serial")
public class SettingsKnownPeersPanel extends JPanel 
{
	public KnownPeersTableModel knownPeersTableModel;
	private JTextField textAddress;
	private JTable knownPeersTable;
	
	public SettingsKnownPeersPanel()
	{	
		//PADDING
		this.setBorder(new EmptyBorder(10, 5, 5, 10));

        GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{87, 202, 44, 37, 0};
        gridBagLayout.rowHeights = new int[]{281, 23, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);
        
		this.knownPeersTableModel = new KnownPeersTableModel();
		
		knownPeersTable = new JTable(knownPeersTableModel);

        GridBagConstraints gbc_knownPeersTable = new GridBagConstraints();
        gbc_knownPeersTable.fill = GridBagConstraints.BOTH;
        gbc_knownPeersTable.gridwidth = 5;
        gbc_knownPeersTable.anchor = GridBagConstraints.SOUTHWEST;
        gbc_knownPeersTable.insets = new Insets(0, 0, 5, 0);
        gbc_knownPeersTable.gridx = 0;
        gbc_knownPeersTable.gridy = 0;
        this.add(new JScrollPane(knownPeersTable), gbc_knownPeersTable);
        
        //CHECKBOX FOR CONNECTED
        TableColumn confirmedColumn = knownPeersTable.getColumnModel().getColumn(1);
        confirmedColumn.setCellRenderer(knownPeersTable.getDefaultRenderer(Boolean.class));
        
        JLabel lblAddNewAddress = new JLabel(Lang.getInstance().translate("Add new address:"));
        GridBagConstraints gbc_lblAddNewAddress = new GridBagConstraints();
        gbc_lblAddNewAddress.anchor = GridBagConstraints.NORTHEAST;
        gbc_lblAddNewAddress.insets = new Insets(4, 0, 0, 5);
        gbc_lblAddNewAddress.gridx = 0;
        gbc_lblAddNewAddress.gridy = 1;
        add(lblAddNewAddress, gbc_lblAddNewAddress);
       
        
        GridBagConstraints gbc_textAddress = new GridBagConstraints();
        gbc_textAddress.insets = new Insets(0, 0, 0, 5);
        gbc_textAddress.fill = GridBagConstraints.HORIZONTAL;
        gbc_textAddress.gridx = 1;
        gbc_textAddress.gridy = 1;
        
        textAddress = new JTextField();
        add(textAddress, gbc_textAddress);
        textAddress.setColumns(10);
        textAddress.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        textAddress.setPreferredSize( new Dimension( 150, 24 ));
        
        JButton btnAdd = new JButton(Lang.getInstance().translate("Add"));
        GridBagConstraints gbc_btnAdd = new GridBagConstraints();
        gbc_btnAdd.fill = GridBagConstraints.BOTH;
        gbc_btnAdd.gridwidth = 2;
        gbc_btnAdd.anchor = GridBagConstraints.SOUTHWEST;
        gbc_btnAdd.gridx = 2;
        gbc_btnAdd.gridy = 1;
        btnAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onAddClick();
			}
		});	    
        add(btnAdd, gbc_btnAdd);
        
        JPopupMenu menu = new JPopupMenu();	
        
		JMenuItem deleteaddressmenu = new JMenuItem(Lang.getInstance().translate("Delete address"));
		deleteaddressmenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = knownPeersTable.getSelectedRow();
				knownPeersTableModel.deleteAddress(row);
			}
		});
		
		menu.add(deleteaddressmenu);
		
		knownPeersTable.addKeyListener(new KeyAdapter() {
		    public void keyPressed(KeyEvent e) {
	    		if(e.getKeyCode()==KeyEvent.VK_DELETE) {
	    			int row = knownPeersTable.getSelectedRow();
	    			knownPeersTableModel.deleteAddress(row);
	    		}
		    } 
		});
		
		knownPeersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		TableMenuPopupUtil.installContextMenu(knownPeersTable, menu);
		
		confirmedColumn.setMaxWidth(100);
		confirmedColumn.setMinWidth(100);
         
	}
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.knownPeersTableModel.removeObservers();
	}
	
	public void onAddClick()
	{
		String addip = this.textAddress.getText();
		IPAddressFormatValidator iPAddressFormatValidator = new IPAddressFormatValidator();
		if(!iPAddressFormatValidator.validate(addip))
		{
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("IP Address is not correct!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		else
		{
			knownPeersTableModel.addAddress(addip);
		}
	}
	

}
