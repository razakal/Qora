package lang;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import settings.Settings;
import utils.Pair;


public class Lang {

	private static Lang instance;
	private Map<String, String> noTranslateMap;
	
	private JSONObject langObj;	
	
	public static Lang getInstance()
	{
		if(instance == null)
		{
			instance = new Lang();
		}
		
		return instance;
	}
	
	private Lang() {
		loadLang();
	}
	
	public void loadLang() {
		langObj = openLangFile( Settings.getInstance().getLang());
		noTranslateMap = new LinkedHashMap<String, String>();
	}
	
	public String[] translate(String[] Messages) 
	{
		String[] translateMessages = Messages.clone();
		for (int i = 0; i < translateMessages.length; i++) {
			translateMessages[i] = this.translate(translateMessages[i]);
		}
		return translateMessages; 
	}
	
	public String translate(String message) 
	{
		//COMMENT AFTER # FOR TRANSLATE THAT WOULD BE THE SAME TEXT IN DIFFERENT WAYS TO TRANSLATE
		String messageWithoutComment = message.replaceFirst("(?<!\\\\)#.*$", ""); 
		messageWithoutComment = messageWithoutComment.replace("\\#", "#");
		
		if (langObj == null) { 
			noTranslate(message);
			return messageWithoutComment;
		}

		if(!langObj.containsKey(message)) {
			noTranslate(message);
			//IF NO SUITABLE TRANSLATION WITH THE COMMENT THEN RETURN WITHOUT COMMENT
			if(!langObj.containsKey(messageWithoutComment)) {
				return messageWithoutComment;
			} else {
				return langObj.get(messageWithoutComment).toString();
			}
		}
		// if "message" = isNull  - return message
		// if "massage" = "any string"  - return "any string" 
		String res = langObj.get(message).toString();
		if (res.isEmpty()) return (message);
		return res;		
	}
	
	private void noTranslate(String message) 
	{
		if(!noTranslateMap.containsKey(message)) {
			noTranslateMap.put(message, message);
		}
	}
	
	public Map<String, String> getNoTranslate() {
		return noTranslateMap;
	}
	
	public static JSONObject openLangFile(String filename)
	{
		JSONObject langJsonObject;
		
		try 
		{
		
			File file = new File( "lang/" + filename );
			if ( !file.isFile() ) {
				return (JSONObject) JSONValue.parse("");
			}
			
			List<String> lines = null;
			try {
				lines = Files.readLines(file, Charsets.UTF_8);
			} catch( IOException e ) {
				lines = new ArrayList<String>();
				e.printStackTrace();
			}
			
			String jsonString = "";
			for( String line : lines ){
				jsonString += line;
			}
			
			langJsonObject = (JSONObject) JSONValue.parse(jsonString);
		} catch ( Exception e ) {
			langJsonObject = new JSONObject();
		}
		
		if( langJsonObject == null ) {
			langJsonObject = new JSONObject();
		}

		if( langJsonObject.size() == 0 ) {
			System.out.println("ERROR reading language file " + filename + ".");	
		}
				
		return langJsonObject;
	};

	public List<Pair<String, String>> getListOfAvailable()
	{
		List<Pair<String, String>> lngList = new ArrayList<>();
		
		File[] fileList;        
        File f = new File("lang");
                
        fileList = f.listFiles();
                        
        for(int i=0; i<fileList.length; i++)           
        {
        	if(fileList[i].isFile() && fileList[i].getName().endsWith(".lng")) {
        		lngList.add(
        				new Pair<>(
        						fileList[i].getName(), 
        						(String)openLangFile(fileList[i].getName()).get("lang_name")
        						)
        				);
        	}
        	
        }
        
        return lngList;
	}
}