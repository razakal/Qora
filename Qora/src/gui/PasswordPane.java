package gui;

import java.awt.Component;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

import controller.Controller;
import lang.Lang;

public class PasswordPane 
{
	public static String showUnlockWalletDialog()
	{
		JPanel userPanel = new JPanel();
		userPanel.setLayout(new GridLayout(2,2));

		//Labels for the textfield components        
		JLabel passwordLbl = new JLabel(Lang.getInstance().translate("Enter wallet password:"));
		JPasswordField passwordFld = new JPasswordField();

		passwordFld.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				
				Container parent = passwordFld.getTopLevelAncestor();              
				
				if (keyCode == KeyEvent.VK_ENTER) 
				{
					if(e.isControlDown())
					{
						final JButton btn = getButton(parent, Lang.getInstance().translate("Unlock"));
						btn.doClick();
					}
					else
					{
						final JButton btn = getButton(parent, Lang.getInstance().translate("Unlock for 2 minutes"));
						btn.doClick();
					}
				}
				else if (keyCode == KeyEvent.VK_ESCAPE) 
				{
					final JButton btn = getButton(parent, Lang.getInstance().translate("Cancel"));
					btn.doClick();
				}
			}
		});
		      
		//Add the components to the JPanel        
		userPanel.add(passwordLbl);
		userPanel.add(passwordFld);

		Object[] options = {Lang.getInstance().translate("Unlock"),
				Lang.getInstance().translate("Unlock for 2 minutes"),
                Lang.getInstance().translate("Cancel")};		
		
		//As the JOptionPane accepts an object as the message
		//it allows us to use any component we like - in this case 
		//a JPanel containing the dialog components we want
		
		int n = JOptionPane.showOptionDialog(
					null, 
					userPanel, 
					Lang.getInstance().translate("Unlock Wallet"),
					JOptionPane.YES_NO_CANCEL_OPTION, 
					JOptionPane.QUESTION_MESSAGE, 
					null,
					options, 
					passwordFld
				);
		
		if(n == JOptionPane.YES_OPTION) {
			Controller.getInstance().setSecondsToUnlock(-1);
		} else if (n == JOptionPane.NO_OPTION) {
			Controller.getInstance().setSecondsToUnlock(120);
		} else {
			return "";
		}
		
		return new String(passwordFld.getPassword());
	}
	
	public static void switchLockDialog()
	{
		if(Controller.getInstance().isWalletUnlocked())
		{
			Controller.getInstance().lockWallet();
		}
		else
		{
			String password = PasswordPane.showUnlockWalletDialog(); 
			if(!password.equals("") && !Controller.getInstance().unlockWallet(password))
			{
				JOptionPane.showMessageDialog(null, Lang.getInstance().translate("Invalid password"), Lang.getInstance().translate("Unlock Wallet"), JOptionPane.ERROR_MESSAGE);
			}
		}
	}
	
    public static JButton getButton(Container container, String text) {
        JButton btn = null;
        List<Container> children = new ArrayList<Container>(25);
        for (Component child : container.getComponents()) {
            if (child instanceof JButton) {
                JButton button = (JButton) child;
                if (text.equals(button.getText())) {
                    btn = button;
                    break;
                }
            } else if (child instanceof Container) {
                children.add((Container) child);
            }
        }
        if (btn == null) {
            for (Container cont : children) {
                JButton button = getButton(cont, text);
                if (button != null) {
                    btn = button;
                    break;
                }
            }
        }
        return btn;
    }
}
