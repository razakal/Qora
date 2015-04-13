package gui.status;

import java.awt.Color;
import java.util.Observable;
import java.util.Observer;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import qora.BlockGenerator;
import utils.GUIUtils;
import utils.ObserverMessage;
import controller.Controller;

@SuppressWarnings("serial")
public class ForgingStatus extends JLabel implements Observer {

	private ImageIcon forgingDisabledIcon;
	private ImageIcon forgingEnabledIcon;
	private ImageIcon forgingIcon;
	
	
	
	
	public ForgingStatus()
	{
		super();
		
		//CREATE ICONS
		this.forgingDisabledIcon = this.createIcon(Color.RED);
		this.forgingEnabledIcon = this.createIcon(Color.ORANGE);
		this.forgingIcon = this.createIcon(Color.GREEN);

		//LISTEN ON STATUS
		Controller.getInstance().addObserver(this);	
		setIconAndText(Controller.getInstance().getForgingStatus());
	}
	
	private ImageIcon createIcon(Color color)
	{
		return GUIUtils.createIcon(color, this.getBackground());
	}
	
	
	@Override
	public void update(Observable arg0, Object arg1) 
	{
		ObserverMessage message = (ObserverMessage) arg1;
		
		if(message.getType() == ObserverMessage.FORGING_STATUS)
		{
			BlockGenerator.ForgingStatus status = (BlockGenerator.ForgingStatus) message.getValue();
			
			setIconAndText(status);
		}		
	}

	private void setIconAndText(BlockGenerator.ForgingStatus status) {
		if(status == BlockGenerator.ForgingStatus.FORGING_DISABLED)
		{
			forgingDisabled();
		}
		if(status ==BlockGenerator.ForgingStatus.FORGING_ENABLED)
		{
			this.setIcon(forgingEnabledIcon);
			this.setText("Forging enabled");
		}
		if(status == BlockGenerator.ForgingStatus.FORGING)
		{
			this.setIcon(forgingIcon);
			this.setText("Forging");
		}
	}

	public void forgingDisabled() {
		this.setIcon(forgingDisabledIcon);
		this.setText("Forging disabled");
	}

	

}
