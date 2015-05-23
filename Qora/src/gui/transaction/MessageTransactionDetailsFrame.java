package gui.transaction;

import gui.PasswordPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.*;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.crypto.InvalidCipherTextException;

import controller.Controller;
import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.crypto.Base58;
import qora.transaction.MessageTransaction;
import utils.Converter;

@SuppressWarnings("serial")
public class MessageTransactionDetailsFrame extends JFrame
{
	private JTextField service;
	
	public MessageTransactionDetailsFrame(final MessageTransaction messageTransaction)
	{
		super("Qora - Transaction Details");
		
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
		detailGBC.gridwidth = 3;
		detailGBC.gridx = 1;		
		
		//LABEL TYPE
		labelGBC.gridy = 0;
		
		JLabel typeLabel = new JLabel("Type:");
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = 0;
		JLabel type = new JLabel("Message Transaction");
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = 1;
		JLabel signatureLabel = new JLabel("Signature:");
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = 1;
		JTextField signature = new JTextField(Base58.encode(messageTransaction.getSignature()));
		signature.setEditable(false);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		labelGBC.gridy = 2;
		JLabel referenceLabel = new JLabel("Reference:");
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = 2;
		JTextField reference = new JTextField(Base58.encode(messageTransaction.getReference()));
		reference.setEditable(false);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		labelGBC.gridy = 3;
		JLabel timestampLabel = new JLabel("Timestamp:");
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = 3;
		Date date = new Date(messageTransaction.getTimestamp());
		DateFormat format = DateFormat.getDateTimeInstance();
		JLabel timestamp = new JLabel(format.format(date));
		this.add(timestamp, detailGBC);
		
		//LABEL SENDER
		labelGBC.gridy = 4;
		JLabel senderLabel = new JLabel("Creator:");
		this.add(senderLabel, labelGBC);
		
		//SENDER
		detailGBC.gridy = 4;
		JTextField sender = new JTextField(messageTransaction.getCreator().getAddress());
		sender.setEditable(false);
		this.add(sender, detailGBC);
		
		//LABEL SERVICE
		labelGBC.gridy = 5;
		JLabel serviceLabel = new JLabel("Message:");
		this.add(serviceLabel, labelGBC);
		
		//SERVICE
		detailGBC.gridy = 5;
		detailGBC.gridwidth = 2;
		service = new JTextField( ( messageTransaction.isText() ) ? new String(messageTransaction.getData(), Charset.forName("UTF-8")) : Converter.toHex(messageTransaction.getData()));
		service.setEditable(false);
		this.add(service, detailGBC);			
		detailGBC.gridwidth = 3;
		//ENCRYPTED CHECKBOX
		
		//TEXTFIELD GBC
		GridBagConstraints chcGBC = new GridBagConstraints();
		chcGBC.insets = new Insets(5,5,5,5);
		chcGBC.fill = GridBagConstraints.HORIZONTAL;  
		chcGBC.anchor = GridBagConstraints.NORTHWEST;

		chcGBC.gridy = 5;
		chcGBC.gridx = 3;
        final JCheckBox encrypted = new JCheckBox("Encrypted");
        
        encrypted.setSelected(messageTransaction.isEncrypted());
        encrypted.setEnabled(messageTransaction.isEncrypted());
        
        this.add(encrypted, chcGBC);
        
        encrypted.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		if(!encrypted.isSelected())
        		{
	        		if(!Controller.getInstance().isWalletUnlocked())
	        		{
	        			//ASK FOR PASSWORD
	        			String password = PasswordPane.showUnlockWalletDialog(); 
	        			if(!Controller.getInstance().unlockWallet(password))
	        			{
	        				//WRONG PASSWORD
	        				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
	        				
	        				encrypted.setSelected(!encrypted.isSelected());
	        				
	        				return;
	        			}
	        		}
	
	        		Account account = Controller.getInstance().getAccountByAddress(messageTransaction.getSender().getAddress());	
	        		
	        		byte[] privateKey = null; 
	        		byte[] publicKey = null;
	        		//IF SENDER ANOTHER
	        		if(account == null)
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageTransaction.getRecipient().getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = messageTransaction.getCreator().getPublicKey();    				
	        		}
	        		//IF SENDER ME
	        		else
	        		{
	            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
	    				privateKey = accountRecipient.getPrivateKey();		
	    				
	    				publicKey = Controller.getInstance().getPublicKeyFromAddress(messageTransaction.getRecipient().getAddress());    				
	        		}
	        		
	        		try {
						service.setText(new String(AEScrypto.dataDecrypt(messageTransaction.getData(), privateKey, publicKey), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (InvalidCipherTextException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		else
        		{
        			try {
						service.setText(new String(messageTransaction.getData(), "UTF-8"));
					} catch (UnsupportedEncodingException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
        		}
        		//encrypted.isSelected();
        		
        	}
        });	  
        
		//LABEL FEE
		labelGBC.gridy = 6;
		JLabel feeLabel = new JLabel("Fee:");
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = 6;
		JTextField fee = new JTextField(messageTransaction.getFee().toPlainString());
		fee.setEditable(false);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		labelGBC.gridy = 7;
		JLabel confirmationsLabel = new JLabel("Confirmations:");
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = 7;
		JLabel confirmations = new JLabel(String.valueOf(messageTransaction.getConfirmations()));
		this.add(confirmations, detailGBC);	
		           
        //PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
