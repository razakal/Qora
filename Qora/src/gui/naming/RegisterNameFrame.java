package gui.naming;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import utils.Pair;

@SuppressWarnings("serial")
public class RegisterNameFrame extends JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaValue;
	private JLabel countLabel;
	private JButton registerButton;

	public RegisterNameFrame()
	{
		super("Qora - Register Name");
		
		//CLOSE
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
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
		
		//LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel("Account:");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		txtGBC.gridy = 0;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
        this.add(this.cbxFrom, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = 1;
      	JLabel nameLabel = new JLabel("Name:");
      	this.add(nameLabel, labelGBC);
      		
      	//TXT NAME
      	txtGBC.gridy = 1;
      	this.txtName = new JTextField();
        this.add(this.txtName, txtGBC);
        
        //LABEL NAME
      	labelGBC.gridy = 2;
      	JLabel valueLabel = new JLabel("Value:");
      	this.add(valueLabel, labelGBC);
      		
      	//TXTAREA NAME
      	txtGBC.gridy = 2;
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
      	this.txtareaValue.setBorder(this.txtName.getBorder());
      	
      	JScrollPane Valuescroll = new JScrollPane(this.txtareaValue);
      	Valuescroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
      	Valuescroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
      	this.add(Valuescroll, txtGBC);
      	
      			
      	//LABEL COUNT
		labelGBC.gridy = 3;
		labelGBC.gridx = 1;
		countLabel = new JLabel("Character count: 0/4000");
		this.add(countLabel, labelGBC);
		
      	//LABEL FEE
      	labelGBC.gridy = 4;
      	labelGBC.gridx = 0;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 4;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 5;
        registerButton = new JButton("Register");
        registerButton.setPreferredSize(new Dimension(80, 25));
        registerButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onRegisterClick();
		    }
		});
    	this.add(registerButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onRegisterClick()
	{
		//DISABLE
		this.registerButton.setEnabled(false);
	
		//CHECK IF NETWORK OKE
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.registerButton.setEnabled(true);
			
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
				this.registerButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) cbxFrom.getSelectedItem();
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.registerButton.setEnabled(true);
				
				return;
			}
		
			//CREATE NAME REGISTRATION
			PrivateKeyAccount registrant = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().registerName(registrant, registrant, this.txtName.getText(),this.txtareaValue.getText(), fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name registration has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			
			case Transaction.NAME_NOT_LOWER_CASE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be lower case!", "Error", JOptionPane.ERROR_MESSAGE);
				this.txtName.setText(this.txtName.getText().toLowerCase());
				break;	
				
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), "Not enough balance!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_NAME_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Name must be between 1 and 100 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_VALUE_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Value must be between 1 and 1000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NAME_ALREADY_REGISTRED:
				
				JOptionPane.showMessageDialog(new JFrame(), "That name is already registred!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.registerButton.setEnabled(true);
	}
}
