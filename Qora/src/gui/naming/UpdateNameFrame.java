package gui.naming;

import gui.PasswordPane;
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

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.google.common.io.BaseEncoding;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.naming.Name;
import qora.transaction.Transaction;
import utils.GZIP;
import utils.MenuPopupUtil;
import utils.Pair;

@SuppressWarnings("serial")
public class UpdateNameFrame extends JFrame
{
	private JComboBox<Name> cbxName;
	private JTextField txtOwner;
	private JTextArea txtareaValue;	
	private JTextField txtFee;
	private JButton updateButton;
	private JButton CompressButton;
	private JButton DeCompressButton;
	private JLabel countLabel;
	
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
      	this.cbxName.addItemListener(new ItemListener()
      	{
      		@Override
      	    public void itemStateChanged(ItemEvent event) 
      		{
      			if (event.getStateChange() == ItemEvent.SELECTED) 
      			{
      				Name name = (Name) event.getItem();
      	 
      				txtareaValue.setText(name.getValue());
      				txtOwner.setText(name.getOwner().getAddress());
      			}
      	    }    
      	});
        this.add(this.cbxName, txtGBC);
        
        //LABEL OWNER
      	labelGBC.gridy = 2;
      	JLabel ownerLabel = new JLabel("Owner:");
      	this.add(ownerLabel, labelGBC);
      		
      	//TXT OWNER
      	txtGBC.gridy = 2;
      	this.txtOwner = new JTextField();
      	this.add(this.txtOwner, txtGBC);
        
        //LABEL VALUE
      	labelGBC.gridy = 3;
      	JLabel valueLabel = new JLabel("Value:");
      	this.add(valueLabel, labelGBC);
      		
      	//TXTAREA VALUE
      	txtGBC.gridy = 3;
      	this.txtareaValue = new JTextArea();
      	this.txtareaValue.getDocument().addDocumentListener(new DocumentListener() {
            
			@Override
			public void changedUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
			}
			@Override
			public void insertUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				countLabel.setText("Character count: "+String.valueOf(txtareaValue.getText().length())+"/4000");
			}
			@Override
			public void removeUpdate(DocumentEvent arg0) {
				// TODO Auto-generated method stub
				countLabel.setText("Character count: "+String.valueOf(txtareaValue.getText().length())+"/4000");
			}
        });
      	this.txtareaValue.setRows(20);
      	this.txtareaValue.setColumns(63);
      	this.txtareaValue.setBorder(cbxName.getBorder());

      	JScrollPane Valuescroll = new JScrollPane(this.txtareaValue);
      	Valuescroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	Valuescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	this.add(Valuescroll, txtGBC);
        
      	//LABEL COUNT
		labelGBC.gridy = 4;
		//labelGBC.gridwidth = ;
		labelGBC.gridx = 1;
		countLabel = new JLabel("Character count: 0/4000");
		this.add(countLabel, labelGBC);

      	//LABEL FEE
		labelGBC.gridx = 0;
		labelGBC.gridy = 5;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 5;
      	txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 6;
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
        buttonGBC.gridy = 4;
        buttonGBC.gridx = 1;
        buttonGBC.fill = GridBagConstraints.EAST;
        buttonGBC.anchor = GridBagConstraints.EAST;
        
        CompressButton = new JButton("Compress/Decompress");
        CompressButton.setPreferredSize(new Dimension(150, 25));
    
        CompressButton.addActionListener(new ActionListener()
	    {
			public void actionPerformed(ActionEvent e)
			{
		    	txtareaValue.setText(GZIP.autoDecompress(txtareaValue.getText()));
		    }
		});
    	this.add(CompressButton, buttonGBC);

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
		
			//CREATE NAME UPDATE
			PrivateKeyAccount owner = Controller.getInstance().getPrivateKeyAccountByAddress(name.getOwner().getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().updateName(owner, new Account(this.txtOwner.getText()), name.getName(), this.txtareaValue.getText(), fee);
			
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
