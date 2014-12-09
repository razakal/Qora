package api;

import java.io.BufferedReader;
import java.io.IOError;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import settings.Settings;
import utils.JSonWriter;

public class ApiClient {

	public String executeCommand(String command)
	{
		if(command.equals("help"))
		{
			String help =  "<method> <url> <data> \n" +
					"Type quit to stop.";	
			
			return help;
		}
		
		try
		{
			//SPLIT
			String[] args = command.split(" ");
			
			//GET METHOD
			String method = args[0].toUpperCase();
			
			//GET PATH
			String path = args[1];
			
			//GET CONTENT
			String content = "";
			if(method.equals("POST"))
			{
				content = command.substring((method + " " + path + " ").length());
			}
			
			//CREATE CONNECTION
			URL url = new URL("http://127.0.0.1:" + Settings.getInstance().getRpcPort() + "/" + path);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			
			//EXECUTE
			connection.setRequestMethod(method);
			
			if(method.equals("POST"))
			{
				connection.setDoOutput(true);
				connection.getOutputStream().write(content.getBytes());
				connection.getOutputStream().flush();
				connection.getOutputStream().close();
			}
			
			//READ RESULT
			InputStream stream;
			if(connection.getResponseCode() == 400)
			{
				stream = connection.getErrorStream();
			}
			else
			{
				stream = connection.getInputStream();
			}
			
			InputStreamReader isReader = new InputStreamReader(stream); 
			BufferedReader br = new BufferedReader(isReader);
			String result = br.readLine(); //TODO READ ALL OR HARDCODE HELP
			
			try
			{
				Writer writer = new JSonWriter();
				Object jsonResult = JSONValue.parse(result);
				
				if(jsonResult instanceof JSONArray)
				{
					((JSONArray) jsonResult).writeJSONString(writer);
					return writer.toString();
				}
				if(jsonResult instanceof JSONObject)
				{
					((JSONObject) jsonResult).writeJSONString(writer);
					return writer.toString();
				}
				
				writer.close();
				return result;
			}
			catch(Exception e)
			{
				return result;
			}
			
		}
		catch(IOError ioe)
		{
			//ioe.printStackTrace();	
			return "Invalid command! \n" +
				"Type help to get a list of commands.";
		}
		catch(Exception e)
		{
			//e.printStackTrace();	
			return "Invalid command! \n" +
				"Type help to get a list of commands.";
		}
	}
	
}
