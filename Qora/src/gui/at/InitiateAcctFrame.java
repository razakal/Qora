package gui.at;

import gui.PasswordPane;
import gui.models.AccountsComboBoxModel;
import lang.Lang;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.Base58;
import qora.transaction.Transaction;
import utils.Converter;
import utils.Pair;
import at.AT_Constants;
import at.AT_Error;

import com.google.common.primitives.Bytes;
import com.google.common.primitives.Longs;

import controller.Controller;
import database.DBSet;

@SuppressWarnings("serial")
public class InitiateAcctFrame extends JFrame {
	private JComboBox<Account> cbxFrom;
	private JTextField txtFee;
	private JPasswordField txtPlainSecret;
	private JPasswordField txtRetypePass;
	private JTextField txtExpirationBlocks;
	private JTextField txtRecipient;
	private JTextField txtMinActivationAmount;
	private JTextField txtAmount;
	private JTextField txtAmountOther;
	private JTextField txtBurstAddress;

	private JButton deployButton;


	public InitiateAcctFrame()
	{
		super(Lang.getInstance().translate("Qora") + " - " + Lang.getInstance().translate("Initiate ACCT"));

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
		cbxGBC.gridwidth = 4;
		cbxGBC.gridx = 1;	

		//TEXTFIELD GBC
		GridBagConstraints txtGBC = new GridBagConstraints();
		txtGBC.insets = new Insets(5,5,5,5);
		txtGBC.fill = GridBagConstraints.HORIZONTAL;  
		txtGBC.anchor = GridBagConstraints.NORTHWEST;
		txtGBC.weightx = 1;	
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
		JLabel fromLabel = new JLabel(Lang.getInstance().translate("Account:"));
		this.add(fromLabel, labelGBC);

		//COMBOBOX FROM
		cbxGBC.gridy = 0;
		this.cbxFrom = new JComboBox<Account>(new AccountsComboBoxModel());
		this.add(this.cbxFrom, cbxGBC);

		//SWAP LABEP
		labelGBC.gridy = 1;
		JLabel swapLabel = new JLabel(Lang.getInstance().translate("Trade:"));
		this.add(swapLabel, labelGBC);
		
		//TXT AMOUNT
		txtGBC.gridy = 1;
		this.txtAmount = new JTextField();
		this.add(this.txtAmount, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 1;
		labelGBC.gridx = 2;
		JLabel qoraLabel = new JLabel(Lang.getInstance().translate("QORA for"));
		this.add(qoraLabel, labelGBC);
		
		//TXT AMOUNT
		txtGBC.gridy = 1;
		txtGBC.gridx = 3;
		this.txtAmountOther = new JTextField();
		this.add(this.txtAmountOther, txtGBC);

		//LABEL NAME
		labelGBC.gridy = 1;
		labelGBC.gridx = 4;
		JLabel otherLabel = new JLabel(Lang.getInstance().translate("BURST"));
		this.add(otherLabel, labelGBC);
		
		//LABEL NAME
		labelGBC.gridy = 2;
		labelGBC.gridx = 0;
		JLabel csPagesLabel = new JLabel(Lang.getInstance().translate("Recipient:"));
		this.add(csPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 2;
		txtGBC.gridx = 1;
		txtGBC.gridwidth = 4;
		this.txtRecipient = new JTextField();
		this.add(this.txtRecipient, txtGBC);
		txtGBC.gridwidth = 1;
		
		//LABEL BURST ADDRESS
		labelGBC.gridy = 3;
		labelGBC.gridx = 0;
		JLabel burstAddressLabel = new JLabel(Lang.getInstance().translate("My BURST address:"));
		this.add(burstAddressLabel, labelGBC);
		
		//TXT LABEL ADDRESS
		txtGBC.gridy = 3;
		txtGBC.gridx = 1;
		txtGBC.gridwidth = 4;
		this.txtBurstAddress = new JTextField();
		this.add(this.txtBurstAddress, txtGBC);
		txtGBC.gridwidth = 1;

		
		//LABEL DATA
		labelGBC.gridy = 4;
		labelGBC.gridx = 0;
		JLabel dataBytesLabel = new JLabel(Lang.getInstance().translate("Password:"));
		this.add(dataBytesLabel, labelGBC);

		//TXTAREA DESCRIPTION
		txtGBC.gridy = 4;
		txtGBC.gridx = 1;
		this.txtPlainSecret = new JPasswordField();
		this.txtPlainSecret.setBorder(this.txtPlainSecret.getBorder());
		this.add(this.txtPlainSecret, txtGBC);

		//LABEL DATA
		labelGBC.gridy = 4;
		labelGBC.gridx = 2;
		JLabel retybePassLabel = new JLabel(Lang.getInstance().translate("Retype password:"));
		this.add(retybePassLabel, labelGBC);

		//TXTAREA DESCRIPTION
		txtGBC.gridy = 4;
		txtGBC.gridx = 3;
		this.txtRetypePass = new JPasswordField();
		this.txtRetypePass.setBorder(this.txtRetypePass.getBorder());
		this.add(this.txtRetypePass, txtGBC);
		
		//PASSWORD ALERT
		labelGBC.gridy = 5;
		labelGBC.gridx = 0;
		labelGBC.gridwidth = 4;
		JLabel passAlert = new JLabel(Lang.getInstance().translate("( DO NOT USE your wallet's password. )"));
		this.add(passAlert, labelGBC);
		
		//LABEL FEE
		labelGBC.gridy = 6;
		labelGBC.gridx = 0;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee:"));
		this.add(feeLabel, labelGBC);

		//TXT FEE
		txtGBC.gridy = 6;
		txtGBC.gridx = 1;
		this.txtFee = new JTextField();
		this.txtFee.setText("20.00000000");
		this.add(this.txtFee, txtGBC);
		
		//LABEL FEES ALERT
		labelGBC.gridx = 2;
		labelGBC.gridwidth = 3;
		JLabel feesAlertLabel = new JLabel(Lang.getInstance().translate("( fees should be at least 20 )"));
		this.add(feesAlertLabel, labelGBC);
		
		//LABEL NAME
		labelGBC.gridy = 7;
		labelGBC.gridx = 0;
		final JLabel dPagesLabel = new JLabel(Lang.getInstance().translate("Expire after:"));
		this.add(dPagesLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 7;
		txtGBC.gridx = 1;
		this.txtExpirationBlocks = new JTextField("200");
		this.add(this.txtExpirationBlocks, txtGBC);
		
		//LABEL BLOCKS
		labelGBC.gridx = 2;
		labelGBC.gridwidth = 3;
		JLabel blocksLabel = new JLabel(Lang.getInstance().translate("blocks ( 1 block approx. %min% min )").replace("%min%", String.valueOf(AT_Constants.getInstance().AVERAGE_BLOCK_MINUTES(DBSet.getInstance().getBlockMap().getLastBlock().getHeight()))));
		this.add(blocksLabel, labelGBC);
		labelGBC.gridwidth = 1;

		//LABEL NAME
		labelGBC.gridy = 8;
		labelGBC.gridx = 0;
		JLabel minActivationAmountLabel = new JLabel(Lang.getInstance().translate("Min activation amount:"));
		this.add(minActivationAmountLabel, labelGBC);

		//TXT NAME
		txtGBC.gridy = 8;
		this.txtMinActivationAmount = new JTextField("20.00000000");
		this.add(this.txtMinActivationAmount, txtGBC);

		
		//BUTTON Register
		buttonGBC.gridy = 9;
		this.deployButton = new JButton(Lang.getInstance().translate("Initiate"));
		this.deployButton.setPreferredSize(new Dimension(80, 25));
		this.deployButton.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				onDeployClick();
			}
		});
		this.add(this.deployButton, buttonGBC);
		

		//PACK
		this.pack();
		this.setResizable(false);
		this.setLocationRelativeTo(null);
		this.setVisible(true);
	}

