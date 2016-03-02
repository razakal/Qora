package utils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import org.json.simple.JSONObject;

public class SaveStrToFile {
	
	public static void save(String path, String str) throws IOException 
	{
		Files.write(Paths.get(path), 
				str.getBytes(StandardCharsets.UTF_8), 
				StandardOpenOption.WRITE);
	} 

	public static void saveJsonFine(String path, JSONObject json) throws IOException 
	{
		save(path, StrJSonFine.convert(json));
	}
}
