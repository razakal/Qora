package gui.voting;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;

@SuppressWarnings("serial")
public class AllPollsFrame extends JFrame{

	private PollTabPane pollTabPane;
	
	public AllPollsFrame() 
	{
		//CREATE FRAME
		super("Qora - All Polls");
		
		//ICON
		List<Image> icons = new ArrayList<Image>();
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon16.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon32.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon64.png"));
		icons.add(Toolkit.getDefaultToolkit().getImage("images/icons/icon128.png"));
		this.setIconImages(icons);
		
		//POLL TABPANE
        this.pollTabPane = new PollTabPane();
		
		//ON CLOSE
		this.addWindowListener(new WindowAdapter()
		{
            public void windowClosing(WindowEvent e)
            {
            	//CLOSE name EXCHANGE
                pollTabPane.close();
                
                //DISPOSE
                setVisible(false);
                dispose();
            }
        });
		       
		 //ADD GENERAL TABPANE TO FRAME
        this.add(this.pollTabPane);
        
        //SHOW FRAME
        this.pack();
        this.setLocationRelativeTo(null);
        this.setVisible(true);
	}	
}