	public void onDeployClick()
	{
		//DISABLE
		this.deployButton.setEnabled(false);

		//CHECK IF NETWORK OK
		if(Controller.getInstance().getStatus() != Controller.STATUS_OK)
		{
			//NETWORK NOT OK
			JOptionPane.showMessageDialog(null, Lang.getInstance().translate("You are unable to send a transaction while synchronizing or while having no connections!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

			//ENABLE
			this.deployButton.setEnabled(true);

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
				this.deployButton.setEnabled(true);

				return;
			}
		}

		//READ CREATOR
		Account sender = (Account) this.cbxFrom.getSelectedItem();

		long parse = 0;
		try
		{
			//READ FEE
			BigDecimal fee = new BigDecimal(this.txtFee.getText()).setScale(8);

			//READ QUANTITY
			parse = 1;
			BigDecimal quantity = new BigDecimal(this.txtAmount.getText()).setScale(8);
			
			BigDecimal burstQuantity = new BigDecimal(this.txtAmountOther.getText()).setScale(8);
			

			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee must be at least 1!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);

				//ENABLE
				this.deployButton.setEnabled(true);

				return;
			}

			//CREATE POLL
			PrivateKeyAccount creator = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress());


			String code = "3501030900000006040000000900000029302009000000040000000f1ab4000000330403090000003525010a000000260a000000320903350703090000003526010a0000001b0a000000cd322801331601000000003317010100000033180102000000331901030000003505020a0000001b0a000000a1320b033205041e050000001833000509000000320a033203041ab400000033160105000000331701060000003318010700000033190108000000320304320b033203041ab7000000";
			if (code.length()==0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Code is empty!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			ByteBuffer b = ByteBuffer.allocate( 2*4*8 + 8 );
			b.order(ByteOrder.LITTLE_ENDIAN);
			String addr = this.txtRecipient.getText();
			String pas = new String(this.txtPlainSecret.getPassword());

			String pas2 = new String(this.txtRetypePass.getPassword());

			if ( !pas.equals(pas2) )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Passwords do not match"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			if ( pas.length() < 20 )
			{
				int resp = JOptionPane.showConfirmDialog(new JFrame(), Lang.getInstance().translate("Password length too short! Continue?"), Lang.getInstance().translate("ALERT"), JOptionPane.YES_NO_OPTION);
				if ( resp == JOptionPane.NO_OPTION )
				{
					this.deployButton.setEnabled(true);
					return;
				}
			}

			int blocksToEnd = 0;
			try
			{
				blocksToEnd= Integer.parseInt( this.txtExpirationBlocks.getText() );
			}
			catch (Exception e)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Expiration error"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}
			
			if ( blocksToEnd < 20 )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Expiration should be at least 20"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}
			
			byte[] address = new byte[32];
			byte[] empty = new byte[7];
			address= Base58.decode(addr);

			ByteBuffer b1 = ByteBuffer.allocate(32);
			b1.order(ByteOrder.LITTLE_ENDIAN);
			b1.clear();
			b1.put(pas.getBytes(Charset.forName("UTF-8")) );
			MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
			ByteBuffer shab = ByteBuffer.allocate(32);
			shab.order(ByteOrder.LITTLE_ENDIAN);
			shab.put( sha256.digest( b1.array()) );
			shab.clear();
			byte[] has = new byte[32];
			has = shab.array().clone();

			//for (int i=0; i<100000; i++)
			//{
			//	shab.put( sha256.digest( has ) );
			//	has = shab.array();
			//	shab.clear();
			//}
			shab.clear();
			shab.put( sha256.digest( has ) );
			
			byte[] finalHash = new byte[32];
			finalHash = shab.array().clone();
			
			b.put(finalHash);

			b.putInt(0);
			b.putInt(blocksToEnd);
			b.put(address);
			b.put(empty);
			b.clear();

			String data =  Converter.toHex(b.array()).toLowerCase();


			if(data == null)
				data = "";
			if((data.length() & 1) != 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Data error!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			int cpages = (code.length() / 2 / 256) + (((code.length() / 2) % 256 ) != 0 ? 1 : 0);


			int dpages = 1;
			int cspages = 0;
			int uspages = 0;

			if ( dpages < 0 || cspages < 0 || uspages < 0 )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Error in data or cs or us pages!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			byte[] balanceBytes = fee.unscaledValue().toByteArray();
			byte[] fill = new byte[8 - balanceBytes.length];
			balanceBytes = Bytes.concat(fill, balanceBytes);

			long lFee = Longs.fromByteArray(balanceBytes);

			if ( (cpages + dpages + cspages + uspages) * AT_Constants.getInstance().COST_PER_PAGE( DBSet.getInstance().getBlockMap().getLastBlock().getHeight()) > lFee )
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fees should be at least ") + (cpages + dpages + cspages + uspages) * AT_Constants.getInstance().COST_PER_PAGE( DBSet.getInstance().getBlockMap().getLastBlock().getHeight()) + " !", Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			BigDecimal minActivationAmountB = new BigDecimal(this.txtMinActivationAmount.getText()).setScale(8);

			byte[] minActivationAmountBytes = minActivationAmountB.unscaledValue().toByteArray();
			byte[] fillActivation = new byte[8 - minActivationAmountBytes.length];
			minActivationAmountBytes = Bytes.concat(fillActivation, minActivationAmountBytes);

			long minActivationAmount = Longs.fromByteArray(minActivationAmountBytes);

			int creationLength = 4;
			creationLength += 8; //pages
			creationLength += 8; //minActivationAmount
			creationLength += cpages * 256 <= 256 ? 1 : (cpages * 256 <= 32767 ? 2 : 4);
			creationLength += code.length() / 2;

			creationLength += dpages * 256 <= 256 ? 1 : (dpages * 256 <= 32767 ? 2 : 4); // data size
			creationLength += data.length() / 2;

			ByteBuffer creation = ByteBuffer.allocate(creationLength);
			creation.order(ByteOrder.LITTLE_ENDIAN);

			creation.putShort(AT_Constants.getInstance().AT_VERSION( DBSet.getInstance().getBlockMap().getLastBlock().getHeight() ));
			creation.putShort((short)0);
			creation.putShort((short)cpages);
			creation.putShort((short)dpages);
			creation.putShort((short)cspages);
			creation.putShort((short)uspages);
			creation.putLong(minActivationAmount);
			if(cpages * 256 <= 256)
				creation.put((byte)(code.length()/2));
			else if(cpages * 256 <= 32767)
				creation.putShort((short)(code.length()/2));
			else
				creation.putInt(code.length()/2);
			byte[] codeBytes = Converter.parseHexString(code);
			if(codeBytes != null)
				creation.put(codeBytes);
			if(dpages * 256 <= 256)
				creation.put((byte)(data.length()/2));
			else if(dpages * 256 <= 32767)
				creation.putShort((short)(data.length()/2));
			else
				creation.putInt(data.length()/2);
			byte[] dataBytes = Converter.parseHexString(data);
			if(dataBytes != null)
				creation.put(dataBytes);
			byte[] creationBytes = null;
			creationBytes = creation.array();
			
			String name = "QORABURST @ " + quantity.divide(burstQuantity, 2, RoundingMode.HALF_UP);
			String desc = Lang.getInstance().translate("Initiators BURST address: ") + this.txtBurstAddress.getText();
			String type = "acct";
			String tags = "acct,atomic cross chain tx,initiate,initiator";

			Pair<Transaction, Integer> result = Controller.getInstance().deployAT(creator, name, desc , type , tags , creationBytes, quantity, fee);

			//CHECK VALIDATE MESSAGE
			if (result.getB() > 1000)
			{
				JOptionPane.showMessageDialog(new JFrame(), AT_Error.getATError( result.getB() - Transaction.AT_ERROR) , Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				return;
			}

			switch(result.getB())
			{
			case Transaction.VALIDATE_OK:
				
				JPanel successPanel = new JPanel();
				successPanel.setLayout(new GridLayout(2,2));

				//Labels for the textfield components        
				JLabel successLabel = new JLabel(Lang.getInstance().translate("***IMPORTANT*** Use the following key to unlock the counterparty funds:"));
				JTextField txtField = new JTextField(Converter.toHex(has));

				//Add the components to the JPanel        
				successPanel.add(successLabel);
				successPanel.add(txtField);
				JOptionPane.showMessageDialog(null, new JScrollPane(successPanel) , Lang.getInstance().translate("AT has been deployed"), JOptionPane.INFORMATION_MESSAGE);
				this.dispose();
				break;	
			case Transaction.NOT_YET_RELEASED:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("AT will be enabled at !"),  Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.NEGATIVE_FEE:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Fee must be at least 1!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;	
			case Transaction.NEGATIVE_AMOUNT:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Quantity must be at least 0!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.NO_BALANCE:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Not enough balance!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_NAME_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Name must be between 1 and 100 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_TAGS_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Tags length exceeded!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_TYPE_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Type length exceeded!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			case Transaction.INVALID_DESCRIPTION_LENGTH:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Description must be between 1 and 1000 characters!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;
			default:
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown error!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
				break;	
			}
		}
		catch(Exception e)
		{
			if(parse == 0)
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Invalid fee!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), Lang.getInstance().translate("Unknown exception!"), Lang.getInstance().translate("Error"), JOptionPane.ERROR_MESSAGE);
				this.deployButton.setEnabled(true);
			}
			e.printStackTrace();
		}

		//ENABLE
		this.deployButton.setEnabled(true);
	}

}
