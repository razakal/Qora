package gui.naming;

import gui.PasswordPane;
import gui.models.KeyValueTableModel;
import gui.models.NameComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.naming.Name;
import qora.transaction.Transaction;
import settings.Settings;
import utils.GZIP;
import utils.MenuPopupUtil;
import utils.Pair;
import controller.Controller;
import database.DBSet;

@SuppressWarnings("serial")
public class UpdateNameFrame extends JFrame
{
	private JComboBox<Name> cbxName;
	private JTextField txtOwner;
	private JTextField txtKey;
	private JTextArea txtareaValue;	
	private JTextField txtFee;
	private JButton updateButton;
//	private JButton CompressButton;
	private JButton removeButton;
	private JButton addButton;
	private JLabel countLabel;
	private KeyValueTableModel namesModel;
	
	public UpdateNameFrame(Name name)
	{
		super("Qora - Update Name");
		
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
		labelGBC.insets = new Insets(5,5,5,5);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.weightx = 0;	
		labelGBC.gridx = 0;
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		
		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;
		txtGBC.gridwidth = 2;
		txtGBC.gridx = 1;		
		
		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.NONE;  
		buttonGBC.anchor = GridBagConstraints.NORTHWEST;
		buttonGBC.gridwidth = 2;
		buttonGBC.gridx = 0;		
		
		//LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel("Name:");
      	this.add(nameLabel, labelGBC);
      	
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.cbxName = new JComboBox<Name>(new NameComboBoxModel());
      	
        this.add(this.cbxName, txtGBC);
        
        //LABEL OWNER
      	labelGBC.gridy = 2;
      	JLabel ownerLabel = new JLabel("Owner:");
      	this.add(ownerLabel, labelGBC);
      	
      		
      	//TXT OWNER
      	txtGBC.gridy = 2;
      	this.txtOwner = new JTextField();
      	this.add(this.txtOwner, txtGBC);
      	
      	 //LABEL OWNER
      	labelGBC.gridy = 3;
      	JLabel keyLabel = new JLabel("Key:");
      	this.add(keyLabel, labelGBC);
      	
      //TXT OWNER
      	txtGBC.gridy = 3;
      	this.txtKey = new JTextField();
      	this.add(this.txtKey, txtGBC);
        
        //LABEL VALUE
      	labelGBC.gridy = 4;
      	JLabel valueLabel = new JLabel("Value:");
      	this.add(valueLabel, labelGBC);
      		
      	//TXTAREA VALUE
      	txtGBC.gridy = 4;
      	this.txtareaValue = new JTextArea();
      	
      	this.txtareaValue.setRows(20);
      	this.txtareaValue.setColumns(63);
      	this.txtareaValue.setBorder(cbxName.getBorder());

      	JScrollPane Valuescroll = new JScrollPane(this.txtareaValue);
      	Valuescroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	Valuescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(Valuescroll, txtGBC);
        
      	//LABEL COUNT
		labelGBC.gridy = 5;
		//labelGBC.gridwidth = ;
		labelGBC.gridx = 1;
		countLabel = new JLabel("Character count: 0/4000");
		this.add(countLabel, labelGBC);
		
		//TABLE GBC
				GridBagConstraints tableGBC = new GridBagConstraints();
				tableGBC.fill = GridBagConstraints.BOTH; 
				tableGBC.anchor = GridBagConstraints.NORTHWEST;
				tableGBC.weightx = 1;
				tableGBC.weighty = 1;
				tableGBC.gridwidth = 10;
				tableGBC.gridx = 0;	
				tableGBC.gridy= 6;	
		
				namesModel = new KeyValueTableModel();
				final JTable namesTable = new JTable(namesModel);
				
				JScrollPane scrollPane = new JScrollPane(namesTable);
		        scrollPane.setPreferredSize(new Dimension(100, 150));
		        scrollPane.setWheelScrollingEnabled(true);
		
				namesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
				namesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(final ListSelectionEvent e) {
				
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						if(!e.getValueIsAdjusting())
						{
							ListSelectionModel lsm = (ListSelectionModel)e.getSource();
							
							int minSelectionIndex = lsm.getMinSelectionIndex();
							txtareaValue.setEnabled(minSelectionIndex != -1);
							txtKey.setEnabled(minSelectionIndex != -1);
							txtareaValue.setText((String) namesModel.getValueAt(minSelectionIndex, 1)); 
							txtKey.setText((String) namesModel.getValueAt(minSelectionIndex, 0)); 
						}
					}
				});
				
			}
		});
		
		
			this.txtareaValue.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				update(namesModel, namesTable);
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				update(namesModel, namesTable);
				countLabel.setText("Character count: "+String.valueOf(txtareaValue.getText().length())+"/4000");
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				update(namesModel, namesTable);
				countLabel.setText("Character count: "+String.valueOf(txtareaValue.getText().length())+"/4000");
			}
			public void update(final KeyValueTableModel namesModel,
					final JTable namesTable) {
				final int selectedRow = namesTable.getSelectedRow();
				SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						String newValue = txtareaValue.getText();
						namesModel.setValueAt(newValue, selectedRow, 1);
						namesModel.fireTableCellUpdated(selectedRow, 1);
					}
				});
			}
        });
		
			//PREVENT UPPERCASE FOR KEYS
