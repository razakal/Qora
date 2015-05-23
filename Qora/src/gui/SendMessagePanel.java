package gui;

import gui.models.AccountsComboBoxModel;
import gui.models.AssetsComboBoxModel;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.assets.Asset;
import qora.crypto.AEScrypto;
import qora.crypto.Crypto;
import qora.transaction.MessageTransaction;
import qora.transaction.Transaction;
import qora.wallet.Wallet;
import utils.Converter;
import utils.MenuPopupUtil;
import utils.NameUtils;
import utils.ObserverMessage;
import utils.Pair;
import utils.TableMenuPopupUtil;
import utils.NameUtils.NameResult;
import controller.Controller;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

import javax.swing.border.*;
import javax.swing.table.*;

import org.bouncycastle.crypto.InvalidCipherTextException;
import org.mapdb.Fun.Tuple2;

import database.SortableList;
import database.wallet.TransactionMap;

@SuppressWarnings("serial")

public class SendMessagePanel extends JPanel 
{
	private final String[] columnNames = {"",""};
    private final DefaultTableModel model = new DefaultTableModel(columnNames,0)  {
        @Override public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);
    
    private ArrayList<MessageBuf> messageBufs;	
    
	private JComboBox<Account> cbxFrom;
	private JTextField txtTo;
	private JTextField txtAmount;
	private JTextField txtFee;
	private JTextArea txtMessage;
	private JCheckBox encrypted;
	private JCheckBox isText;
	private JButton sendButton;
	private AccountsComboBoxModel accountsModel;
	private JComboBox<Asset> cbxFavorites;
	private JTextField txtRecDetails;
	
	public SendMessagePanel()
	{
		messageBufs = new ArrayList<MessageBuf>();
		
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 112, 140, 0, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0};
		this.setLayout(gridBagLayout);
		
		//PADDING
		this.setBorder(new EmptyBorder(10, 10, 10, 10));
		
		//COMBOBOX GBC
		GridBagConstraints cbxGBC = new GridBagConstraints();
		cbxGBC.insets = new Insets(5,5,5,5);
		cbxGBC.fill = GridBagConstraints.NONE;  
		cbxGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxGBC.weightx = 0;	
		cbxGBC.gridx = 1;	
		

		
		//FAVORITES GBC
		GridBagConstraints favoritesGBC = new GridBagConstraints();
		favoritesGBC.insets = new Insets(5, 5, 5, 0);
		favoritesGBC.fill = GridBagConstraints.BOTH;  
		favoritesGBC.anchor = GridBagConstraints.NORTHWEST;
		favoritesGBC.weightx = 1;
		favoritesGBC.gridwidth = 5;
		favoritesGBC.gridx = 0;	
		favoritesGBC.gridy = 0;	
		
		//ASSET FAVORITES
		cbxFavorites = new JComboBox<Asset>(new AssetsComboBoxModel());
		this.add(cbxFavorites, favoritesGBC);
		this.accountsModel = new AccountsComboBoxModel();
        
		//ON FAVORITES CHANGE
		cbxFavorites.addActionListener (new ActionListener () {
		    public void actionPerformed(ActionEvent e) {
		    	
		    	Asset asset = ((Asset) cbxFavorites.getSelectedItem());
		    	if(asset != null)
		    	{
		    		//REMOVE ITEMS
			    	cbxFrom.removeAllItems();
			    	
			    	//SET RENDERER
			    	cbxFrom.setRenderer(new AccountRenderer(asset.getKey()));
			    	
			    	//UPDATE MODEL
			    	accountsModel.removeObservers();
			    	accountsModel = new AccountsComboBoxModel();
			    	cbxFrom.setModel(accountsModel);
		    	}
		    }
		});
        
		//LABEL GBC
		GridBagConstraints labelDetailsGBC = new GridBagConstraints();
		labelDetailsGBC.gridy = 3;
		labelDetailsGBC.insets = new Insets(5,5,5,5);
		labelDetailsGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelDetailsGBC.anchor = GridBagConstraints.NORTHWEST;
		labelDetailsGBC.weightx = 0;	
		labelDetailsGBC.gridx = 0;
		
		//LABEL FROM
		GridBagConstraints labelFromGBC = new GridBagConstraints();
		labelFromGBC.insets = new Insets(5, 5, 5, 5);
		labelFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelFromGBC.anchor = GridBagConstraints.NORTHWEST;
		labelFromGBC.weightx = 0;	
		labelFromGBC.gridx = 0;
		labelFromGBC.gridy = 1;
		JLabel fromLabel = new JLabel("From:");
		this.add(fromLabel, labelFromGBC);
		
