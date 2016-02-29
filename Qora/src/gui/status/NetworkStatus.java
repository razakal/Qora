package gui.status;

import java.awt.Color;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.TimeUnit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.ToolTipManager;

import controller.Controller;
import lang.Lang;
import utils.GUIUtils;
import utils.ObserverMessage;

@SuppressWarnings("serial")
public class NetworkStatus extends JLabel implements Observer
{
	private ImageIcon noConnectionsIcon;
	private ImageIcon synchronizingIcon;
	private ImageIcon walletSynchronizingIcon;
	private ImageIcon okeIcon;
	private int currentHeight = 1;
	
	public NetworkStatus()
	{
		super();
		
		//CREATE ICONS
		this.noConnectionsIcon = this.createIcon(Color.RED);
		this.synchronizingIcon = this.createIcon(Color.ORANGE);
		this.walletSynchronizingIcon = this.createIcon(Color.YELLOW);
		this.okeIcon = this.createIcon(Color.GREEN);
		
		ToolTipManager.sharedInstance().setDismissDelay( (int) TimeUnit.SECONDS.toMillis(5));
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent mEvt) {
				if(Controller.getInstance().getStatus() == Controller.STATUS_OK || Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS) {
					setToolTipText(Lang.getInstance().translate("Block height: ") + Controller.getInstance().getHeight());
				} else if( Controller.getInstance().getWalletSyncHeight() > 0 ) {
					setToolTipText(Lang.getInstance().translate("Block height: ") + currentHeight + "/" + Controller.getInstance().getHeight() + "/" + Controller.getInstance().getMaxPeerHeight());
				} else {
					setToolTipText(Lang.getInstance().translate("Block height: ") + currentHeight + "/" + Controller.getInstance().getMaxPeerHeight());
				}
		}});
		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);	
		//Controller.getInstance().addWalletListener(this);	
	}
	
	private ImageIcon createIcon(Color color)
	{
		return GUIUtils.createIcon(color, this.getBackground());
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.WALLET_SYNC_STATUS)
		{
			currentHeight = (int)message.getValue();
			if(currentHeight == -1)
			{
				this.update(null, new ObserverMessage(
						ObserverMessage.NETWORK_STATUS, Controller.getInstance().getStatus()));
				currentHeight = Controller.getInstance().getHeight();
				return;
			}
			
			this.setIcon(walletSynchronizingIcon);
			this.setText(Lang.getInstance().translate("Wallet Synchronizing ") + 100 * currentHeight/Controller.getInstance().getHeight() + "%");
		}
		
		if(message.getType() == ObserverMessage.BLOCKCHAIN_SYNC_STATUS)
		{
			currentHeight = (int)message.getValue(); 

			if(Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING)
			{
				this.setText(Lang.getInstance().translate("Synchronizing") + " " + 100 * currentHeight/Controller.getInstance().getMaxPeerHeight() + "%");	
			}	
		}
		
		if(message.getType() == ObserverMessage.NETWORK_STATUS)
		{
			int status = (int) message.getValue();
			
			if(status == Controller.STATUS_NO_CONNECTIONS)
			{
				this.setIcon(noConnectionsIcon);
				this.setText(Lang.getInstance().translate("No connections"));
			}
			if(status == Controller.STATUS_SYNCHRONIZING)
			{
				this.setIcon(synchronizingIcon);
				this.setText(Lang.getInstance().translate("Synchronizing"));
			}
			if(status == Controller.STATUS_OK)
			{
				this.setIcon(okeIcon);
				this.setText(Lang.getInstance().translate("OK"));
			}
		}		
	}
}
