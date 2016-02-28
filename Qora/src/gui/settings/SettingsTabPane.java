package gui.settings;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import lang.Lang;


public class SettingsTabPane extends JTabbedPane{

	private static final long serialVersionUID = 2198816415884720961L;
	
	public SettingsKnownPeersPanel settingsKnownPeersPanel;
	public SettingsParametersPanel settingsParametersPanel;
	public SettingsAllowedPanel settingsAllowedPanel;
	
	public SettingsTabPane()
	{
		super();
		
		//ADD TABS
			
        settingsParametersPanel = new SettingsParametersPanel();
        JScrollPane scrollPane1 = new JScrollPane(settingsParametersPanel);
        this.addTab(Lang.getInstance().translate("Basic"), scrollPane1);

		settingsKnownPeersPanel = new SettingsKnownPeersPanel();
        JScrollPane scrollPane2 = new JScrollPane(settingsKnownPeersPanel);
        this.addTab(Lang.getInstance().translate("Known Peers"), scrollPane2);
        
        settingsAllowedPanel = new SettingsAllowedPanel();
        JScrollPane scrollPane3 = new JScrollPane(settingsAllowedPanel);
        this.addTab(Lang.getInstance().translate("Access permission"), scrollPane3);

	}
	public void close() 
	{
		//REMOVE OBSERVERS/HANLDERS
		this.settingsKnownPeersPanel.close();
		this.settingsAllowedPanel.close();
	}
}