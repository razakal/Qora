package settings;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.math.BigDecimal;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import controller.Controller;
import network.Peer;

public class Settings {

	//NETWORK
	private static final int DEFAULT_MIN_CONNECTIONS = 10;
	private static final int DEFAULT_MAX_CONNECTIONS = 50;
	private static final int DEFAULT_MAX_RECEIVE_PEERS = 20;
	private static final int DEFAULT_MAX_SENT_PEERS = 20;
	private static final int DEFAULT_CONNECTION_TIMEOUT = 10000;
	private static final int DEFAULT_PING_INTERVAL = 30000;
	private static final boolean DEFAULT_TRYING_CONNECT_TO_BAD_PEERS = true;
	private static final String[] DEFAULT_PEERS = { };

	
	//RPC
	private static final int DEFAULT_RPC_PORT = 9085;
	private static final String DEFAULT_RPC_ALLOWED = "127.0.0.1";
	private static final boolean DEFAULT_RPC_ENABLED = true;
	
	//GUI CONSOLE
	private static final boolean DEFAULT_GUI_CONSOLE_ENABLED = true;
	
	//WEB
	private static final int DEFAULT_WEB_PORT = 9090;
	private static final String DEFAULT_WEB_ALLOWED = "127.0.0.1";
	private static final boolean DEFAULT_WEB_ENABLED = true;
	
	//GUI
	private static final boolean DEFAULT_GUI_ENABLED = true;
	
	//SETTINGS.JSON FILE
	private static final String DEFAULT_SETTINGS_PATH = "settings.json";
	
	//DATA
	private static final String DEFAULT_DATA_DIR = "data";
	private static final String DEFAULT_WALLET_DIR = "wallet";
	
	private static final boolean DEFAULT_GENERATOR_KEY_CACHING = false;
	private static final boolean DEFAULT_CHECKPOINTING = true;

	private static final boolean DEFAULT_SOUND_RECEIVE_COIN = true;
	private static final boolean DEFAULT_SOUND_MESSAGE = true;
	private static final boolean DEFAULT_SOUND_NEW_TRANSACTION = true;
	
	private static final int DEFAULT_MAX_BYTE_PER_FEE = 512;
	private static final boolean ALLOW_FEE_LESS_REQUIRED = false;
	
	private static final BigDecimal DEFAULT_BIG_FEE = new BigDecimal(1000);
	private static final String DEFAULT_BIG_FEE_MESSAGE = "Do you really want to set such a large fee?\nThese coins will go to the forgers.";
	
	//DATE FORMAT
	private static final String DEFAULT_TIME_ZONE = "";
	private static final String DEFAULT_TIME_FORMAT = "";
	
	private static final boolean DEFAULT_NS_UPDATE = false;
	
	private static Settings instance;
	
	private JSONObject settingsJSON;
	
	private String currentSettingsPath;
	
	public static Settings getInstance()
	{
		if(instance == null)
		{
			instance = new Settings();
		}
		
		return instance;
	}
	
	public static void FreeInstance()
	{
		if(instance != null)
		{
			instance = null;
		}
	}
	
	private Settings()
	{
		BufferedReader reader;
		int alreadyPassed = 0;
		String settingsFilePath = "settings.json";
		
		try
		{
			while(alreadyPassed<2)
			{
				//OPEN FILE
				File file = new File(settingsFilePath);
				currentSettingsPath = settingsFilePath;
				
				//CREATE FILE IF IT DOESNT EXIST
				if(!file.exists())
				{
					file.createNewFile();
				}
				
				//READ SETTINGS FILE
				reader = new BufferedReader(new FileReader(file));
				
				String line;
				String jsonString = "";
				
				//READ LINE
				while ((line = reader.readLine()) != null)
				{
					jsonString += line;
			    }
				
				//CLOSE
				reader.close();
				
				//CREATE JSON OBJECT
				this.settingsJSON = (JSONObject) JSONValue.parse(jsonString);
				
				alreadyPassed++;
				
				if(this.settingsJSON.containsKey("settingspath"))
				{
					settingsFilePath = (String) this.settingsJSON.get("settingspath");
				}
				else
				{
					alreadyPassed ++;
				}	
			}
		}
		catch(Exception e)
		{
			//STOP
			System.out.println("ERROR reading settings.json. closing");
			System.exit(0);
		}
	}
	
	public JSONObject Dump()
	{
		return settingsJSON;
	}
	
	public String getCurrentSettingsPath()
	{
		return currentSettingsPath;
	}
	
