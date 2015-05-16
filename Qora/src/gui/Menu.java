package gui;

import gui.settings.SettingsFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import controller.Controller;


public class Menu extends JMenuBar 
{
	private static final long serialVersionUID = 5237335232850181080L;

	public Menu()
	{
		super();
		
		//FILE MENU
        JMenu fileMenu = new JMenu("File");
        fileMenu.getAccessibleContext().setAccessibleDescription("File menu");
        this.add(fileMenu);
 
        //CONSOLE
        JMenuItem consoleItem = new JMenuItem("Debug");
        consoleItem.getAccessibleContext().setAccessibleDescription("Debug information");
        consoleItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new DebugFrame();
        	}
        });
        fileMenu.add(consoleItem);
        
        //SETTINGS
        JMenuItem settingsItem = new JMenuItem("Settings");
        settingsItem.getAccessibleContext().setAccessibleDescription("Settings of program");
        settingsItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
                new SettingsFrame();
        	}
        });
        fileMenu.add(settingsItem);        
        
        //QUIT
        JMenuItem quitItem = new JMenuItem("Quit");
        quitItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, ActionEvent.ALT_MASK));
        quitItem.getAccessibleContext().setAccessibleDescription("Quit the application");
        quitItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
               Controller.getInstance().stopAll();
               System.exit(0);
        	}
        });
        fileMenu.add(quitItem);    
        
        /*//HELP MENU
        JMenu helpMenu = new JMenu("Help");
        helpMenu.getAccessibleContext().setAccessibleDescription("Help menu");
        this.add(helpMenu);
        
        //ABOUT
        JMenuItem aboutItem = new JMenuItem("About");
        aboutItem.getAccessibleContext().setAccessibleDescription("Information about the application");
        helpMenu.add(aboutItem);  */ 
	}
	
}
