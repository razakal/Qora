package gui.status;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

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

		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);			
	}
	
	private ImageIcon createIcon(Color color)
	{
		//CREATE IMAGE
		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        
        //AA
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        //SET COLOR
        g.setColor(color);
        
        //CREATE CIRCLE
        g.fillOval(0, 0, 16, 16);
        
        //SET BACKGROUND
        g.setBackground(this.getBackground());     
        
        //CONVERT TO ICON
        return new ImageIcon(image);
	}

	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
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
