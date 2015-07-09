package gui.assets;

import gui.AccountRenderer;
import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.DateFormat;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.transaction.Transaction;
import settings.Settings;
import utils.Pair;

@SuppressWarnings("serial")
public class OrderPanel extends JPanel
{
	private Asset have;
	private Asset want;
	private JButton sellButton;
	private JComboBox<Account> cbxAccount;
	private JTextField txtAmount;
	private JTextField txtPrice;
	private JTextField txtFee;
	private JTextField txtBuyingPrice;
	private JTextField txtBuyingAmount;
	
	public OrderPanel(Asset have, Asset want, boolean buying)
	{
		this.setLayout(new GridBagLayout());
		
		this.have = have;
		this.want = want;
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//LABEL GBC
		GridBagConstraints labelGBC = new GridBagConstraints();
		labelGBC.insets = new Insets(0, 5, 5, 0);
		labelGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelGBC.anchor = GridBagConstraints.NORTHWEST;
		labelGBC.gridx = 0;
		
		//DETAIL GBC
		GridBagConstraints detailGBC = new GridBagConstraints();
		detailGBC.insets = new Insets(0, 5, 5, 0);
		detailGBC.fill = GridBagConstraints.HORIZONTAL;  
		detailGBC.anchor = GridBagConstraints.NORTHWEST;
		detailGBC.gridx = 1;	
		
		//LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel("Account:");
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		detailGBC.gridy = 0;
		this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel());
		this.cbxAccount.setRenderer(new AccountRenderer(this.have.getKey()));
        this.add(this.cbxAccount, detailGBC);
		
		//LABEL PRICE
		labelGBC.gridy++;
		JLabel priceLabel = new JLabel("Price:");
		this.add(priceLabel, labelGBC);
		
		//PRICE
		detailGBC.gridy++;
		txtPrice = new JTextField();
		this.add(txtPrice, detailGBC);	
		
		if(buying)
		{
			//LABEL BUYING PRICE
			labelGBC.gridy++;
			JLabel buyingPriceLabel = new JLabel("Buying price:");
			this.add(buyingPriceLabel, labelGBC);
			
			//BUYING PRICE
			detailGBC.gridy++;
			txtBuyingPrice = new JTextField();
			txtBuyingPrice.setEnabled(false);
			this.add(txtBuyingPrice, detailGBC);
			
			//ON PRICE CHANGE
			txtPrice.getDocument().addDocumentListener(new DocumentListener() 
			{
				public void changedUpdate(DocumentEvent e) 
				{
					calculateBuyingPrice(txtBuyingPrice);
				}
				
				public void removeUpdate(DocumentEvent e) 
				{
					calculateBuyingPrice(txtBuyingPrice);
				}
				  
				public void insertUpdate(DocumentEvent e) 
				{
					calculateBuyingPrice(txtBuyingPrice);
				}
			});
		}
		
		//LABEL AMOUNT
		labelGBC.gridy++;
		JLabel amountLabel = new JLabel("Amount:");
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		detailGBC.gridy++;
		this.txtAmount = new JTextField();
		this.add(this.txtAmount, detailGBC);	
		
		//LABEL AMOUNT
		labelGBC.gridy++;
		JLabel buyingAmountLabel = new JLabel("Buying amount:");
		this.add(buyingAmountLabel, labelGBC);
					
		//AMOUNT
		detailGBC.gridy++;
		txtBuyingAmount = new JTextField();
		txtBuyingAmount.setEnabled(false);
		this.add(txtBuyingAmount, detailGBC);
			
		//ON PRICE CHANGE
		txtAmount.getDocument().addDocumentListener(new DocumentListener() 
		{
			public void changedUpdate(DocumentEvent e) 
			{
				calculateBuyingAmount(txtBuyingAmount);
			}
			
			public void removeUpdate(DocumentEvent e) 
			{
				calculateBuyingAmount(txtBuyingAmount);
			}
				  
			public void insertUpdate(DocumentEvent e) 
			{
				calculateBuyingAmount(txtBuyingAmount);
			}
		});	
		
		//LABEL FEE
		labelGBC.gridy++;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);
		           
		//FEE
		detailGBC.gridy++;
		txtFee = new JTextField("1");
		this.add(txtFee, detailGBC);		
		
		
		//ADD SELL BUTTON
		labelGBC.gridy++;
		labelGBC.gridwidth = 2;
		this.sellButton = new JButton("Sell");
		this.sellButton.setPreferredSize(new Dimension(125, 25));
		this.sellButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onSellClick();
			}
		});	
		this.add(this.sellButton, labelGBC);
	}
	
	public void calculateBuyingPrice(JTextField target) 
	{
	    try
	    {
	    	BigDecimal price = new BigDecimal(txtPrice.getText());		    	
	    	target.setText(BigDecimal.ONE.setScale(8).divide(price, RoundingMode.DOWN).toPlainString());	
	    }
	    catch(Exception e)
	    {
	    	target.setText("0");
	    }
	    
	    calculateBuyingAmount(txtBuyingAmount);
	}
	
	public void calculateBuyingAmount(JTextField target) 
	{
	    try
	    {
	    	BigDecimal price = new BigDecimal(txtPrice.getText());		    	
	    	BigDecimal amount = new BigDecimal(txtAmount.getText());
	    	target.setText(price.multiply(amount).toPlainString());
	    }
	    catch(Exception e)
	    {
	    	target.setText("0");
	    }
	}
	
	public void onSellClick()
	{
		//DISABLE
		this.sellButton.setEnabled(false);
	
		//CHECK IF NETWORK OKE
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.sellButton.setEnabled(true);
			
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
				this.sellButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ CREATOR
		Account sender = (Account) this.cbxAccount.getSelectedItem();
		
		long parse = 0;
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(this.txtFee.getText()).setScale(8);
			
			//READ AMOUNT
			parse = 1;
			BigDecimal amount = new BigDecimal(this.txtAmount.getText()).setScale(8);
			
			//READ PRICE
			parse = 2;
			BigDecimal price = new BigDecimal(this.txtPrice.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sellButton.setEnabled(true);
				
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
					this.sellButton.setEnabled(true);
					
					return;
				}
			}

			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());
			Pair<Transaction, Integer> result = Controller.getInstance().createOrder(creator, this.have, this.want, amount, price, fee);
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Order has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				Date release = new Date(Transaction.ASSETS_RELEASE);	
				DateFormat format = DateFormat.getDateTimeInstance();
				JOptionPane.showMessageDialog(new JFrame(), "Assets will be enabled at " + format.format(release) + "!",  "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.HAVE_EQUALS_WANT:
				
				JOptionPane.showMessageDialog(new JFrame(), "Have can not equal Want!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.ASSET_DOES_NOT_EXIST:
				
				JOptionPane.showMessageDialog(new JFrame(), "The asset does not exist!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), "Amount must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.NEGATIVE_PRICE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Price must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.INVALID_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.INVALID_RETURN:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid total price!", "Error", JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.FEE_LESS_REQUIRED:
				
				JOptionPane.showMessageDialog(new JFrame(), "Fee below the minimum for this size of a transaction!", "Error", JOptionPane.ERROR_MESSAGE);
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
			e.printStackTrace();
			
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			if(parse == 1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
			}
			if(parse == 2)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Invalid price!", "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//ENABLE
		this.sellButton.setEnabled(true);
	}
	
	
	
}
