package gui.voting;


import javax.swing.JTabbedPane;

public class PollTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2717571093561259483L;

	private AllPollsPanel allPollsPanel;
		
	public PollTabPane()
	{
		super();
			
		//ALL POLLS
		this.allPollsPanel = new AllPollsPanel();
		this.addTab("All Polls", this.allPollsPanel);
	}

	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.allPollsPanel.removeObservers();
	}
	
}
