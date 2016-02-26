package gui.transaction;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import lang.Lang;
import qora.crypto.Base58;
import qora.transaction.PaymentTransaction;
import utils.DateTimeFormat;
import utils.MenuPopupUtil;

@SuppressWarnings("serial")
public class PaymentDetailsFrame extends JFrame
{
	public PaymentDetailsFrame(PaymentTransaction payment)
	{
		super(Lang.getInstance().translate("Qora") + " - " + Lang.getInstance().translate("Transaction Details"));
		
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
		
		//LABEL TYPE
		labelGBC.gridy = 0;
		JLabel typeLabel = new JLabel(Lang.getInstance().translate("Type:"));
		this.add(typeLabel, labelGBC);
						
		//TYPE
		detailGBC.gridy = 0;
		JLabel type = new JLabel(Lang.getInstance().translate("Payment Transaction"));
		this.add(type, detailGBC);
		
		//LABEL SIGNATURE
		labelGBC.gridy = 1;
		JLabel signatureLabel = new JLabel(Lang.getInstance().translate("Signature:"));
		this.add(signatureLabel, labelGBC);
				
		//SIGNATURE
		detailGBC.gridy = 1;
		JTextField signature = new JTextField(Base58.encode(payment.getSignature()));
		signature.setEditable(false);
		MenuPopupUtil.installContextMenu(signature);
		this.add(signature, detailGBC);
		
		//LABEL REFERENCE
		labelGBC.gridy = 2;
		JLabel referenceLabel = new JLabel(Lang.getInstance().translate("Reference:"));
		this.add(referenceLabel, labelGBC);
						
		//REFERENCE
		detailGBC.gridy = 2;
		JTextField reference = new JTextField(Base58.encode(payment.getReference()));
		reference.setEditable(false);
		MenuPopupUtil.installContextMenu(reference);
		this.add(reference, detailGBC);
		
		//LABEL TIMESTAMP
		labelGBC.gridy = 3;
		JLabel timestampLabel = new JLabel(Lang.getInstance().translate("Timestamp:"));
		this.add(timestampLabel, labelGBC);
						
		//TIMESTAMP
		detailGBC.gridy = 3;
		JTextField timestamp = new JTextField(DateTimeFormat.timestamptoString(payment.getTimestamp()));
		timestamp.setEditable(false);
		MenuPopupUtil.installContextMenu(timestamp);
		this.add(timestamp, detailGBC);
		
		//LABEL SENDER
		labelGBC.gridy = 4;
		JLabel senderLabel = new JLabel(Lang.getInstance().translate("Sender:"));
		this.add(senderLabel, labelGBC);
		
		//SENDER
		detailGBC.gridy = 4;
		JTextField sender = new JTextField(payment.getSender().getAddress());
		sender.setEditable(false);
		MenuPopupUtil.installContextMenu(sender);
		this.add(sender, detailGBC);
		
		//LABEL RECIPIENT
		labelGBC.gridy = 5;
		JLabel recipientLabel = new JLabel(Lang.getInstance().translate("Recipient:"));
		this.add(recipientLabel, labelGBC);
		
		//RECIPIENT
		detailGBC.gridy = 5;
		JTextField recipient = new JTextField(payment.getRecipient().getAddress());
		recipient.setEditable(false);
		MenuPopupUtil.installContextMenu(recipient);
		this.add(recipient, detailGBC);		
		
		//LABEL AMOUNT
		labelGBC.gridy = 6;
		JLabel amountLabel = new JLabel(Lang.getInstance().translate("Amount:"));
		this.add(amountLabel, labelGBC);
				
		//AMOUNT
		detailGBC.gridy = 6;
		JTextField amount = new JTextField(payment.getAmount().toPlainString());
		amount.setEditable(false);
		MenuPopupUtil.installContextMenu(amount);
		this.add(amount, detailGBC);		
		
		//LABEL FEE
		labelGBC.gridy = 7;
		JLabel feeLabel = new JLabel(Lang.getInstance().translate("Fee:"));
		this.add(feeLabel, labelGBC);
						
		//FEE
		detailGBC.gridy = 7;
		JTextField fee = new JTextField(payment.getFee().toPlainString());
		fee.setEditable(false);
		MenuPopupUtil.installContextMenu(fee);
		this.add(fee, detailGBC);	
		
		//LABEL CONFIRMATIONS
		labelGBC.gridy = 8;
		JLabel confirmationsLabel = new JLabel(Lang.getInstance().translate("Confirmations:"));
		this.add(confirmationsLabel, labelGBC);
								
		//CONFIRMATIONS
		detailGBC.gridy = 8;
		JLabel confirmations = new JLabel(String.valueOf(payment.getConfirmations()));
		this.add(confirmations, detailGBC);	
		
		//PACK
		this.pack();
        this.setResizable(false);
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}
}
