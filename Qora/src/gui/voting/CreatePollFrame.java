package gui.voting;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.models.CreateOptionsTableModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import utils.Pair;

@SuppressWarnings("serial")
public class CreatePollFrame extends JFrame
{
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JTextField txtName;
	private JTextArea txtareaDescription;
	private JButton createButton;
	private CreateOptionsTableModel optionsTableModel;

	public CreatePollFrame()
	{
		super("Qora - Create Poll");
		
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
      	JLabel descriptionLabel = new JLabel("Description:");
      	this.add(descriptionLabel, labelGBC);
      		
      	//TXTAREA NAME
      	txtGBC.gridy = 2;
      	this.txtareaDescription = new JTextArea();
      	this.txtareaDescription.setRows(4);
      	this.txtareaDescription.setBorder(this.txtName.getBorder());
      	this.add(this.txtareaDescription, txtGBC);
        
      	//LABEL OPTIONS
      	labelGBC.gridy = 3;
      	JLabel optionsLabel = new JLabel("Options:");
      	this.add(optionsLabel, labelGBC);
      	
      	//TABLE OPTIONS
      	txtGBC.gridy = 3;
      	this.optionsTableModel = new CreateOptionsTableModel(new Object[] { "Name" }, 0);
      	final JTable table = new JTable(optionsTableModel);
      	
      	this.add(new JScrollPane(table), txtGBC);
      	
      	//TABLE OPTIONS DELETE
      	txtGBC.gridy = 4;
      	JButton deleteButton = new JButton("Delete");
        deleteButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
            	
            	if(optionsTableModel.getRowCount() > 1)
            	{        	
	                int selRow = table.getSelectedRow();
	                if(selRow != -1) {
	                    ((DefaultTableModel) optionsTableModel).removeRow(selRow);
	                }
            	}
            }
        });
        
        this.add(deleteButton, txtGBC);
      	
        //LABEL FEE
      	labelGBC.gridy = 5;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	txtGBC.gridy = 5;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, txtGBC);
		           
        //BUTTON Register
        buttonGBC.gridy = 6;
        createButton = new JButton("Create");
        createButton.setPreferredSize(new Dimension(80, 25));
        createButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onRegisterClick();
		    }
		});
    	this.add(createButton, buttonGBC);
        
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onRegisterClick()
	{
		//DISABLE
		this.createButton.setEnabled(false);
	
		//CHECK IF NETWORK OKE
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.createButton.setEnabled(true);
			
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
				this.createButton.setEnabled(true);
				
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
				this.createButton.setEnabled(true);
				
				return;
			}
		
			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().createPoll(creator, this.txtName.getText(),this.txtareaDescription.getText(), this.optionsTableModel.getOptions(), fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Poll creation has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				Date release = new Date(Transaction.VOTING_RELEASE);	
				DateFormat format = DateFormat.getDateTimeInstance();
				JOptionPane.showMessageDialog(new JFrame(), "Voting will be enabled at " + format.format(release) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
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
				
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "Description must be between 1 and 1000 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.POLL_ALREADY_CREATED:
				
				JOptionPane.showMessageDialog(new JFrame(), "A poll with that name already exists!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.INVALID_OPTIONS_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "The amount of options must be between 1 and 100!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.INVALID_OPTION_LENGTH:
				
				JOptionPane.showMessageDialog(new JFrame(), "All options must be between 1 and 100 characters!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.DUPLICATE_OPTION:
				
				JOptionPane.showMessageDialog(new JFrame(), "All options must be unique!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.createButton.setEnabled(true);
	}
}