		//LABEL GBC
		GridBagConstraints cbxFromGBC = new GridBagConstraints();
		cbxFromGBC.gridwidth = 4;
		cbxFromGBC.insets = new Insets(5, 5, 5, 0);
		cbxFromGBC.fill = GridBagConstraints.HORIZONTAL;   
		cbxFromGBC.anchor = GridBagConstraints.NORTHWEST;
		cbxFromGBC.weightx = 0;	
		cbxFromGBC.gridx = 1;
		
		//COMBOBOX FROM
		cbxFromGBC.gridy = 1;
		this.cbxFrom = new JComboBox<Account>(accountsModel);
		this.add(this.cbxFrom, cbxFromGBC);
		
		//LABEL GBC
		GridBagConstraints labelToGBC = new GridBagConstraints();
		labelToGBC.gridy = 2;
		labelToGBC.insets = new Insets(5,5,5,5);
		labelToGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelToGBC.anchor = GridBagConstraints.NORTHWEST;
		labelToGBC.weightx = 0;	
		labelToGBC.gridx = 0;
		JLabel toLabel = new JLabel("To:");
		this.add(toLabel, labelToGBC);
      	
		//LABEL GBC
		GridBagConstraints txtToGBC = new GridBagConstraints();
		txtToGBC.gridwidth = 4;
		txtToGBC.insets = new Insets(5, 5, 5, 0);
		txtToGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtToGBC.anchor = GridBagConstraints.NORTHWEST;
		txtToGBC.weightx = 0;	
		txtToGBC.gridx = 1;
		
      	//TXT TO
		txtToGBC.gridy = 2;
		txtTo = new JTextField();
		this.add(txtTo, txtToGBC);
		
		        txtTo.getDocument().addDocumentListener(new DocumentListener() {
		            
					@Override
					public void changedUpdate(DocumentEvent arg0) {
						// TODO Auto-generated method stub
					}
					@Override
					public void insertUpdate(DocumentEvent arg0) {
						// TODO Auto-generated method stub
						refreshReceiverDetails();
					}
					@Override
					public void removeUpdate(DocumentEvent arg0) {
						// TODO Auto-generated method stub
						refreshReceiverDetails();
					}
		        });
		        
      	//CONTEXT MENU
      	MenuPopupUtil.installContextMenu(txtTo);
      	JLabel recDetailsLabel = new JLabel("Receiver details:");
      	this.add(recDetailsLabel, labelDetailsGBC);
        
		//LABEL GBC
		GridBagConstraints txtReceiverGBC = new GridBagConstraints();
		txtReceiverGBC.gridwidth = 4;
		txtReceiverGBC.insets = new Insets(5, 5, 5, 0);
		txtReceiverGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtReceiverGBC.anchor = GridBagConstraints.NORTHWEST;
		txtReceiverGBC.weightx = 0;	
		txtReceiverGBC.gridx = 1;
		
      	//RECEIVER DETAILS 
      	txtReceiverGBC.gridy = 3;
      	txtRecDetails = new JTextField();
      	txtRecDetails.setEditable(false);
      	this.add(txtRecDetails, txtReceiverGBC);
      	MenuPopupUtil.installContextMenu(txtRecDetails);
        
		//LABEL GBC
		GridBagConstraints labelIsTextGBC = new GridBagConstraints();
		labelIsTextGBC.gridy = 5;
		labelIsTextGBC.insets = new Insets(5,5,5,5);
		labelIsTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelIsTextGBC.anchor = GridBagConstraints.NORTHWEST;
		labelIsTextGBC.weightx = 0;	
		labelIsTextGBC.gridx = 0;
      	
      	//LABEL GBC
      	GridBagConstraints labelMessageGBC = new GridBagConstraints();
      	labelMessageGBC.insets = new Insets(5,5,5,5);
      	labelMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
      	labelMessageGBC.anchor = GridBagConstraints.NORTHWEST;
      	labelMessageGBC.weightx = 0;	
      	labelMessageGBC.gridx = 0;

      	//LABEL MESSAGE
      	labelMessageGBC.gridy = 4;
      	JLabel messageLabel = new JLabel("Message:");
      	this.add(messageLabel, labelMessageGBC);
      	


      	
		//LABEL GBC
		GridBagConstraints txtMessageGBC = new GridBagConstraints();
		txtMessageGBC.gridwidth = 4;
		txtMessageGBC.insets = new Insets(5, 5, 5, 0);
		txtMessageGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtMessageGBC.anchor = GridBagConstraints.NORTHWEST;
		txtMessageGBC.weightx = 0;	
		txtMessageGBC.gridx = 1;
		
        //TXT MESSAGE
        txtMessageGBC.gridy = 4;
        this.txtMessage = new JTextArea();
        this.txtMessage.setRows(4);
      	this.txtMessage.setBorder(this.txtTo.getBorder());
      	