	@SuppressWarnings("unchecked")
	public List<Peer> getKnownPeers()
	{
		try
		{
			//GET PEERS FROM JSON
			JSONArray peersArray = (JSONArray) this.settingsJSON.get("knownpeers");
			
			if(peersArray.isEmpty())
				peersArray.addAll(Arrays.asList(DEFAULT_PEERS));
				
			//CREATE LIST WITH PEERS
			List<Peer> peers = new ArrayList<Peer>();
			
			for(int i=0; i<peersArray.size(); i++)
			{
				InetAddress address = InetAddress.getByName((String) peersArray.get(i));
				
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
		if(this.settingsJSON.containsKey("maxconnections"))
		{
			return ((Long) this.settingsJSON.get("maxconnections")).intValue();
		}
		
		return DEFAULT_MAX_CONNECTIONS;
	}
	
	public int getMaxReceivePeers()
	{
		if(this.settingsJSON.containsKey("maxreceivepeers"))
		{
			return ((Long) this.settingsJSON.get("maxreceivepeers")).intValue();
		}
		
		return DEFAULT_MAX_RECEIVE_PEERS;
	}
	
	public int getMaxSentPeers()
	{
		if(this.settingsJSON.containsKey("maxsentpeers"))
		{
			return ((Long) this.settingsJSON.get("maxsentpeers")).intValue();
		}
		
		return DEFAULT_MAX_SENT_PEERS;
	}
	
	public int getMinConnections()
	{
		if(this.settingsJSON.containsKey("minconnections"))
		{
			return ((Long) this.settingsJSON.get("minconnections")).intValue();
		}
		
		return DEFAULT_MIN_CONNECTIONS;
	}
	
	public int getConnectionTimeout()
	{
		if(this.settingsJSON.containsKey("connectiontimeout"))
		{
			return ((Long) this.settingsJSON.get("connectiontimeout")).intValue();
		}
		
		return DEFAULT_CONNECTION_TIMEOUT;
	}
	
	public boolean isTryingConnectToBadPeers()
	{
		if(this.settingsJSON.containsKey("tryingconnecttobadpeers"))
		{
			return ((Boolean) this.settingsJSON.get("tryingconnecttobadpeers")).booleanValue();
		}
		
		return DEFAULT_TRYING_CONNECT_TO_BAD_PEERS;
	}
		
	public int getRpcPort()
	{
		if(this.settingsJSON.containsKey("rpcport"))
		{
			return ((Long) this.settingsJSON.get("rpcport")).intValue();
		}
		
		return DEFAULT_RPC_PORT;
	}
	
	public String[] getRpcAllowed()
	{
		try
		{
			if(this.settingsJSON.containsKey("rpcallowed"))
			{
				//GET PEERS FROM JSON
				JSONArray allowedArray = (JSONArray) this.settingsJSON.get("rpcallowed");
				
				//CREATE LIST WITH PEERS
				String[] allowed = new String[allowedArray.size()];
				for(int i=0; i<allowedArray.size(); i++)
				{
					allowed[i] = (String) allowedArray.get(i);
				}
				
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

	public boolean isRpcEnabled() 
	{
		if(this.settingsJSON.containsKey("rpcenabled"))
		{
			return ((Boolean) this.settingsJSON.get("rpcenabled")).booleanValue();
		}
		
		return DEFAULT_RPC_ENABLED;
	}
	
	public int getWebPort()
	{
		if(this.settingsJSON.containsKey("webport"))
		{
			return ((Long) this.settingsJSON.get("webport")).intValue();
		}
		
		return DEFAULT_WEB_PORT;
	}
	
	public boolean isGuiConsoleEnabled() 
	{
		if(this.settingsJSON.containsKey("guiconsoleenabled"))
		{
			return ((Boolean) this.settingsJSON.get("guiconsoleenabled")).booleanValue();
		}
		
		return DEFAULT_GUI_CONSOLE_ENABLED;
	}
	
	public String[] getWebAllowed()
	{
		try
		{
			if(this.settingsJSON.containsKey("weballowed"))
			{
				//GET PEERS FROM JSON
				JSONArray allowedArray = (JSONArray) this.settingsJSON.get("weballowed");
				
				//CREATE LIST WITH PEERS
				String[] allowed = new String[allowedArray.size()];
				for(int i=0; i<allowedArray.size(); i++)
				{
					allowed[i] = (String) allowedArray.get(i);
				}
				
				//RETURN
				return allowed;	
			}
			
			//RETURN
			return DEFAULT_WEB_ALLOWED.split(";");
		}
		catch(Exception e)
		{
			//RETURN EMPTY LIST
			return new String[0];
		}
	}

	public boolean isWebEnabled() 
	{
		if(this.settingsJSON.containsKey("webenabled"))
		{
			return ((Boolean) this.settingsJSON.get("webenabled")).booleanValue();
		}
		
		return DEFAULT_WEB_ENABLED;
	}
	
	public boolean updateNameStorage() 
	{
		if(this.settingsJSON.containsKey("nsupdate"))
		{
			return ((Boolean) this.settingsJSON.get("nsupdate")).booleanValue();
		}
		
		return DEFAULT_NS_UPDATE;
	}
	
	public String getWalletDir()
	{
		if(this.settingsJSON.containsKey("walletdir"))
		{
			return (String) this.settingsJSON.get("walletdir");
		}
		
		return DEFAULT_WALLET_DIR;
	}
	
	public String getDataDir()
	{
		if(this.settingsJSON.containsKey("datadir"))
		{
			return (String) this.settingsJSON.get("datadir");
		}
		
		return DEFAULT_DATA_DIR;
	}
	
	public String getSettingsPath()
	{
		if(this.settingsJSON.containsKey("settingspath"))
		{
			return (String) this.settingsJSON.get("settingspath");
		}
		
		return DEFAULT_SETTINGS_PATH;
	}
	
	public int getPingInterval()
	{
		if(this.settingsJSON.containsKey("pinginterval"))
		{
			return ((Long) this.settingsJSON.get("pinginterval")).intValue();
		}
		
		return DEFAULT_PING_INTERVAL;
	}

	public boolean isGeneratorKeyCachingEnabled() 
	{
		if(this.settingsJSON.containsKey("generatorkeycaching"))
		{
			return ((Boolean) this.settingsJSON.get("generatorkeycaching")).booleanValue();
		}
		
		return DEFAULT_GENERATOR_KEY_CACHING;
	}
	
	public boolean isCheckpointingEnabled() 
	{
		if(this.settingsJSON.containsKey("checkpoint"))
		{
			return ((Boolean) this.settingsJSON.get("checkpoint")).booleanValue();
		}
		
		return DEFAULT_CHECKPOINTING;
	}
	
	public boolean isSoundReceivePaymentEnabled() 
	{
		if(this.settingsJSON.containsKey("soundreceivepayment"))
		{
			return ((Boolean) this.settingsJSON.get("soundreceivepayment")).booleanValue();
		}
		
		return DEFAULT_SOUND_RECEIVE_COIN;
	}
	
	public boolean isSoundReceiveMessageEnabled() 
	{
		if(this.settingsJSON.containsKey("soundreceivemessage"))
		{
			return ((Boolean) this.settingsJSON.get("soundreceivemessage")).booleanValue();
		}
		
		return DEFAULT_SOUND_MESSAGE;
	}
	
	public boolean isSoundNewTransactionEnabled() 
	{
		if(this.settingsJSON.containsKey("soundnewtransaction"))
		{
			return ((Boolean) this.settingsJSON.get("soundnewtransaction")).booleanValue();
		}
		
		return DEFAULT_SOUND_NEW_TRANSACTION;
	}
	
	public int getMaxBytePerFee() 
	{
		if(this.settingsJSON.containsKey("maxbyteperfee"))
		{
			return ((Long) this.settingsJSON.get("maxbyteperfee")).intValue();
		}
		
		return DEFAULT_MAX_BYTE_PER_FEE;
	}
	
	public boolean isAllowFeeLessRequired() 
	{
		if(this.settingsJSON.containsKey("allowfeelessrequired"))
		{
			return ((Boolean) this.settingsJSON.get("allowfeelessrequired")).booleanValue();
		}
		
		return ALLOW_FEE_LESS_REQUIRED;
	}
	
	public BigDecimal getBigFee() 
	{
		return DEFAULT_BIG_FEE;
	}
	
	public String getBigFeeMessage() 
	{
		return DEFAULT_BIG_FEE_MESSAGE;
	}

	public boolean isGuiEnabled() 
	{
		
		if(!Controller.getInstance().doesWalletDatabaseExists())
		{
			return true;
		}
		
		if(System.getProperty("nogui") != null)
		{
			return false;
		}
		if(this.settingsJSON.containsKey("guienabled"))
		{
			return ((Boolean) this.settingsJSON.get("guienabled")).booleanValue();
		}
		
		return DEFAULT_GUI_ENABLED;
	}
	
	public String getTimeZone()
	{
		if(this.settingsJSON.containsKey("timezone")) {
			return (String) this.settingsJSON.get("timezone");
		}
		
		return DEFAULT_TIME_ZONE;
	}
	
	public String getTimeFormat()
	{
		if(this.settingsJSON.containsKey("timeformat")) {
			return (String) this.settingsJSON.get("timeformat");
		}
		
		return DEFAULT_TIME_FORMAT;
	}

	public boolean isSysTrayEnabled() {
		if(this.settingsJSON.containsKey("systray"))
		{
			return ((Boolean) this.settingsJSON.get("systray")).booleanValue();
		}
		return true;
	}
}
