package gui;

import gui.settings.SettingsFrame;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import settings.Settings;
import controller.Controller;

import utils.URLViewer;

public class Menu extends JMenuBar 
{
	private static final long serialVersionUID = 5237335232850181080L;
	public static JMenuItem webServerItem;
	
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

        //WEB SERVER
        webServerItem = new JMenuItem("Decentralized Web-server");
        webServerItem.getAccessibleContext().setAccessibleDescription("http://127.0.0.1:"+Settings.getInstance().getWebPort());
        webServerItem.addActionListener(new ActionListener()
        {
        	public void actionPerformed(ActionEvent e)
        	{
        		try {
        			URLViewer.openWebpage(new URL("http://127.0.0.1:"+Settings.getInstance().getWebPort()));
				} catch (MalformedURLException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
        	}
        });
        fileMenu.add(webServerItem);   
        
        webServerItem.setVisible(Settings.getInstance().isWebEnabled());
        
        //SEPARATOR
        fileMenu.addSeparator();
        
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
