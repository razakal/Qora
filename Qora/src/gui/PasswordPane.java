package gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordPane 
{
	public static String showUnlockWalletDialog()
	{
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(2,2));

		//Labels for the textfield components        
		JLabel passwordLbl = new JLabel("Enter wallet password:");
		JPasswordField passwordFld = new JPasswordField();

		//Add the components to the JPanel        
		userPanel.add(passwordLbl);
		userPanel.add(passwordFld);

		//As the JOptionPane accepts an object as the message
		//it allows us to use any component we like - in this case 
		//a JPanel containing the dialog components we want
		if(JOptionPane.showConfirmDialog(null, userPanel, "Unlock Wallet" ,JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION)
		{
			return new String(passwordFld.getPassword());
		}
		
		return "";
	}
}