        this.add(txtMessage, txtMessageGBC);
        MenuPopupUtil.installContextMenu(txtMessage);
      	final JLabel isTextLabel = new JLabel("Text Message:");
      	isTextLabel.setHorizontalAlignment(SwingConstants.RIGHT);
      	this.add(isTextLabel, labelIsTextGBC);
      	
      	
		//LABEL GBC
		GridBagConstraints isChkTextGBC = new GridBagConstraints();
		isChkTextGBC.insets = new Insets(5,5,5,5);
		isChkTextGBC.fill = GridBagConstraints.HORIZONTAL;   
		isChkTextGBC.anchor = GridBagConstraints.NORTHWEST;
		isChkTextGBC.weightx = 0;	
		isChkTextGBC.gridx = 0;
		
        //TEXT ISTEXT
		isChkTextGBC.gridx = 1;
		isChkTextGBC.gridy = 5;
        isText = new JCheckBox();
        isText.setSelected(true);
        this.add(isText, isChkTextGBC);
		

		//LABEL GBC
		GridBagConstraints labelEncGBC = new GridBagConstraints();
		labelEncGBC.insets = new Insets(5,5,5,5);
		labelEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		labelEncGBC.anchor = GridBagConstraints.NORTHWEST;
		labelEncGBC.weightx = 0;	
		labelEncGBC.gridx = 4;
		
        //LABEL ENCRYPTED
		labelEncGBC.gridx = 2;
		labelEncGBC.gridy = 5;
		JLabel encLabel = new JLabel("Encrypt Message:");
		encLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(encLabel, labelEncGBC);
		
		//LABEL GBC
		GridBagConstraints ChkEncGBC = new GridBagConstraints();
		ChkEncGBC.insets = new Insets(5,5,5,5);
		ChkEncGBC.fill = GridBagConstraints.HORIZONTAL;   
		ChkEncGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkEncGBC.weightx = 0;	
		ChkEncGBC.gridx = 3;
		
        //ENCRYPTED CHECKBOX
		ChkEncGBC.gridx = 3;
		ChkEncGBC.gridy = 5;
		encrypted = new JCheckBox();
		encrypted.setSelected(true);
		this.add(encrypted, ChkEncGBC);
		
    	//LABEL GBC
		GridBagConstraints amountlabelGBC = new GridBagConstraints();
		amountlabelGBC.insets = new Insets(5,5,5,5);
		amountlabelGBC.fill = GridBagConstraints.HORIZONTAL;   
		amountlabelGBC.anchor = GridBagConstraints.NORTHWEST;
		amountlabelGBC.weightx = 0;	
		amountlabelGBC.gridx = 0;
		
		//LABEL AMOUNT
		amountlabelGBC.gridy = 6;
		final JLabel amountLabel = new JLabel("Amount:");
		amountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		this.add(amountLabel, amountlabelGBC);
		
        
		//LABEL GBC
		GridBagConstraints txtAmountGBC = new GridBagConstraints();
		txtAmountGBC.insets = new Insets(5,5,5,5);
		txtAmountGBC.fill = GridBagConstraints.HORIZONTAL;   
		txtAmountGBC.anchor = GridBagConstraints.NORTHWEST;
		txtAmountGBC.weightx = 0;	
		txtAmountGBC.gridx = 1;
		
      	//TXT AMOUNT
		txtAmountGBC.gridy = 6;
		txtAmount = new JTextField("0.00000000");
		txtAmount.setPreferredSize(new Dimension(130,22));
		this.add(txtAmount, txtAmountGBC);
		MenuPopupUtil.installContextMenu(txtAmount);

		//BUTTON GBC
		GridBagConstraints buttonGBC = new GridBagConstraints();
		buttonGBC.insets = new Insets(5,5,5,5);
		buttonGBC.fill = GridBagConstraints.BOTH;  
		buttonGBC.anchor = GridBagConstraints.PAGE_START;
		buttonGBC.gridx = 0;
		
