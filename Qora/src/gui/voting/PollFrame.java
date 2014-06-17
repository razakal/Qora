package gui.voting;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

import qora.voting.Poll;

@SuppressWarnings("serial")
public class PollFrame extends JFrame{

	private PollTabPane PollTabPane;
	
	public PollFrame(Poll poll) 
	{
		//CREATE FRAME
		super("Qora - Poll Details");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//POLL TABPANE
        this.PollTabPane = new PollTabPane(poll);
		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//CLOSE POLL FRME
                PollTabPane.close();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
		 //ADD POLL TABPANE TO FRAME
        this.add(this.PollTabPane);
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