//		((AbstractDocument)	txtKey.getDocument()).setDocumentFilter(new DocumentFilter()
//		{
//			@Override
//            public void insertString(FilterBypass fb, int offset,
//                    String string, AttributeSet attr)
//                    throws BadLocationException {
//				super.insertString(fb, offset, string.toLowerCase(), attr);
//            }
//
//
//            @Override
//            public void replace(FilterBypass fb, int offset, int length,
//                    String text, AttributeSet attrs)
//                    throws BadLocationException {
//            	super.insertString(fb, offset, text.toLowerCase(), attrs);
//            }
//		});
		
		txtKey.getDocument().addDocumentListener(new DocumentListener() {
			  public void changedUpdate(DocumentEvent e) {
			    valueChanged(e);
			  }
			  public void removeUpdate(DocumentEvent e) {
			    valueChanged(e);
			  }
			  public void insertUpdate(DocumentEvent e) {
			    valueChanged(e);
			  }

			  public void valueChanged(final DocumentEvent e) {
				  SwingUtilities.invokeLater(new Runnable() {
					
					@Override
					public void run() {
						final int selectedRow = namesTable.getSelectedRow();
						String newKey = txtKey.getText().toLowerCase();
						
						namesModel.setValueAt(newKey, selectedRow, 0);
						namesModel.fireTableCellUpdated(selectedRow, 0);
						
					}
				});
			  }
			});
		
		
		txtareaValue.setEnabled(namesTable.getSelectedRow() != -1);
		txtKey.setEnabled(namesTable.getSelectedRow() != -1);
		
		
		this.cbxName.addItemListener(new ItemListener()
      	{
      		@Override
      	    public void itemStateChanged(ItemEvent event) 
      		{
      			if (event.getStateChange() == ItemEvent.SELECTED) 
      			{
      				Name name = (Name) event.getItem();
      	 
//      				txtareaValue.setText(name.getValue());
//      				txtOwner.setText(name.getOwner().getAddress());
      				
      				String value = GZIP.webDecompress(name.getValue());
      				JSONObject jsonObject;
      				try {
						jsonObject = (JSONObject) JSONValue.parse(value);
						
					} catch (Exception e) {
						jsonObject = null;
					}
      				
      				List<Pair<String, String>> keyvaluepairs = new ArrayList<>();
      				if(jsonObject != null)
      				{
      					@SuppressWarnings("unchecked")
						Set<String> keySet = jsonObject.keySet();
      					for (String key : keySet) {
      						Object object = jsonObject.get(key);
      						if(object instanceof Long)
      						{
      							object = ""+object;
      						}
							keyvaluepairs.add(new Pair<String, String>(key, (String) object));
      					}
      					
      				}else
      				{
      					keyvaluepairs.add(new Pair<String, String>("defaultkey", value));
      				}
      				
      				namesModel.setData(keyvaluepairs);
      				namesTable.requestFocus();
      				namesTable.changeSelection(0,0,false, false);
      				
      			}
      	    }    
      	});
		
		
		

		//ADD NAMING SERVICE TABLE
		this.add(new JScrollPane(scrollPane), tableGBC);
		
      	//LABEL FEE
		labelGBC.gridx = 0;
		labelGBC.gridy = 8;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 8;
      	txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 9;
        updateButton = new JButton("Update");
        updateButton.setPreferredSize(new Dimension(80, 25));
        updateButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onUpdateClick();
		    }
		});
    	this.add(updateButton, buttonGBC);
             
    	//BUTTON COMPRESS