        //BUTTON SEND
        buttonGBC.gridy = 11;
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 25));
    	sendButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		        onSendClick();
		    }
		});	
		
    	//LABEL GBC
		GridBagConstraints feelabelGBC = new GridBagConstraints();
		feelabelGBC.anchor = GridBagConstraints.EAST;
		feelabelGBC.gridy = 6;
		feelabelGBC.insets = new Insets(5,5,5,5);
		feelabelGBC.fill = GridBagConstraints.BOTH;
		feelabelGBC.weightx = 0;	
		feelabelGBC.gridx = 2;
		final JLabel feeLabel = new JLabel("Fee:");
		feeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		feeLabel.setVerticalAlignment(SwingConstants.TOP);
		this.add(feeLabel, feelabelGBC);
		
		
		//FEETXT GBC
		GridBagConstraints feetxtGBC = new GridBagConstraints();
		feetxtGBC.fill = GridBagConstraints.BOTH;
		feetxtGBC.insets = new Insets(5, 5, 5, 5);
		feetxtGBC.anchor = GridBagConstraints.NORTH;
		feetxtGBC.gridx = 3;	
		
      	//TXT FEE
		feetxtGBC.gridy = 6;
		txtFee = new JTextField();
		txtFee.setText("1.00000000");
		txtFee.setPreferredSize(new Dimension(130,22));
		
		this.add(txtFee, feetxtGBC);
		MenuPopupUtil.installContextMenu(txtFee);
		this.add(sendButton, buttonGBC);
		
	
		
		GridBagConstraints ChkDecryptAllGBC = new GridBagConstraints();
		ChkDecryptAllGBC.insets = new Insets(5,5,5,5);
		ChkDecryptAllGBC.fill = GridBagConstraints.HORIZONTAL;   
		ChkDecryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
		ChkDecryptAllGBC.weightx = 0;	
		ChkDecryptAllGBC.gridx = 3;
		
		//BUTTON DECRYPTALL
		GridBagConstraints decryptAllGBC = new GridBagConstraints();
		decryptAllGBC.insets = new Insets(5,5,5,5);
		decryptAllGBC.fill = GridBagConstraints.HORIZONTAL;  
		decryptAllGBC.anchor = GridBagConstraints.NORTHWEST;
		decryptAllGBC.gridwidth = 1;
		decryptAllGBC.gridx = 3;
		 
        //BUTTON DECRYPTALL
		decryptAllGBC.gridy = 11;
		JButton decryptButton = new JButton("Decrypt All");

		decryptButton.addActionListener(new ActionListener()
		{
		    public void actionPerformed(ActionEvent e)
		    {
		    	for (int i = 0; i < messageBufs.size(); i++) {
		    		CryptoOpenBox(i);
				} 
		    }
		});	
    	this.add(decryptButton, decryptAllGBC);
				
		
		//TEXTFIELD GBC
		GridBagConstraints messagesGBC = new GridBagConstraints();
		messagesGBC.gridx = 0;
		
		messagesGBC.insets = new Insets(5, 5, 0, 5);
		messagesGBC.fill = GridBagConstraints.HORIZONTAL;  
		messagesGBC.anchor = GridBagConstraints.NORTHWEST;
		messagesGBC.weightx = 1;	
		messagesGBC.gridwidth = 5;
		messagesGBC.gridy = 12;
		messagesGBC.gridwidth = 4;   
        
		//super(new BorderLayout());
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
        table.setTableHeader(null);
        table.setRowSelectionAllowed(true);
        //table.setFocusable(false);
       // table.setFillsViewportHeight(true);
        table.setShowVerticalLines(false);
       
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setRowHeight(56);
       // table.setMinimumSize(new Dimension(10, 10));
        for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
            TableColumn c = table.getColumnModel().getColumn(i);
            c.setCellRenderer(new ColumnSpanningCellRenderer());
            c.setMinWidth(50);
        }
        JScrollPane scrollPane = new JScrollPane(table);
        // scrollPane.setMaximumSize(new Dimension(1000, 1000));
        scrollPane.setPreferredSize(new Dimension(100, 100));
        scrollPane.setWheelScrollingEnabled(true);

        table.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent e) 
				{
					if(e.getClickCount() == 2) 
					{
						Point p = e.getPoint();
					    int row = table.rowAtPoint(p);
					    //table.setRowSelectionInterval(row, row);
					    
					    CryptoOpenBox(row);	
						
				    }
				}
			});
        
        //BOTTOM GBC
		GridBagConstraints bottomGBC = new GridBagConstraints();
		bottomGBC.insets = new Insets(5,5,5,5);
		bottomGBC.fill = GridBagConstraints.BOTH;   
		bottomGBC.anchor = GridBagConstraints.NORTHWEST;
		bottomGBC.weightx = 0;	
		bottomGBC.gridx = 0;
		
        //ADD BOTTOM SO IT PUSHES TO TOP
		bottomGBC.gridy = 13;
		bottomGBC.weighty = 4;
		bottomGBC.gridwidth = 5;
		
        add(scrollPane, bottomGBC);
        
		//MENU
		JPopupMenu menu = new JPopupMenu();	

		JMenuItem copyMessage = new JMenuItem("Copy message");
		copyMessage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getDecrMessage());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyMessage);
		
		JMenuItem copySender = new JMenuItem("Copy sender address");
		copySender.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getFrom());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copySender);
				
		JMenuItem copyRecipient = new JMenuItem("Copy recipient address");
		copyRecipient.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				int row = table.getSelectedRow();
				row = table.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getTo());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyRecipient);
		
		TableMenuPopupUtil.installContextMenu(table, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
	}
	
	public void cryptoCloseAll()
	{
		for (MessageBuf messageBuf : messageBufs) {
			messageBuf.setOpend(false);
			messageBuf.setDecrMessage("");
		}
	}
	
	private void CryptoOpenBox(int row)
	{
		if(messageBufs.get(row).getEncrypted())
		{
			if(!messageBufs.get(row).getOpend())
			{
				if(!Controller.getInstance().isWalletUnlocked())
        		{
        			//ASK FOR PASSWORD
        			String password = PasswordPane.showUnlockWalletDialog(); 
        			if(!Controller.getInstance().unlockWallet(password))
        			{
        				//WRONG PASSWORD
        				JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
        				
        				return;
        			}
        		}

        		Account account = Controller.getInstance().getAccountByAddress(messageBufs.get(row).getFrom());	
        		
        		byte[] privateKey = null; 
        		byte[] publicKey = null;
        		//IF SENDER ANOTHER
        		if(account == null)
        		{
            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageBufs.get(row).getTo());
    				privateKey = accountRecipient.getPrivateKey();		
    				
    				publicKey = messageBufs.get(row).getFromPublicKey();    				
        		}
        		//IF SENDER ME
        		else
        		{
            		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
    				privateKey = accountRecipient.getPrivateKey();		
    				
    				if(messageBufs.get(row).getToPublicKey() == null)
    				{
    					messageBufs.get(row).setToPublicKey(Controller.getInstance().getPublicKeyFromAddress( messageBufs.get(row).getTo()));
    				}
    				publicKey = messageBufs.get(row).getToPublicKey();    				
        		}
        		
        		String decrypt = null;
        		
        		try {
        			try {
						decrypt = new String(AEScrypto.dataDecrypt(messageBufs.get(row).getMessage(), privateKey, publicKey), "UTF-8");
					} catch (InvalidCipherTextException e1) {
						// TODO Auto-generated catch block
						messageBufs.get(row).setDecrMessage("Decrypt Error!");
					} catch (NullPointerException  e1) {
						messageBufs.get(row).setDecrMessage("Decrypt Error!");
					} 
				} catch (UnsupportedEncodingException e1) {
					// TODO Auto-generated catch block
					messageBufs.get(row).setDecrMessage("Error!");
				}
        		
        		if(decrypt == null)
        		{
        			messageBufs.get(row).setDecrMessage("Decrypt Error!");			        			
        		}
        		else
        		{
        			messageBufs.get(row).setDecrMessage(decrypt);
        			messageBufs.get(row).setOpend(true);
        		}
			}
			else
			{
				messageBufs.get(row).setDecrMessage("");
				messageBufs.get(row).setOpend(false);
				
			}
		}	
	} 
	
	
	private void refreshReceiverDetails()
	{
		String toValue = txtTo.getText();
		
		if(toValue.isEmpty())
		{
			txtRecDetails.setText("");
			return;
		}
		
		if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			txtRecDetails.setText("Status must be OK to show receiver details.");
			return;
		}
		
		//CHECK IF RECIPIENT IS VALID ADDRESS
		if(!Crypto.getInstance().isValidAddress(toValue))
		{
			Pair<Account, NameResult> nameToAdress = NameUtils.nameToAdress(toValue);
					
			if(nameToAdress.getB() == NameResult.OK)
			{
				Account account = nameToAdress.getA();
				txtRecDetails.setText(account.getBalance(1).toPlainString() + " - " + account.getAddress());
			}
			else
			{
				txtRecDetails.setText(nameToAdress.getB().getShortStatusMessage());
			}
		}else
		{
			Account account = new Account(toValue);
			txtRecDetails.setText(account.getBalance(1).toPlainString() + " - " + account.getAddress());
		}	
	}
	
	public void onSendClick()
	{
		//DISABLE
		this.sendButton.setEnabled(false);
		
		//TODO TEST
		//CHECK IF NETWORK OKE
		/*if(Controller.getInstance().getStatus() != Controller.STATUS_OKE)
		{
			//NETWORK NOT OKE
			JOptionPane.showMessageDialog(null, "You are unable to send a transaction while synchronizing or while having no connections!", "Error", JOptionPane.ERROR_MESSAGE);
			
			//ENABLE
			this.sendButton.setEnabled(true);
			
			return;
		}*/
		
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
				this.sendButton.setEnabled(true);
				
				return;
			}
		}
		
		//READ SENDER
		Account sender = (Account) cbxFrom.getSelectedItem();
		
		
		//READ RECIPIENT
		String recipientAddress = txtTo.getText();
		
		Account recipient;
		
		//ORDINARY RECIPIENT
		if(Crypto.getInstance().isValidAddress(recipientAddress))
		{
			recipient = new Account(recipientAddress);
		//NAME RECIPIENT
		}else
		{
			Pair<Account, NameResult> result = NameUtils.nameToAdress(recipientAddress);
			
			if(result.getB() == NameResult.OK)
			{
				recipient = result.getA();
			}
			else		
			{
				JOptionPane.showMessageDialog(null, result.getB().getShortStatusMessage() , "Error", JOptionPane.ERROR_MESSAGE);
		
				//ENABLE
				this.sendButton.setEnabled(true);
			
				return;
			}
		}
		
		int parsing = 0;
		try
		{
			//READ AMOUNT
			parsing = 1;
			BigDecimal amount = new BigDecimal(txtAmount.getText()).setScale(8);
			
			//READ FEE
			parsing = 2;
			BigDecimal fee = new BigDecimal(txtFee.getText()).setScale(8);
			
			//CHECK MIMIMUM FEE
			if(fee.compareTo(Transaction.MINIMUM_FEE) == -1)
			{
				JOptionPane.showMessageDialog(new JFrame(), "Fee must be at least 1!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}
			
			String message = txtMessage.getText();
			
			boolean isTextB = isText.isSelected();
			
			byte[] messageBytes;
			
			if ( isTextB )
			{
				messageBytes = message.getBytes( Charset.forName("UTF-8") );
			}
			else
			{
				try
				{
					messageBytes = Converter.parseHexString( message );
				}
				catch (Exception e)
				{
					JOptionPane.showMessageDialog(new JFrame(), "Message format is not hex!", "Error", JOptionPane.ERROR_MESSAGE);
					
					//ENABLE
					this.sendButton.setEnabled(true);
					
					return;
				}
			}
			if ( messageBytes.length < 1 || messageBytes.length > 4000 )
			{
				JOptionPane.showMessageDialog(new JFrame(), "Message size exceeded!", "Error", JOptionPane.ERROR_MESSAGE);
				
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}
			
			boolean encryptMessage = encrypted.isSelected();
		
			byte[] encrypted = (encryptMessage)?new byte[]{1}:new byte[]{0};
			byte[] isTextByte = (isTextB)? new byte[] {1}:new byte[]{0};
			
			//CHECK IF PAYMENT OR ASSET TRANSFER
			Asset asset = (Asset) this.cbxFavorites.getSelectedItem();
			Pair<Transaction, Integer> result;
			if(asset.getKey() == 0l)
			{

				if(encryptMessage)
				{
					//sender
					PrivateKeyAccount account = Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress().toString());
					byte[] privateKey = account.getPrivateKey();		

					//recipient
					byte[] publicKey = Controller.getInstance().getPublicKeyFromAddress(recipient.getAddress());
					if(publicKey == null)
					{
						JOptionPane.showMessageDialog(new JFrame(), "The recipient has not yet performed any action in the blockchain.\nTo send an encrypted message to him impossible.", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					messageBytes = AEScrypto.dataEncrypt(messageBytes, privateKey, publicKey);
				}
				
				//CREATE PAYMENT
				result = Controller.getInstance().sendMessage(Controller.getInstance().getPrivateKeyAccountByAddress(sender.getAddress()), recipient, amount, fee, messageBytes, isTextByte, encrypted);
			}
			else
			{
				JOptionPane.showMessageDialog(new JFrame(), "Feature not available", "Error", JOptionPane.ERROR_MESSAGE);
				//ENABLE
				this.sendButton.setEnabled(true);
				
				return;
			}
			
			//CHECK VALIDATE MESSAGE
			switch(result.getB())
			{
			case Transaction.VALIDATE_OKE:
				
				//RESET FIELDS
				if(!this.txtAmount.equals("0.00000000"))
				{
					this.txtAmount.setText("0.00000000");
				}
				
				this.txtMessage.setText("");
				
				if(amount.compareTo(BigDecimal.ZERO) > 0)
				{
					JOptionPane.showMessageDialog(new JFrame(), "Message with payment has been sent!", "Success", JOptionPane.INFORMATION_MESSAGE);
				}
				break;	
			
			case Transaction.INVALID_ADDRESS:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid address!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case Transaction.NEGATIVE_AMOUNT:
				
				JOptionPane.showMessageDialog(new JFrame(), "Amount must be positive!", "Error", JOptionPane.ERROR_MESSAGE);
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
			//CHECK WHERE PARSING ERROR HAPPENED
			switch(parsing)
			{
			case 1:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid amount!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
				
			case 2:
				
				JOptionPane.showMessageDialog(new JFrame(), "Invalid fee!", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
		}
		
		//ENABLE
		this.sendButton.setEnabled(true);
	}
	
	class ColumnSpanningCellRenderer extends JPanel implements TableCellRenderer, Observer {
	    private final JTextArea textArea = new JTextArea(2, 999999);
	    private final JLabel label = new JLabel();
	    private final JLabel iconLabel = new JLabel();
	    private final JLabel iconCryptoLabel = new JLabel();
	    private final JScrollPane scroll = new JScrollPane();
	    
	    
		Icon iconSend;
		Icon iconReceive;
		Icon iconLock;
		Icon iconUnlock;
		
	    public ColumnSpanningCellRenderer() {
	    	super(new BorderLayout(0, 0));
	    	
	    	Controller.getInstance().addWalletListener(this);
	    	
	    	iconSend = new ImageIcon("images/send.png");
	    	iconReceive = new ImageIcon("images/receive.png");
	    	
	    	BufferedImage buff;
			try {
				buff = ImageIO.read(new File("images/wallet/locked.png"));
				iconLock = new ImageIcon(buff.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
				
				buff = ImageIO.read(new File("images/wallet/unlocked.png"));
				iconUnlock = new ImageIcon(buff.getScaledInstance(20, 16, Image.SCALE_SMOOTH));
					
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	   
	        scroll.setViewportView(textArea);
	        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
	        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
	        scroll.setBorder(BorderFactory.createEmptyBorder());
	        scroll.setViewportBorder(BorderFactory.createEmptyBorder());
	        scroll.setOpaque(false);
	        scroll.getViewport().setOpaque(false);

	        textArea.setBorder(BorderFactory.createEmptyBorder());
	        textArea.setMargin(new Insets(0, 0, 0, 0));
	        textArea.setForeground(Color.black);
	        textArea.setEditable(false);
	        textArea.setFocusable(false);
	        textArea.setOpaque(false);
	        textArea.setFont(label.getFont());

	        iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
	        iconLabel.setOpaque(false);

	        iconCryptoLabel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
	
	        
	        Border b1 = BorderFactory.createEmptyBorder(2, 2, 2, 2);
	        Border b2 = BorderFactory.createMatteBorder(0, 0, 1, 1, Color.GRAY);
	        label.setBorder(BorderFactory.createCompoundBorder(b2, b1));

	        setBackground(textArea.getBackground());
	        setOpaque(true);
	        add(label, BorderLayout.NORTH);
	        add(scroll);
	    }
	    

	    @Override public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
	        Account account = Controller.getInstance().getAccountByAddress( messageBufs.get(row).getFrom());
	         
	    	if(column == 0)
	    	{
	    		add(iconLabel, BorderLayout.WEST);
	 	       	if(account != null)
	 	       	{
		    	   iconLabel.setIcon(iconSend);
	 	       	}
	 	       	else
	 	       	{
		    	   iconLabel.setIcon(iconReceive);
	 	       	}
	    	}
	    	
	    	if(column == 0)
	    	{
	    		label.setHorizontalAlignment(SwingConstants.LEFT);
	    		label.setText("<html><P ALIGN=LEFT>From:"+messageBufs.get(row).getFrom()
	    				+"<br>To:" + messageBufs.get(row).getTo() + "</P></html>");
	    	}
	    	else
	    	{		
	    		Date date = new Date(messageBufs.get(row).getTime());
				DateFormat format = DateFormat.getDateTimeInstance();
			
				label.setHorizontalAlignment(SwingConstants.RIGHT);
	       		label.setText("<html><P ALIGN=RIGHT >"+format.format(date)
	    				+"<br>Amount: " +  messageBufs.get(row).getAmount().toPlainString()+" Fee: "+
	    				messageBufs.get(row).getFee().toPlainString()+"</P></html>");
	  	
	    	}
	    
	    	if(messageBufs.get(row).getEncrypted())
	    	{
	    		if(messageBufs.get(row).getOpend())
	    		{
	    			textArea.setForeground(Color.blue);
	    		}
	    		else
	    		{
	    			textArea.setForeground(Color.red);
	    		}
	    	}
	    	else
	    	{
	    		textArea.setForeground(Color.black);
	    	}
    		textArea.setText( messageBufs.get(row).getDecrMessage());
	    		
	    	
			
			int fontHeight = textArea.getFontMetrics(textArea.getFont()).getHeight();
			int textHeight = (3+textArea.getLineCount())*fontHeight;
	
			if(textHeight< iconSend.getIconHeight() + 4*fontHeight)
			{
				textHeight = iconSend.getIconHeight() + 4*fontHeight;
			}
			
			table.setRowHeight(row, textHeight);
	        ///iconLabel.setIcon(test.icon);
	        
			if(column == 1)
			{
				add(iconCryptoLabel, BorderLayout.EAST);
	 	       	if(messageBufs.get(row).getEncrypted() && !messageBufs.get(row).getOpend())
	 	       	{
	 	       		iconCryptoLabel.setIcon(iconLock);
	 	       	}
	 	       	else
	 	       	{
	 	       		iconCryptoLabel.setIcon(iconUnlock);
	 	       	}
			}
			
	        Rectangle cr = table.getCellRect(row, column, false);
	        if (column != 0) {
	            cr.x -= iconLabel.getPreferredSize().width;
	        }
	        scroll.getViewport().setViewPosition(cr.getLocation());

	        if (isSelected) {
	            setBackground(Color.ORANGE);
	        } else {
	            setBackground(Color.WHITE);
	        }
	        return this;
	    }
		@Override
		public void update(Observable o, Object arg) 
		{
			try
			{
				this.syncUpdate(o, arg);
			}
			catch(Exception e)
			{
				//GUI ERROR
			}
		}
		
		@SuppressWarnings("unchecked")
		public synchronized void syncUpdate(Observable o, Object arg)
		{
			SortableList<Tuple2<String, String>, Transaction> transactions = null;
			ObserverMessage message = (ObserverMessage) arg;
			
			if(message.getType() == ObserverMessage.WALLET_STATUS)
			{
				int status = (int) message.getValue();
				
				if(status == Wallet.STATUS_LOCKED)
				{
					cryptoCloseAll();
				}
			}
				
			//CHECK IF NEW LIST
			if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
			{
				if(transactions == null)
				{
					transactions = (SortableList<Tuple2<String, String>, Transaction>) message.getValue();
					transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
				}
				boolean is = false;
				
				boolean added = false; 
				
				for (int i = transactions.size()-1; i >= 0; i--) {
					if(transactions.get(i).getB().getType() == Transaction.MESSAGE_TRANSACTION)
					{
						MessageTransaction transaction = (MessageTransaction)transactions.get(i).getB();
						
						is = false;
						
						for (int j = 0; j < messageBufs.size(); j++) 
						{			
							if(messageBufs.get(j).getSign().equals(transaction.getSignature()))
							{
								is = true;
							}
						}
						
						if(! is)
						{
							messageBufs.add(0, new MessageBuf(
								transaction.getData(), 
								transaction.isEncrypted(),
								transaction.getSender().getAddress(),
								transaction.getRecipient().getAddress(),
								transaction.getTimestamp(),
								transaction.getAmount(),
								transaction.getFee(),
								transaction.getSignature(),
								transaction.getCreator().getPublicKey()
							));
							added = true;
						}
					}
				}

				((DefaultTableModel) table.getModel()).setRowCount(messageBufs.size());
				
				if(added && messageBufs.get(1).getOpend())
				{
					if(Controller.getInstance().isWalletUnlocked())
					{
						CryptoOpenBox(0);
					}
				}

				this.repaint();
			}
		}
	}
	
	public class MessageBuf
	{
		private byte[] message;
		private String decrMessage;
		private boolean encrypted;
		private boolean opened;
		private String from;
		private byte[] fromPublicKey;
		private String to;
		private byte[] toPublicKey;
		private long time;
		private BigDecimal amount;
		private BigDecimal fee;
		private byte[] sign;
		
		public MessageBuf(byte[] message, boolean encrypted, String from, String to, long time, BigDecimal amount, BigDecimal fee, byte[] sign, byte[] fromPublicKey)
		{
			this.message = message;
			this.encrypted = encrypted;	
			this.decrMessage = "";
			this.opened = false;
			this.from = from;
			this.to = to;
			this.time = time;
			this.amount = amount;
			this.fee = fee;
			this.fromPublicKey = fromPublicKey;
			this.toPublicKey = null;
			this.sign = sign;
		}

		public byte[] getMessage()
		{
			return this.message;
		}
		public boolean getEncrypted()
		{
			return this.encrypted;
		}
		public String getDecrMessage()
		{
			if(decrMessage.equals(""))
			{
				if(this.encrypted && !this.opened)
				{
					this.decrMessage = "Encrypted";
				}
				if(!this.encrypted)
				{
					try {
						this.decrMessage = new String(message, "UTF-8");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return this.decrMessage;
		}
		public String getFrom()
		{
			return this.from;
		}
		public String getTo()
		{
			return this.to;
		}
		public BigDecimal getFee()
		{
			return this.fee;
		}
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		public byte[] getSign()
		{
			return this.sign;
		}
		public byte[] getFromPublicKey()
		{
			return this.fromPublicKey;
		}
		public byte[] getToPublicKey()
		{
			return this.toPublicKey;
		}
		public void setToPublicKey(byte[] toPublicKey)
		{
			this.toPublicKey = toPublicKey;
		}
		public long getTime()
		{
			return this.time;
		}
		public boolean getOpend()
		{
			return this.opened;
		}
		public void setOpend(boolean opened)
		{
			this.opened = opened;
		}
		public void setDecrMessage(String decrMessage)
		{
			this.decrMessage = decrMessage;
		}
	}
	
	
}


