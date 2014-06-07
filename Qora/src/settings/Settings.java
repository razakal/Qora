package settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import network.Peer;

public class Settings {

	//NETWORK
	private static final int DEFAULT_MIN_CONNECTIONS = 5;
	private static final int DEFAULT_MAX_CONNECTIONS = 20;
	private static final int DEFAULT_CONNECTION_TIMEOUT = 60000;
	
	//RPC
	private static final int DEFAULT_RPC_PORT = 9085;
	private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1";
	
	//DATA
	private static final String DEFAULT_DATA_DIR = "data";
	private static final String DEFAULT_WALLET_DIR = "wallet";
	
	private static Settings instance;
	
	private Map<String, String> settingsMap;
	
	public static Settings getInstance()
	{
		if(instance == null)
		{
			instance = new Settings();
		}
		
		return instance;
	}
	
	private Settings()
	{
		BufferedReader reader;
		
		try
		{
			//INIT MAP
			settingsMap = new HashMap<String, String>();
			
			//OPEN FILE
			File file = new File("settings.ini");
			
			//CREATE FILE IF IT DOESNT EXIST
			if(!file.exists())
			{
				file.createNewFile();
			}
			
			//READ SETTINGS FILE
			reader = new BufferedReader(new FileReader(file));
			
			String line;
			
			//READ LINE
			while ((line = reader.readLine()) != null)
			{
				String[] splitLine = line.split(" ");
				
				//ADD TO MAP
				settingsMap.put(splitLine[0], splitLine[1]);
		    }
			
			//CLOSE
			reader.close();
		}
		catch(Exception e)
		{
			//STOP
			System.out.println("ERROR reading settings.ini. closing");
			System.exit(0);
		}
	}
	
	public List<Peer> getKnownPeers()
	{
		try
		{
			//GET PEERS FOR MAP
			String peersString = settingsMap.get("knownpeers");
			
			//SPLIT PEERS
			String[] peersArray = peersString.split(";");
			
			//CREATE LIST WITH PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			for(int i=0; i<peersArray.length; i++)
			{
				InetAddress address = InetAddress.getByName(peersArray[i]);
				
				//CHECK IF SOCKET IS NOT LOCALHOST
				if(!address.equals(InetAddress.getLocalHost()))
				{
					//CREATE PEER
					Peer peer = new Peer(address);
								
					//ADD TO LIST
					peers.add(peer);
				}
			}
			
			//RETURN
			return peers;
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new ArrayList<Peer>();
		}
	}
	
	public int getMaxConnections()
	{
		if(settingsMap.containsKey("maxconnections"))
		{
			return Integer.parseInt(settingsMap.get("maxconnections"));
		}
		
		return DEFAULT_MAX_CONNECTIONS;
	}
	
	public int getMinConnections()
	{
		if(settingsMap.containsKey("minconnections"))
		{
			return Integer.parseInt(settingsMap.get("minconnections"));
		}
		
		return DEFAULT_MIN_CONNECTIONS;
	}
	
	public int getConnectionTimeout()
	{
		if(settingsMap.containsKey("connectiontimeout"))
		{
			return Integer.parseInt(settingsMap.get("connectiontimeout"));
		}
		
		return DEFAULT_CONNECTION_TIMEOUT;
	}
	
	public int getRpcPort()
	{
		if(settingsMap.containsKey("rpcport"))
		{
			return Integer.parseInt(settingsMap.get("rpcport"));
		}
		
		return DEFAULT_RPC_PORT;
	}
	
	public String[] getRpcAllowed()
	{
		try
		{
			if(settingsMap.containsKey("rpcallowed"))
			{
				//GET PEERS FOR MAP
				String rpcAllowed = settingsMap.get("rpcallowed");
				
				//SPLIT PEERS
				String[] allowed = rpcAllowed.split(";");
				
				//RETURN
				return allowed;
			}
			
			//RETURN
			return DEFAULT_RPC_ALLOWED.split(";");
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new String[0];
		}
	}
	
	public String getWalletDir()
	{
		if(settingsMap.containsKey("walletdir"))
		{
			return settingsMap.get("walletdir");
		}
		
		return DEFAULT_WALLET_DIR;
	}
	
	public String getDataDir()
	{
		if(settingsMap.containsKey("datadir"))
		{
			return settingsMap.get("datadir");
		}
		
		return DEFAULT_DATA_DIR;
	}
}