//        buttonGBC.gridy = 5;
//        buttonGBC.gridx = 1;
//        buttonGBC.fill = GridBagConstraints.EAST;
//        buttonGBC.anchor = GridBagConstraints.EAST;
//        
//        CompressButton = new JButton("Compress/Decompress");
//        CompressButton.setPreferredSize(new Dimension(150, 25));
//    
//        CompressButton.addActionListener(new ActionListener()
//	    {
//			public void actionPerformed(ActionEvent e)
//			{
//		    	txtareaValue.setText(GZIP.autoDecompress(txtareaValue.getText()));
//		    }
//		});
//    	this.add(CompressButton, buttonGBC);
    	
    	
    	//BUTTON REMOVE
        buttonGBC.gridy = 7;
        buttonGBC.gridx = 1;
        buttonGBC.fill = GridBagConstraints.EAST;
        buttonGBC.anchor = GridBagConstraints.EAST;
        
        removeButton = new JButton("Remove");
        removeButton.setPreferredSize(new Dimension(150, 25));
        this.add(removeButton, buttonGBC);
        
        buttonGBC.gridx = 0;
        addButton = new JButton("Add");
        addButton.setPreferredSize(new Dimension(150, 25));
        this.add(addButton, buttonGBC);
    
        removeButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				int index = namesTable.getSelectionModel().getMinSelectionIndex();
				if(index != -1)
				{
					namesModel.removeEntry(index);
					
					if(namesModel.getRowCount() > index)
					{
						namesTable.requestFocus();
						namesTable.changeSelection(index,index,false, false);
					}
				}
		    }
		});
        
        
        addButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
				namesModel.addAtEnd();
				namesTable.requestFocus();
				int index = namesModel.getRowCount();
				namesTable.changeSelection(index-1,index-1,false, false);
		    }
		});

    	//SET DEFAULT SELECTED ITEM
    	if(this.cbxName.getItemCount() > 0)
    	{
    		this.cbxName.setSelectedItem(name);
    		this.txtareaValue.setText(name.getValue());
			this.txtOwner.setText(name.getOwner().getAddress());
    	}
    	
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
        
      	//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtOwner);
      	MenuPopupUtil.installContextMenu(txtareaValue);
      	MenuPopupUtil.installContextMenu(txtFee);
	}
	
	
	public void onUpdateClick()
	{
		//DISABLE
		this.updateButton.setEnabled(false);
		
		
		//CHECK IF NETWORK OKE
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.updateButton.setEnabled(true);
			
			return;
		}
		
		//CHECK IF WALLET UNLOCKED
		if(!Controller.getInstance().isWalletUnlocked())
		{
			//ASK FOR PASSWORD
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!Controller.getInstance().unlockWallet(password))
			{
				//WRONG PASSWORD
				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ NAME
		Name name = (Name) this.cbxName.getSelectedItem();
		name.setOwner(DBSet.getInstance().getNameMap().get(name.getName()).getOwner());
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
		
			//CHECK BIG FEE
			if(fee.compareTo(Settings.getInstance().getBigFee()) >= 0)
			{
				int n = JOptionPane.showConfirmDialog(
						new JFrame(), Settings.getInstance().getBigFeeMessage(),
		                "Confirmation",
		                JOptionPane.YES_NO_OPTION);
				if (n == JOptionPane.YES_OPTION) {
					
				}
				if (n == JOptionPane.NO_OPTION) {
					
					txtFee.setText("1");
					
					//ENABLE
					this.updateButton.setEnabled(true);
					
					return;
				}
			}
			
			
			Pair<Boolean, String> isUpdatable = namesModel.checkUpdateable();
			if(!isUpdatable.getA())
			{
				
				JOptionPane.showMessageDialog(new JFrame(), isUpdatable.getB(), "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
			
			//CREATE NAME UPDATE
			PrivateKeyAccount owner = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			String currentValueAsJsonStringOpt = namesModel.getCurrentValueAsJsonStringOpt();
			if(currentValueAsJsonStringOpt == null)
			{
					JOptionPane.showMessageDialog(new JFrame(), "Bad Json value", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.updateButton.setEnabled(true);
				
				return;
			}
			
			
			currentValueAsJsonStringOpt = GZIP.compress(currentValueAsJsonStringOpt);
			
			Pair<Transaction, Integer> result = Controller.getInstance().updateName(owner, new Account(this.txtOwner.getText()), name.getName(), currentValueAsJsonStringOpt, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name update has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), "Not enough balance!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be between 1 and 400 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_VALUE_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Value must be between 1 and 4000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NAME_DOES_NOT_EXIST:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NAME_ALREADY_FOR_SALE:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name is already for sale!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid owner!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_NAME_OWNER:
				
				JOptionPane.showMessageDialog(new JFrame(), "You are no longer the owner this name!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			default:
				
				JOptionPane.showMessageDialog(new JFrame(), "Unknown error!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			}
		}
		catch(Exception e)
		{
			JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
		}
		
		//ENABLE
		this.updateButton.setEnabled(true);
	}
}
