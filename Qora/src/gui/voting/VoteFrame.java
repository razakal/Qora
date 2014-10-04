package gui.voting;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import gui.models.OptionsComboBoxModel;

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

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.transaction.Transaction;
import qora.voting.Poll;
import qora.voting.PollOption;
import utils.Pair;

@SuppressWarnings("serial")
public class VoteFrame extends JFrame
{
	private Poll poll;
	private JComboBox<Account> cbxAccount;
	private JComboBox<PollOption> cbxOptions;
	private JButton voteButton;
	private JTextField txtFee;
	
	public VoteFrame(Poll poll, int option)
	{
		super("Qora - Vote");
		
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
		
		//LABEL NAME
		labelGBC.gridy = 1;
		JLabel nameLabel = new JLabel("Poll:");
		this.add(nameLabel, labelGBC);
		
		//NAME
		detailGBC.gridy = 1;
		JTextField name = new JTextField(poll.getName());
		name.setEditable(false);
		this.add(name, detailGBC);		
		
		//LABEL DESCRIPTION
		labelGBC.gridy = 2;
		JLabel descriptionLabel = new JLabel("Description:");
		this.add(descriptionLabel, labelGBC);
				
		//DESCRIPTION
		detailGBC.gridy = 2;
		JTextArea txtAreaDescription = new JTextArea(poll.getDescription());
		txtAreaDescription.setRows(4);
		txtAreaDescription.setBorder(name.getBorder());
		txtAreaDescription.setEditable(false);
		this.add(txtAreaDescription, detailGBC);		
		
        //LABEL ACCOUNT
      	labelGBC.gridy = 3;
      	JLabel ownerLabel = new JLabel("Account:");
      	this.add(ownerLabel, labelGBC);
      		
      	//CBX ACCOUNT
      	detailGBC.gridy = 3;
      	this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel());
      	this.add(this.cbxAccount, detailGBC);
		
      	//LABEL OPTIONS
      	labelGBC.gridy = 4;
      	JLabel optionsLabel = new JLabel("Option:");
      	this.add(optionsLabel, labelGBC);
      		
      	//CBX ACCOUNT
      	detailGBC.gridy = 4;
      	this.cbxOptions = new JComboBox<PollOption>(new OptionsComboBoxModel(poll.getOptions()));
      	this.cbxOptions.setSelectedIndex(option);
      	this.add(this.cbxOptions, detailGBC);
      	
      	 //LABEL FEE
      	labelGBC.gridy = 5;
      	JLabel feeLabel = new JLabel("Fee:");
      	this.add(feeLabel, labelGBC);
      		
      	//TXT FEE
      	detailGBC.gridy = 5;
      	this.txtFee = new JTextField();
      	this.txtFee.setText("1");
        this.add(this.txtFee, detailGBC);
		
		//ADD EXCHANGE BUTTON
		detailGBC.gridy = 6;
		voteButton = new JButton("Vote");
		voteButton.setPreferredSize(new Dimension(100, 25));
		voteButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onVoteClick();
			}
		});	
		this.add(voteButton, detailGBC);
		
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
	
	public void onVoteClick()
	{
		//DISABLE
		this.voteButton.setEnabled(false);
	
		//CHECK IF NETWORK OKE
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.voteButton.setEnabled(true);
			
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
				this.voteButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) cbxAccount.getSelectedItem();
		
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.voteButton.setEnabled(true);
				
				return;
			}
		
			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			PollOption option = (PollOption) this.cbxOptions.getSelectedItem();
			
			Pair<Transaction, Integer> result = Controller.getInstance().createPollVote(creator, poll, option, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Poll vote has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				Date release = new Date(Transaction.VOTING_RELEASE);	
				DateFormat format = DateFormat.getDateTimeInstance();
				JOptionPane.showMessageDialog(new JFrame(), "Voting will be enabled at " + format.format(release) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
			
			case Transaction.ALREADY_VOTED_FOR_THAT_OPTION:
				
				JOptionPane.showMessageDialog(new JFrame(), "You have already voted for that option!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), "Not enough balance!", "Error", JOptionPane.ERROR_MESSAGE);
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
		this.voteButton.setEnabled(true);
	}
}
