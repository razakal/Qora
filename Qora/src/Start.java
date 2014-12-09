import java.util.Scanner;

import javax.swing.UIManager;

import api.ApiClient;
import controller.Controller;

public class Start {

	public static void main(String args[])
	{	
		boolean cli = false;
		boolean disableRpc = false;
		for(String arg: args)
		{		
			if(arg.equals("-cli"))
			{
				cli = true;
			}
		}
		
		if(!cli)
		{			
			try
			{
				//STARTING NETWORK/BLOCKCHAIN/RPC
				Controller.getInstance().start(disableRpc);
			}
			catch(Exception e)
			{
				e.printStackTrace();
				
				//USE SYSTEM STYLE
		        try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				
				//ERROR STARTING
				System.out.println("STARTUP ERROR: " + e.getMessage());
				
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
