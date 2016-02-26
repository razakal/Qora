package gui.assets;

import gui.AccountRenderer;
import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.transaction.Transaction;
import settings.Settings;
import utils.DateTimeFormat;
import utils.Pair;
import controller.Controller;

@SuppressWarnings("serial")
public class OrderPanel extends JPanel
{
	private Asset have;
	private Asset want;
	private JButton sellButton;
	private JComboBox<Account> cbxAccount;
	public JTextField txtAmount;
	public JTextField txtPrice;
	private JTextField txtFee;
	private JTextField txtBuyingPrice;
	private JTextField txtBuyingAmount;
	private JTextPane superHintText;
	
	public OrderPanel(Asset have, Asset want, boolean buying)
	{
		this.setLayout(new GridBagLayout());
		
		this.have = have;
		this.want = want;
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//LABEL GBC
		GridBagConstraints superhintGBC = new GridBagConstraints();
		superhintGBC.insets = new Insets(0, 5, 5, 0);
		superhintGBC.fill = GridBagConstraints.BOTH;   
		superhintGBC.anchor = GridBagConstraints.SOUTHWEST;;
		superhintGBC.gridx = 0;
		superhintGBC.gridwidth = 3;
		superhintGBC.weightx = superhintGBC.weighty = 1.0;
		superhintGBC.weighty = 1.0;
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
		
		//DETAIL GBC
		GridBagConstraints assetHintGBC = new GridBagConstraints();
		assetHintGBC.insets = new Insets(0, 5, 5, 0);
		assetHintGBC.fill = GridBagConstraints.HORIZONTAL;  
		assetHintGBC.anchor = GridBagConstraints.NORTHWEST;
		assetHintGBC.gridx = 2;	
		
		//LABEL FROM
		labelGBC.gridy = 0;
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account:"));
		this.add(fromLabel, labelGBC);
		
		//COMBOBOX FROM
		detailGBC.gridy = 0;
		this.cbxAccount = new JComboBox<Account>(new AccountsComboBoxModel());
		this.cbxAccount.setRenderer(new AccountRenderer(this.have.getKey()));
        this.add(this.cbxAccount, detailGBC);
		
		//ASSET HINT
		assetHintGBC.gridy = detailGBC.gridy;
		JLabel accountHintLabel = new JLabel( have.getShort() );
		this.add(accountHintLabel, assetHintGBC);
        
		//LABEL PRICE
		labelGBC.gridy++;
		JLabel priceLabel = new JLabel(Lang.getInstance().translate("Price:"));
		this.add(priceLabel, labelGBC);
		
		//PRICE
		detailGBC.gridy++;
		txtPrice = new JTextField();
		this.add(txtPrice, detailGBC);	
		
		//ASSET HINT
		assetHintGBC.gridy = detailGBC.gridy;
		JLabel priceHintLabel = new JLabel( want.getShort() );
		this.add(priceHintLabel, assetHintGBC);
				
		if(buying)
		{
			//LABEL BUYING PRICE
			labelGBC.gridy++;
			JLabel buyingPriceLabel = new JLabel(Lang.getInstance().translate("Buying price:"));
			this.add(buyingPriceLabel, labelGBC);
			
			//BUYING PRICE
			detailGBC.gridy++;
			txtBuyingPrice = new JTextField();
			txtBuyingPrice.setEnabled(false);
			this.add(txtBuyingPrice, detailGBC);
			
			//ASSET HINT
			assetHintGBC.gridy = detailGBC.gridy;
			JLabel buyingPriceHintLabel = new JLabel( have.getShort() );
			this.add(buyingPriceHintLabel, assetHintGBC);
			
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
		else
		{
			//ON PRICE CHANGE
			txtPrice.getDocument().addDocumentListener(new DocumentListener() 
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
		}
		
		//LABEL AMOUNT
		labelGBC.gridy++;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount:"));
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		detailGBC.gridy++;
		this.txtAmount = new JTextField();
		this.add(this.txtAmount, detailGBC);	
		
		//ASSET HINT
		assetHintGBC.gridy = detailGBC.gridy;
		JLabel amountHintLabel = new JLabel( have.getShort() );
		this.add(amountHintLabel, assetHintGBC);
		
		//LABEL AMOUNT
		labelGBC.gridy++;
		JLabel buyingAmountLabel = new JLabel(Lang.getInstance().translate("Buying amount:"));
		this.add(buyingAmountLabel, labelGBC);
					
		//AMOUNT
		detailGBC.gridy++;
		txtBuyingAmount = new JTextField();
		txtBuyingAmount.setEnabled(false);
		this.add(txtBuyingAmount, detailGBC);
			
		//ASSET HINT
		assetHintGBC.gridy = detailGBC.gridy;
		JLabel buyingAmountHintLabel = new JLabel( want.getShort() );
		this.add(buyingAmountHintLabel, assetHintGBC);
		
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
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee:"));
		this.add(feeLabel, labelGBC);
		           
		//FEE
		detailGBC.gridy++;
		txtFee = new JTextField("1");
		this.add(txtFee, detailGBC);		
		
		//ASSET HINT
		assetHintGBC.gridy = detailGBC.gridy;
		JLabel feeHintLabel = new JLabel( Controller.getInstance().getAsset(0).getShort());
		this.add(feeHintLabel, assetHintGBC);
		
		//ADD SELL BUTTON
		labelGBC.gridy++;
		labelGBC.gridwidth = 3;
		
		superHintText = new JTextPane();
		superHintText.setEditable(false);
		superHintText.setBackground(this.getBackground());
		superHintText.setContentType("text/html");
		
		superHintText.setFont(txtBuyingAmount.getFont());
		superHintText.setText( "<html><body style='font-size: 100%'>&nbsp;<br>&nbsp;<br></body></html>" );
		
		superHintText.setPreferredSize(new Dimension(125, 40));
		
		JPanel scrollPaneSuperHintText = new JPanel(new BorderLayout());
		
		scrollPaneSuperHintText.add(superHintText, BorderLayout.SOUTH);
		
		this.add(scrollPaneSuperHintText, superhintGBC);
		
		labelGBC.gridy++;
		
		if(buying)
			this.sellButton = new JButton(Lang.getInstance().translate("Buy"));	
		else
			this.sellButton = new JButton(Lang.getInstance().translate("Sell"));	
		
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
	
	public void calculateHint() 
	{
		if(!isDigit(this.txtPrice.getText()))
			superHintText.setText( "<html><body style='font-size: 100%'>&nbsp;<br>" + Lang.getInstance().translate("Enter correct price.") + "</body></html>" );
		else if(!isDigit(this.txtAmount.getText()))
			superHintText.setText( "<html><body style='font-size: 100%'>&nbsp;<br>" + Lang.getInstance().translate("Enter correct amount.") + "</body></html>" );
		else
			superHintText.setText( "<html><body style='font-size: 100%'>" + 
					Lang.getInstance().translate("Give <b>%amount%&nbsp;%have%</b>" + 
					" at the price of <b>%price%&nbsp;%want%</b> per <b>1%&nbsp;%have%</b> that would get " + 
					"<b>%buyingamount%&nbsp;%want%</b>.")
					.replace("%amount%", this.txtAmount.getText())
					.replace("%have%", have.getShort())
					.replace("%price%", this.txtPrice.getText())
					.replace("%want%", want.getShort())
					.replace("%buyingamount%", this.txtBuyingAmount.getText())
					+"</body></html>");
	}
	
	private static boolean isDigit(String s) throws NumberFormatException {
	    try {
	        new BigDecimal(s);
	        return true;
	    } catch (NumberFormatException e) {
	        return false;
	    }
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
	    	target.setText(price.multiply(amount).setScale(8, RoundingMode.DOWN).toPlainString());
	    }
	    catch(Exception e)
	    {
	    	target.setText("0");
	    }
	    
	    calculateHint();
	}
	
	public void onSellClick()
	{
		//DISABLE
		this.sellButton.setEnabled(false);
	
		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			
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
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
				
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
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee must be at least 1!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sellButton.setEnabled(true);
				
				return;
			}
		
			//CHECK BIG FEE
			if(fee.compareTo(Settings.getInstance().getBigFee()) >= 0)
			{
				int n = JOptionPane.showConfirmDialog(
						new JFrame(), Lang.getInstance().translate("Do you really want to set such a large fee?\nThese coins will go to the forgers."),
						Lang.getInstance().translate("Confirmation"),
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
			case Transaction.VALIDATE_OK:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Order has been sent!"), Lang.getInstance().translate("Success"), JOptionPane.INFORMATION_MESSAGE);
				
				this.txtFee.setText("1");
				this.txtAmount.setText("");
				this.txtPrice.setText("");
				
				break;	
				
			case Transaction.NOT_YET_RELEASED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Assets will be enabled at %time%!").replace("%time%", DateTimeFormat.timestamptoString(Transaction.getASSETS_RELEASE())),  "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.HAVE_EQUALS_WANT:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Have can not equal Want!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.ASSET_DOES_NOT_EXIST:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("The asset does not exist!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Amount must be positive!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.NEGATIVE_PRICE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Price must be positive!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.INVALID_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.INVALID_RETURN:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid total price!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			case Transaction.NEGATIVE_FEE:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee must be at least 1!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	
				
			case Transaction.FEE_LESS_REQUIRED:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee below the minimum for this size of a transaction!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NO_BALANCE:
			
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;	

				
			default:
				
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				break;		
				
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			if(parse == 1)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid amount!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
			if(parse == 2)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid price!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//ENABLE
		this.sellButton.setEnabled(true);
	}
	
	
	
}
