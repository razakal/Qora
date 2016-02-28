package gui.settings;

import java.awt.Color;
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
import javax.swing.JCheckBox;
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
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import gui.models.AllowedTableModel;
import lang.Lang;
import settings.Settings;
import utils.IPAddressFormatValidator;
import utils.TableMenuPopupUtil;

@SuppressWarnings("serial")
public class SettingsAllowedPanel extends JPanel 
{
	public AllowedTableModel webAllowedTableModel;
	private JTable webAllowedTable;
	public AllowedTableModel rpcAllowedTableModel;
	private JTable rpcAllowedTable;
	private JButton btnAddAddressWeb;
	private JButton btnAddAddressRpc;
	private JTextField textAddressWeb;
	private JTextField textAddressRpc;
	public JCheckBox chckbxWebAllowForAll;
	public JCheckBox chckbxRpcAllowForAll;
	boolean rpcServiceRestart = false;
	boolean webServiceRestart = false;
	
	public SettingsAllowedPanel()
	{

		//PADDING
		this.setBorder(new EmptyBorder(5, 5, 5, 10));

        GridBagLayout gridBagLayout1 = new GridBagLayout();
        gridBagLayout1.columnWidths = new int[]{60, 202, 60};
        gridBagLayout1.rowHeights = new int[]{0, 100, 0, 15, 0, 100, 0};
        setLayout(gridBagLayout1);
        
        this.webAllowedTableModel = new AllowedTableModel(Settings.getInstance().getWebAllowed());
        

        webAllowedTable = new JTable(webAllowedTableModel);
        
        webAllowedTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
	    		if(e.getKeyCode()==KeyEvent.VK_DELETE) {
	    			int row = webAllowedTable.getSelectedRow();
	    			webAllowedTableModel.deleteAddress(row);
	    			webServiceRestart = true;
	    		}
            } 
        });
        
        JPopupMenu menu = new JPopupMenu();	
        
 		JMenuItem deleteaddressmenu = new JMenuItem(Lang.getInstance().translate("Delete address"));
 		deleteaddressmenu.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				int row = webAllowedTable.getSelectedRow();
 				webAllowedTableModel.deleteAddress(row);
 				webServiceRestart = true;
 			}
 		});
 		
 		menu.add(deleteaddressmenu);
        
        JLabel lblWebWhiteList = new JLabel(Lang.getInstance().translate("WEB server white list. The addresses are allowed to access the node."));
        lblWebWhiteList.setHorizontalAlignment(SwingConstants.LEFT);
        GridBagConstraints gbc_lblWebWhiteList = new GridBagConstraints();
        gbc_lblWebWhiteList.anchor = GridBagConstraints.WEST;
        gbc_lblWebWhiteList.gridwidth = 2;
        gbc_lblWebWhiteList.insets = new Insets(0, 0, 5, 5);
        gbc_lblWebWhiteList.gridx = 0;
        gbc_lblWebWhiteList.gridy = 0;
        add(lblWebWhiteList, gbc_lblWebWhiteList);
        
        chckbxWebAllowForAll = new JCheckBox(Lang.getInstance().translate("Allow for all"));
        GridBagConstraints gbc_chckbxWebAllowForAll = new GridBagConstraints();
        gbc_chckbxWebAllowForAll.gridwidth = 2;
        gbc_chckbxWebAllowForAll.insets = new Insets(0, 0, 5, 0);
        gbc_chckbxWebAllowForAll.gridx = 2;
        gbc_chckbxWebAllowForAll.gridy = 0;

        add(chckbxWebAllowForAll, gbc_chckbxWebAllowForAll);
		
        TableMenuPopupUtil.installContextMenu(webAllowedTable, menu);
 
        webAllowedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        GridBagConstraints gbc_webAllowedScrollPane = new GridBagConstraints();
        gbc_webAllowedScrollPane.fill = GridBagConstraints.BOTH;
        gbc_webAllowedScrollPane.gridwidth = 3;
        gbc_webAllowedScrollPane.insets = new Insets(0, 0, 5, 0);
        gbc_webAllowedScrollPane.gridx = 0;
        gbc_webAllowedScrollPane.gridy = 1;
        
        JScrollPane webAllowedScrollPane = new JScrollPane(webAllowedTable);
        webAllowedScrollPane.setPreferredSize(new Dimension( 490, 180 ));
        this.add(webAllowedScrollPane, gbc_webAllowedScrollPane);
         
	     JLabel lblAddNewAddress = new JLabel(Lang.getInstance().translate("Add new address:"));
	     GridBagConstraints gbc_lblAddNewAddress = new GridBagConstraints();
	     gbc_lblAddNewAddress.anchor = GridBagConstraints.NORTHEAST;
	     gbc_lblAddNewAddress.insets = new Insets(4, 0, 5, 5);
	     gbc_lblAddNewAddress.gridx = 0;
	     gbc_lblAddNewAddress.gridy = 2;
	     add(lblAddNewAddress, gbc_lblAddNewAddress);
		
		 GridBagConstraints gbc_textAddressWeb = new GridBagConstraints();
		 gbc_textAddressWeb.insets = new Insets(0, 0, 5, 5);
		 gbc_textAddressWeb.fill = GridBagConstraints.HORIZONTAL;
		 gbc_textAddressWeb.gridx = 1;
		 gbc_textAddressWeb.gridy = 2;
		 
		 textAddressWeb = new JTextField();
		 add(textAddressWeb, gbc_textAddressWeb);
		 textAddressWeb.setColumns(10);
		 textAddressWeb.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		
        this.rpcAllowedTableModel = new AllowedTableModel(Settings.getInstance().getRpcAllowed());

        rpcAllowedTable = new JTable(rpcAllowedTableModel);
        
        rpcAllowedTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
	    		if(e.getKeyCode()==KeyEvent.VK_DELETE) {
	    			int row = rpcAllowedTable.getSelectedRow();
	    			rpcAllowedTableModel.deleteAddress(row);
	    			rpcServiceRestart = true;
	    		}
            } 
        });
        
        JPopupMenu menuRpc = new JPopupMenu();	
        
 		JMenuItem deleteaddressmenu1 = new JMenuItem(Lang.getInstance().translate("Delete address"));
 		deleteaddressmenu1.addActionListener(new ActionListener()
 		{
 			public void actionPerformed(ActionEvent e) 
 			{
 				int row = rpcAllowedTable.getSelectedRow();
 				rpcAllowedTableModel.deleteAddress(row);
 				rpcServiceRestart = true;
 			}
 		});
 		
 		menuRpc.add(deleteaddressmenu1);
        
               
        btnAddAddressWeb = new JButton(Lang.getInstance().translate("Add"));
        GridBagConstraints gbc_btnAddAddressWeb = new GridBagConstraints();
        gbc_btnAddAddressWeb.insets = new Insets(0, 0, 5, 0);
        gbc_btnAddAddressWeb.fill = GridBagConstraints.BOTH;
        gbc_btnAddAddressWeb.anchor = GridBagConstraints.SOUTHWEST;
        gbc_btnAddAddressWeb.gridx = 2;
        gbc_btnAddAddressWeb.gridy = 2;
        btnAddAddressWeb.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		onAddClick(textAddressWeb.getText(), 1);
        	}
        });	    
        add(btnAddAddressWeb, gbc_btnAddAddressWeb);
    
        JLabel lblRpcWhiteList = new JLabel(Lang.getInstance().translate("RPC server white list. The addresses are allowed to access the node."));
        GridBagConstraints gbc_lblRpcWhiteList = new GridBagConstraints();
        gbc_lblRpcWhiteList.anchor = GridBagConstraints.WEST;
        gbc_lblRpcWhiteList.gridwidth = 2;
        gbc_lblRpcWhiteList.insets = new Insets(0, 0, 5, 5);
        gbc_lblRpcWhiteList.gridx = 0;
        gbc_lblRpcWhiteList.gridy = 4;
        add(lblRpcWhiteList, gbc_lblRpcWhiteList);
        
        chckbxRpcAllowForAll = new JCheckBox(Lang.getInstance().translate("Allow for all"));
        GridBagConstraints gbc_RpcAllowForAll = new GridBagConstraints();
        gbc_RpcAllowForAll.insets = new Insets(0, 0, 5, 0);
        gbc_RpcAllowForAll.gridx = 2;
        gbc_RpcAllowForAll.gridy = 4;
        
        add(chckbxRpcAllowForAll, gbc_RpcAllowForAll);
		
        TableMenuPopupUtil.installContextMenu(rpcAllowedTable, menuRpc);
 		
        rpcAllowedTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        GridBagConstraints gbc_rpcAllowedTable = new GridBagConstraints();
			
        gbc_rpcAllowedTable.fill = GridBagConstraints.BOTH;
        gbc_rpcAllowedTable.gridwidth = 3;
        gbc_rpcAllowedTable.insets = new Insets(0, 0, 5, 0);
        gbc_rpcAllowedTable.gridx = 0;
        gbc_rpcAllowedTable.gridy = 5;
		
		JScrollPane rpcScrollPane = new JScrollPane(rpcAllowedTable);
		
		rpcScrollPane.setPreferredSize(new Dimension( 490, 180 ));
        this.add(rpcScrollPane, gbc_rpcAllowedTable);
		
		JLabel labelRpcAddNewAddress = new JLabel(Lang.getInstance().translate("Add new address:"));
		GridBagConstraints gbc_labelRpcAddNewAddress = new GridBagConstraints();
		gbc_labelRpcAddNewAddress.anchor = GridBagConstraints.EAST;
		gbc_labelRpcAddNewAddress.insets = new Insets(4, 0, 0, 5);
		gbc_labelRpcAddNewAddress.gridx = 0;
		gbc_labelRpcAddNewAddress.gridy = 6;
		add(labelRpcAddNewAddress, gbc_labelRpcAddNewAddress);
		
		textAddressRpc = new JTextField();
		textAddressRpc.setFont(new Font("Monospaced", Font.PLAIN, 12));
		GridBagConstraints gbc_textAddressRpc = new GridBagConstraints();
		gbc_textAddressRpc.insets = new Insets(0, 0, 0, 5);
		gbc_textAddressRpc.fill = GridBagConstraints.HORIZONTAL;
		gbc_textAddressRpc.gridx = 1;
		gbc_textAddressRpc.gridy = 6;
		add(textAddressRpc, gbc_textAddressRpc);
		
		btnAddAddressRpc = new JButton(Lang.getInstance().translate("Add"));
		GridBagConstraints btn_AddAddressRpc = new GridBagConstraints();
		btn_AddAddressRpc.anchor = GridBagConstraints.SOUTHWEST;
		btn_AddAddressRpc.fill = GridBagConstraints.BOTH;
		btn_AddAddressRpc.gridx = 2;
		btn_AddAddressRpc.gridy = 6;
		btnAddAddressRpc.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		onAddClick(textAddressRpc.getText(), 2);
        	}
        });	   
		add(btnAddAddressRpc, btn_AddAddressRpc);

		chckbxRpcAllowForAll.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		AllowedEnabled(chckbxRpcAllowForAll.isSelected(),2);
        		rpcServiceRestart = true;
        	}
        });	  
        
		chckbxRpcAllowForAll.setSelected(Settings.getInstance().getRpcAllowed().length == 0);
        AllowedEnabled(chckbxRpcAllowForAll.isSelected(), 2);
        
        chckbxWebAllowForAll.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		AllowedEnabled(chckbxWebAllowForAll.isSelected(),1);
        		webServiceRestart = true;
        	}
        });	    
        
        chckbxWebAllowForAll.setSelected(Settings.getInstance().getWebAllowed().length == 0);
        AllowedEnabled(chckbxWebAllowForAll.isSelected(), 1);
	}
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
	}
	
	public void onAddClick(String addip, int type)
	{
		IPAddressFormatValidator iPAddressFormatValidator = new IPAddressFormatValidator();
		if(!iPAddressFormatValidator.validate(addip))
		{
			JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("IP Address is not correct!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
		}
		else if(type == 1)
		{
			webAllowedTableModel.addAddress(addip);
			webServiceRestart = true;
		}
		else if(type == 2)
		{
			rpcAllowedTableModel.addAddress(addip);
			rpcServiceRestart = true;
		}
	}

	public void AllowedEnabled(boolean allow, int type)
	{
		if(type == 1)
		{
			webAllowedTable.setEnabled(!allow);
    		textAddressWeb.setEnabled(!allow);
    		btnAddAddressWeb.setEnabled(!allow);
    		
    		if(allow)
    		{
    			webAllowedTable.setBackground(getBackground());
    				
    		}
    		else
    		{        			
    			webAllowedTable.setBackground(Color.WHITE);
    			if(webAllowedTable.getRowCount() == 0)
    			{
    				webAllowedTableModel.addAddress("127.0.0.1");
    			}
    		}
	
		}
		else if (type == 2)
		{
			rpcAllowedTable.setEnabled(!allow);
    		textAddressRpc.setEnabled(!allow);
    		btnAddAddressRpc.setEnabled(!allow);
    		
    		if(allow)
    		{
    			rpcAllowedTable.setBackground(getBackground());
    		}
    		else
    		{        			
    			rpcAllowedTable.setBackground(Color.WHITE);
    			if(rpcAllowedTable.getRowCount() == 0)
    			{
    				rpcAllowedTableModel.addAddress("127.0.0.1");
    			}
    		}	
    	}
	}
}
