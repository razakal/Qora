package gui.status;

import gui.PasswordPane;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import controller.Controller;

@SuppressWarnings("serial")
public class StatusPanel extends JPanel 
{
	public StatusPanel()
	{
		super();
		
		this.add(new NetworkStatus(), BorderLayout.EAST);
		
		WalletStatus walletStatus = new WalletStatus();
		walletStatus.addMouseListener(new MouseAdapter() 
		{
			public void mouseClicked(MouseEvent e) 
			{
				if(e.getClickCount() == 2) 
				{
					if(Controller.getInstance().isWalletUnlocked())
					{
						Controller.getInstance().lockWallet();
					}
					else
					{
						String password = PasswordPane.showUnlockWalletDialog(); 
						if(!Controller.getInstance().unlockWallet(password))
						{
							JOptionPane.showMessageDialog(null, "Invalid password", "Unlock Wallet", JOptionPane.ERROR_MESSAGE);
						}
					}
			    }
			}
		});
		
		this.add(walletStatus, BorderLayout.EAST);
		this.add(new ForgingStatus(), BorderLayout.EAST);
		
	}
}
