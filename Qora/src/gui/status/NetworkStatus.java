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

import utils.GUIUtils;
import utils.ObserverMessage;
import controller.Controller;

@SuppressWarnings("serial")
public class NetworkStatus extends JLabel implements Observer
{
	private ImageIcon noConnectionsIcon;
	private ImageIcon synchronizingIcon;
	private ImageIcon okeIcon;
	
	public NetworkStatus()
	{
		super();
		
		//CREATE ICONS
		this.noConnectionsIcon = this.createIcon(Color.RED);
		this.synchronizingIcon = this.createIcon(Color.ORANGE);
		this.okeIcon = this.createIcon(Color.GREEN);

		ToolTipManager.sharedInstance().setDismissDelay( (int) TimeUnit.SECONDS.toMillis(5));
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseEntered(MouseEvent mEvt) {
				if(Controller.getInstance().getStatus() == Controller.STATUS_OKE || Controller.getInstance().getStatus() == Controller.STATUS_NO_CONNECTIONS)
				{
					setToolTipText("Block height: " + Controller.getInstance().getHeight());
				}
				else if (Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING)
				{
					setToolTipText("Block height: " + Controller.getInstance().getHeight()+"/"+Controller.getInstance().getMaxPeerHeight());
				}	
		}});
		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);			
	}
	
	private ImageIcon createIcon(Color color)
	{
		return GUIUtils.createIcon(color, this.getBackground());
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.ADD_BLOCK_TYPE)
		{
			if(Controller.getInstance().getStatus() == Controller.STATUS_SYNCHRONIZING)
			{
				this.setText("Synchronizing "+ 100*Controller.getInstance().getHeight()/Controller.getInstance().getMaxPeerHeight() + "%");	
			}	
			
		}
		
		if(message.getType() == ObserverMessage.NETWORK_STATUS)
		{
			int status = (int) message.getValue();
			
			if(status == Controller.STATUS_NO_CONNECTIONS)
			{
				this.setIcon(noConnectionsIcon);
				this.setText("No connections");
			}
			if(status == Controller.STATUS_SYNCHRONIZING)
			{
				this.setIcon(synchronizingIcon);
				this.setText("Synchronizing");
			}
			if(status == Controller.STATUS_OKE)
			{
				this.setIcon(okeIcon);
				this.setText("Oke");
			}
		}		
	}
}
