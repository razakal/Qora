import gui.Gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.swing.JOptionPane;
import javax.swing.UIManager;

import lang.Lang;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import settings.Settings;
import utils.SysTray;
import api.ApiClient;
import controller.Controller;

public class Start {
	
	static Logger LOGGER = Logger.getLogger(Start.class.getName());

	public static void main(String args[]) throws IOException
	{	
		
		File log4j = new File("log4j.properties");
		if(log4j.exists())
		{
			PropertyConfigurator.configure(log4j.getAbsolutePath());
		}else
		{
			try( InputStream resourceAsStream = ClassLoader.class.getResourceAsStream("/log4j/log4j.default");)
			{
				PropertyConfigurator.configure(resourceAsStream);
				LOGGER.error("log4j.properties not found, search path is " + log4j.getAbsolutePath() + " using default!");
			}
		}
		
		boolean cli = false;
		
		
		
		for(String arg: args)
		{
			if(arg.equals("-cli"))
			{
				cli = true;
			} 
			else 
			{
				if(arg.startsWith("-peers=") && arg.length() > 7) 
				{
					Settings.getInstance().setDefaultPeers(arg.substring(7).split(","));
				}
				
				if(arg.equals("-testnet")) 
				{
					Settings.getInstance().setGenesisStamp(System.currentTimeMillis());
				} 
				else if(arg.startsWith("-testnet=") && arg.length() > 9) 
				{
					try
					{
						long testnetstamp = Long.parseLong(arg.substring(9));
						
						if (testnetstamp == 0)
						{
							testnetstamp = System.currentTimeMillis();
						}
							
						Settings.getInstance().setGenesisStamp(testnetstamp);
					} catch(Exception e) {
						Settings.getInstance().setGenesisStamp(Settings.DEFAULT_MAINNET_STAMP);
					}
				}
			}
		}
		
		if(!cli)
		{			
			try
			{
				
				//ONE MUST BE ENABLED
				if(!Settings.getInstance().isGuiEnabled() && !Settings.getInstance().isRpcEnabled())
				{
					throw new Exception(Lang.getInstance().translate("Both gui and rpc cannot be disabled!"));
				}
				
				LOGGER.info(Lang.getInstance().translate("Starting %qora% / version: %version% / build date: %builddate% / ...")
						.replace("%version%", Controller.getInstance().getVersion())
						.replace("%builddate%", Controller.getInstance().getBuildDateString())
						.replace("%qora%", Lang.getInstance().translate("Qora"))
						);
				
				//STARTING NETWORK/BLOCKCHAIN/RPC
				Controller.getInstance().start();
				
				try
				{
						//START GUI
						if(Gui.getInstance() != null && Settings.getInstance().isSysTrayEnabled())
						{					
							SysTray.getInstance().createTrayIcon();
						}
				} catch(Exception e) {
					LOGGER.error(Lang.getInstance().translate("GUI ERROR") ,e);
				}
				
			} catch(Exception e) {
				
				LOGGER.error(e.getMessage(),e);
				
				//USE SYSTEM STYLE
		        try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
					LOGGER.error(e2);
				}
				
				//ERROR STARTING
				LOGGER.error(Lang.getInstance().translate("STARTUP ERROR") + ": " + e.getMessage());
				
				if(Gui.isGuiStarted())
				{
					JOptionPane.showMessageDialog(null, e.getMessage(), Lang.getInstance().translate("Startup Error"), JOptionPane.ERROR_MESSAGE);
				}
				
				//FORCE SHUTDOWN
				System.exit(0);
			}
		}
		else
		{
			Scanner scanner = new Scanner(System.in);
			ApiClient client = new ApiClient();
			
			while(true)
			{
				
				System.out.print("[COMMAND] ");
				String command = scanner.nextLine();
				
				if(command.equals("quit"))
				{
					scanner.close();
					System.exit(0);
				}
				
				String result = client.executeCommand(command);
				System.out.println("[RESULT] " + result);
			}
		}		
	}
}
