package gui.models;

import gui.PasswordPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import org.bouncycastle.crypto.InvalidCipherTextException;

import qora.account.Account;
import qora.account.PrivateKeyAccount;
import qora.crypto.AEScrypto;
import qora.transaction.MessageTransaction;
import qora.transaction.Transaction;
import qora.wallet.Wallet;
import utils.Converter;
import utils.ObserverMessage;
import utils.TableMenuPopupUtil;
import controller.Controller;
import database.DBSet;
import database.SortableList;
import database.wallet.TransactionMap;

@SuppressWarnings("serial")
public class MessagesTableModel extends JTable implements Observer{
	private ArrayList<MessageBuf> messageBufs;	
	SortableList<byte[], Transaction> transactions;
	JMenuItem menuDecrypt;
	private DefaultTableModel messagesModel;
	int width;
	int fontHeight;
	
	public MessagesTableModel()
	{
		this.setShowGrid(false);

		fontHeight = this.getFontMetrics(this.getFont()).getHeight();

		messageBufs = new ArrayList<MessageBuf>();
		messagesModel = new DefaultTableModel();
		this.setModel(messagesModel);
		messagesModel.addColumn("");
		
		DefaultTableCellRenderer topRenderer = new DefaultTableCellRenderer();
		topRenderer.setVerticalAlignment(DefaultTableCellRenderer.TOP);
		this.getColumn("").setCellRenderer( topRenderer );
		
		Controller.getInstance().addMessagesObserver(this);

		//MENU
		JPopupMenu menu = new JPopupMenu();	

		JMenuItem copyMessage = new JMenuItem("Copy Message");
		copyMessage.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);

				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getDecrMessage());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copyMessage);
		
		JMenuItem copySender = new JMenuItem("Copy Sender Address");
		copySender.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getSender());
			    clipboard.setContents(value, null);
			}
		});
		menu.add(copySender);
				
		JMenuItem copyRecipient = new JMenuItem("Copy Recipient Address");
		copyRecipient.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
				StringSelection value = new StringSelection(messageBufs.get(row).getRecipient());
			    clipboard.setContents(value, null);
			}
		});
		
		menu.add(copyRecipient);
		
		menuDecrypt = new JMenuItem("Decrypt");
		menuDecrypt.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e) 
			{
				JMenuItem menuItem = (JMenuItem) e.getSource();
		        JPopupMenu popupMenu = (JPopupMenu) menuItem.getParent();
		        Component invoker = popupMenu.getInvoker(); //this is the JMenu (in my code)
		        MessagesTableModel invokerAsJComponent = (MessagesTableModel) invoker;
		        
				int row = invokerAsJComponent.getSelectedRow();
				row = invokerAsJComponent.convertRowIndexToModel(row);
				
				CryptoOpenBox(row, 0);
				
				invokerAsJComponent.repaint();
			}
	
		});
		
		menu.add(menuDecrypt);
		
		TableMenuPopupUtil.installContextMenu(this, menu);  // SELECT ROW ON WHICH CLICKED RIGHT BUTTON
		
        this.addMouseListener(new MouseAdapter() 
			{
				public void mouseClicked(MouseEvent e) 
				{
					if(e.getClickCount() == 2) 
					{
						MessagesTableModel tableModelparent = (MessagesTableModel) e.getSource();
						
				        Point p = e.getPoint();
					    int row = tableModelparent.rowAtPoint(p);
					    
					    CryptoOpenBox(row, 0);	
					    tableModelparent.repaint();
				    }
				}
			});
        
        
        this.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				ListSelectionModel lsm = (ListSelectionModel)e.getSource();
				int row = lsm.getMinSelectionIndex();

				if(row > -1)
				{
					if(!messageBufs.get(row).getEncrypted())
					{
						menuDecrypt.setVisible(false);
					}
					else
					{
						menuDecrypt.setVisible(true);
					}	
					if(messageBufs.get(row).getOpend())
					{
						menuDecrypt.setText("Hide decrypted");
					}
					else
					{
						menuDecrypt.setText("Decrypt");
					}
				}
			}
		});

	}

	@Override
	public Component prepareRenderer(TableCellRenderer renderer, int row, int column)
	{
		Object value = getValueAt(row, column);
		
		boolean isSelected = false;
		boolean hasFocus = false;
		
		// Only indicate the selection and focused cell if not printing
		if (!isPaintingForPrint()) {
			isSelected = isCellSelected(row, column);
		
			boolean rowIsLead = (selectionModel.getLeadSelectionIndex() == row);
			boolean colIsLead = (columnModel.getSelectionModel().getLeadSelectionIndex() == column);
		
			hasFocus = (rowIsLead && colIsLead) && isFocusOwner();
		}
		JComponent cellRenderer = (JComponent) renderer.getTableCellRendererComponent(this, value, isSelected,
		hasFocus, row, column);
		
		if (isSelected && hasFocus) {
			cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(102, 167, 232, 255), 1));
		} else {
			cellRenderer.setBorder(BorderFactory.createLineBorder(new Color(205, 205, 205, 255), 1));
		}
		return cellRenderer;
	}
   
	@Override
	public boolean isCellEditable(int row, int column) {
		//all cells false
	    return false;
	}
	 

	@Override
	public Object getValueAt(int row, int column)
	{
		Date date = new Date(messageBufs.get(row).getTimestamp());
		DateFormat format = DateFormat.getDateTimeInstance();
		
		Account account = Controller.getInstance().getAccountByAddress( messageBufs.get(row).getSender());
		String imginout = "";
		if(account != null)
	    {
			imginout = "file:images/messages/receive.png";
		}
		else
		{
			imginout = "file:images/messages/send.png";
	    }
		
		String imglock = "";
		
		if(messageBufs.get(row).getOpend())
		{
			imglock = "file:images/messages/unlocked.png";
		}
		else
		{	
			imglock = "file:images/messages/locked.png";
		}
		
		if(messageBufs.get(row).getEncrypted())
		{	
			if(messageBufs.get(row).getOpend())
			{
				imglock = "file:images/messages/unlocked.png";
			}
			else
			{	
				imglock = "file:images/messages/locked.png";
			}
		}
		else
		{
			imglock = "file:images/messages/unlockedred.png";
		}
	
		String confirmations = Integer.toString(messageBufs.get(row).getConfirmations());
		
		if(messageBufs.get(row).getConfirmations()<1)
		{
			confirmations = "<font color=red>" + confirmations +"</font>";
		}
		
		String colorHeader = "F0F0F0";
		String colorTextHeader = "000000";
		String colorTextMessage = "000000";
		String colorTextBackground = "FFFFFF";
		
		if(this.getSelectedRow() == row)
		{
			colorHeader = "C4DAEF";
			colorTextBackground = "D1E8FF";
		}
		
		if(messageBufs.get(row).getEncrypted())
		{
			if(messageBufs.get(row).getOpend())
			{
				colorTextMessage = "0000FF";
			}
			else
			{
				colorTextMessage = "FF0000";
			}	
		}
		
		String decrMessage = messageBufs.get(row).getDecrMessage(); 
		decrMessage = decrMessage.replace("<","&lt;");
		decrMessage = decrMessage.replace(">","&gt;");
		decrMessage = decrMessage.replace("\n","<br>");

		
		String text =	"<html>"
						+ "<body width='"+this.getWidth()+"'>"
						+ "<table border='0' cellpadding='3' cellspacing='0'><tr><td bgcolor='"+colorHeader+"' width='"+this.getWidth()/2+"'>"
						+ "<font size='2' color='"+colorTextHeader+"'>From:"+messageBufs.get(row).getSender()
						+ "<br>To:"
						+ messageBufs.get(row).getRecipient()+"</font></td>"
						+ "<td bgcolor='"+colorHeader+"' align='right' width='"+this.getWidth()/2+"'>"
						+ "<font color='"+colorTextHeader+"'>"+ confirmations + " . "
						+ format.format(date)+"<br>"
						+ "Amount: " +  messageBufs.get(row).getAmount().toPlainString()+" Fee: "
						+ messageBufs.get(row).getFee().toPlainString()
						+ "</font></td></tr></table>"
						+ "<table border='0' cellpadding='3' cellspacing='0'><tr bgcolor='"+colorTextBackground+"'><td width='25'><img src='"+imginout+"'>"
						+ "<td width='"+(this.getWidth())+"'>"
						+ "<font color='"+colorTextMessage+"'>"
						+ decrMessage
						+ "</font>"
						+ "<td width='30'><img src='"+imglock+"'>"
						+ "</td></tr></table></body></html>";

		return text;
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

		ObserverMessage message = (ObserverMessage) arg;
		if(message.getType() == ObserverMessage.WALLET_STATUS)
		{
			int status = (int) message.getValue();
			
			if(status == Wallet.STATUS_LOCKED)
			{
				cryptoCloseAll();
			}
		}
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE || message.getType() == ObserverMessage.REMOVE_BLOCK_TYPE
				|| message.getType() == ObserverMessage.LIST_BLOCK_TYPE)
		{
			new Thread()
			{
				public void run() 
				{
					try {
						sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					updateBlock();
				}
			}.start();
		}
			
		//CHECK IF NEW LIST
		if(message.getType() == ObserverMessage.LIST_TRANSACTION_TYPE || message.getType() == ObserverMessage.ADD_TRANSACTION_TYPE || message.getType() == ObserverMessage.REMOVE_TRANSACTION_TYPE)
		{			
			if(transactions == null)
			{
				transactions = (SortableList<byte[], Transaction>) message.getValue();
				transactions.sort(TransactionMap.TIMESTAMP_INDEX, true);
			}
		
			boolean alreadyIs = false;
			boolean added = false; 
			
			for (int i = transactions.size()-1; i >= 0; i--) {
				if(transactions.get(i).getB().getType() == Transaction.MESSAGE_TRANSACTION)
				{	
					alreadyIs = false;
					
					for (int j = 0; j < messageBufs.size(); j++) 
					{			
						if(Arrays.equals(transactions.get(i).getB().getSignature(),messageBufs.get(j).getSignature()))
						{
							alreadyIs = true;
						}
					}
		
					if(! alreadyIs)
					{
						messageBufs.add(0, new MessageBuf(
							((MessageTransaction)transactions.get(i).getB()).getData(), 
							((MessageTransaction)transactions.get(i).getB()).isEncrypted(),
							((MessageTransaction)transactions.get(i).getB()).getSender().getAddress(),
							((MessageTransaction)transactions.get(i).getB()).getRecipient().getAddress(),
							((MessageTransaction)transactions.get(i).getB()).getTimestamp(),
							((MessageTransaction)transactions.get(i).getB()).getAmount(),
							((MessageTransaction)transactions.get(i).getB()).getFee(),
							((MessageTransaction)transactions.get(i).getB()).getSignature(),
							((MessageTransaction)transactions.get(i).getB()).getCreator().getPublicKey(),
							((MessageTransaction)transactions.get(i).getB()).getConfirmations(),
							((MessageTransaction)transactions.get(i).getB()).isText()
						));
						
						added = true;
					}
				}
			}
			
			if(added)
			{
				messagesModel.setRowCount(messageBufs.size());
			
				for (int j = 0; j < messageBufs.size(); j++) 
				{
					int textHeight = (3+lineCount(messageBufs.get(j).getDecrMessage()))*fontHeight;
					if(textHeight< 24 + 3*fontHeight)
					{
						textHeight = 24 + 3*fontHeight;
					}
					this.setRowHeight(j, textHeight);
				}
				                
				if(messageBufs.get(1).getOpend() && Controller.getInstance().isWalletUnlocked())
				{
					CryptoOpenBox(0, 1);
				}
			}
			this.repaint();
		}
	}

	public void cryptoCloseAll()
	{
		for (int i = 0; i < messageBufs.size(); i++) {
			CryptoOpenBox(i, 2);
		}
		
		menuDecrypt.setText("Decrypt");
		this.repaint();
	}
	
	
	public void CryptoOpenBoxAll()
	{
		int toOpen = 0;
		if(messageBufs.size()>0 && messageBufs.get(0).getOpend())
		{
			toOpen = 2;
		}
		else
		{
			toOpen = 1;
		}
	
		for (int i = 0; i < messageBufs.size(); i++) {
			CryptoOpenBox(i, toOpen);
		}
	}
	
	private void CryptoOpenBox(int row, int toOpen)
	{
		// toOpen 0 - switch, 1 - open, 2 - close
	
		if(messageBufs.get(row).getEncrypted())
		{
			if(toOpen != 2 && !messageBufs.get(row).getOpend())
			{
				if(!Controller.getInstance().isWalletUnlocked())
				{
					//ASK FOR PASSWORD
					String password = PasswordPane.showUnlockWalletDialog(); 
					if(password.equals(""))
					{
						return;
					}
					if(!Controller.getInstance().unlockWallet(password))
					{
						//WRONG PASSWORD
						JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
						
						return;
					}
					
				}
		
				Account account = Controller.getInstance().getAccountByAddress(messageBufs.get(row).getSender());	
				
				byte[] privateKey = null; 
				byte[] publicKey = null;
				//IF SENDER ANOTHER
				if(account == null)
				{
		    		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(messageBufs.get(row).getRecipient());
					privateKey = accountRecipient.getPrivateKey();		
					
					publicKey = messageBufs.get(row).getSenderPublicKey();    				
				}
				//IF SENDER ME
				else
				{
		    		PrivateKeyAccount accountRecipient = Controller.getInstance().getPrivateKeyAccountByAddress(account.getAddress());
					privateKey = accountRecipient.getPrivateKey();		
					
					if(messageBufs.get(row).getToPublicKey() == null)
					{
						messageBufs.get(row).setRecipientPublicKey(Controller.getInstance().getPublicKeyFromAddress( messageBufs.get(row).getRecipient()));
					}
					publicKey = messageBufs.get(row).getToPublicKey();    				
				}
				
				byte[] decrypt = null;
				
				try {
					decrypt = AEScrypto.dataDecrypt(messageBufs.get(row).getMessage(), privateKey, publicKey);
				} catch (InvalidCipherTextException | NullPointerException e1) {
					messageBufs.get(row).setDecryptedMessage("Decrypt Error!");
				} 
		
				if(decrypt == null)
				{
					messageBufs.get(row).setDecryptedMessage("Decrypt Error!");			        			
				}
				else
				{
					messageBufs.get(row).setDecryptedMessage(( messageBufs.get(row).isText() ) ? new String(decrypt, Charset.forName("UTF-8")) : Converter.toHex(decrypt));
					messageBufs.get(row).setOpend(true);
					menuDecrypt.setText("Hide decrypted");
				}
			}
			else
			{
				if(toOpen != 1)
				{
					messageBufs.get(row).setDecryptedMessage("");
					messageBufs.get(row).setOpend(false);
					menuDecrypt.setText("Decrypt");
				}
			}
	
		
			int textHeight = (3+lineCount(messageBufs.get(row).getDecrMessage()))*fontHeight;
			if(textHeight< 24 + 3*fontHeight)
			{
				textHeight = 24 + 3*fontHeight;
			}
			this.setRowHeight(row, textHeight);
		}	
	} 
	
	
	private void updateBlock()
	{
		for (int j = 0; j < messageBufs.size(); j++) 
		{	
			try
			{
				if(DBSet.getInstance().getTransactionMap().contains(messageBufs.get(j).getSignature()))
				{
					messageBufs.get(j).setConfirmations(0);
				}
				else
				{
					messageBufs.get(j).setConfirmations(Controller.getInstance().getTransaction(messageBufs.get(j).getSignature()).getConfirmations());
				}	
			} catch (Exception e) {
				messageBufs.get(j).setConfirmations(0);
			}
		}
		this.repaint();
	}
	
	int lineCount(String text) 
	{
		int lineCount = 1;
		
		for(int k = 0; k < text.length(); k++) 
	    {
	       if (text.charAt(k)=='\n')
	       {
	    	   lineCount++;
	       }
	    }
		return lineCount;
	}

	public class MessageBuf
	{
		private byte[] rawMessage;
		private String decryptedMessage;
		private boolean encrypted;
		private boolean opened;
		private boolean isText;
		private String sender;
		private byte[] senderPublicKey;
		private String recipient;
		private byte[] recipientPublicKey;
		private long timestamp;
		private BigDecimal amount;
		private BigDecimal fee;
		private byte[] signature;
		private int confirmations;
		
		
		public MessageBuf(byte[] rawMessage, boolean encrypted, String sender, String recipient, long timestamp, BigDecimal amount, BigDecimal fee, byte[] signature, byte[] senderPublicKey, int confirmations, boolean isText)
		{
			this.rawMessage = rawMessage;
			this.encrypted = encrypted;	
			this.decryptedMessage = "";
			this.opened = false;
			this.sender = sender;
			this.recipient = recipient;
			this.timestamp = timestamp;
			this.amount = amount;
			this.fee = fee;
			this.senderPublicKey = senderPublicKey;
			this.recipientPublicKey = null;
			this.signature = signature;
			this.confirmations = confirmations;
			this.isText = isText;
		}

		public byte[] getMessage()
		{
			return this.rawMessage;
		}
		public boolean getEncrypted()
		{
			return this.encrypted;
		}
		public String getDecrMessage()
		{
			if(decryptedMessage.equals(""))
			{
				if(this.encrypted && !this.opened)
				{
					this.decryptedMessage = "Encrypted";
				}
				if(!this.encrypted)
				{
					this.decryptedMessage = ( isText ) ? new String(this.rawMessage, Charset.forName("UTF-8")) : Converter.toHex(this.rawMessage);
				}
			}
			return this.decryptedMessage;
		}
		public String getSender()
		{
			return this.sender;
		}
		public String getRecipient()
		{
			return this.recipient;
		}
		public BigDecimal getFee()
		{
			return this.fee;
		}
		public BigDecimal getAmount()
		{
			return this.amount;
		}
		public byte[] getSignature()
		{
			return this.signature;
		}
		public byte[] getSenderPublicKey()
		{
			return this.senderPublicKey;
		}
		public byte[] getToPublicKey()
		{
			return this.recipientPublicKey;
		}
		public void setRecipientPublicKey(byte[] recipientPublicKey)
		{
			this.recipientPublicKey = recipientPublicKey;
		}
		public long getTimestamp()
		{
			return this.timestamp;
		}
		public boolean getOpend()
		{
			return this.opened;
		}
		public void setOpend(boolean opened)
		{
			this.opened = opened;
		}
		public void setDecryptedMessage(String decryptedMessage)
		{
			this.decryptedMessage = decryptedMessage;
		}
		public int getConfirmations()
		{
			return this.confirmations;
		}
		public void setConfirmations(int confirmations)
		{
			this.confirmations = confirmations;
		}
		public boolean isText()
		{
			return isText;
		}
	}
		
}

