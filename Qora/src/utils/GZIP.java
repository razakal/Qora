package utils;

import gui.models.KeyValueTableModel;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import qora.crypto.Base64;


public class GZIP {
	private static byte[] GZIPcompress(String str) throws Exception {
		try( ByteArrayOutputStream obj= new ByteArrayOutputStream(); GZIPOutputStream gzip = new GZIPOutputStream(obj);)
		{
			gzip.write(str.getBytes(StandardCharsets.UTF_8));
			gzip.close();
			return obj.toByteArray();
		}
		
	}

	private static String GZIPdecompress(byte[] bytes) throws Exception {
		String outStr = "";
		try( GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));)
		{
			String line;
			while ((line=bf.readLine())!=null) {
				outStr += line + "\r\n";
			}
		}
		return outStr;
	}
	
	public static String getZippedCharacterCount(KeyValueTableModel namesModel) {
		return "Compressed Character Count:" + GZIP.compress(namesModel.getCurrentValueAsJsonStringOpt()).length() + "/4000";
	}
      
	public static String webDecompress(String value) 
	{
		if(value.startsWith("?gz!"))
		{
			value = value.substring(4, value.length());
	      	
			byte[] compressed = Base64.decode(value);
	          
			try {
				value = GZIPdecompress(compressed);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return value;
	}
	
	public static String autoDecompress(String text) 
	{
		if(text.startsWith("?gz!"))
        {
			text = text.substring(4, text.length());
            
        	byte[] compressed = Base64.decode(text);
        	try {
            	text = GZIPdecompress(compressed);
			} catch (Exception e) {
				e.printStackTrace();
			}
            return text;
        }
		else
		{
			return compress(text); 
		}	
	}

	public static String compress(String text) {
		byte[] compressed = null;
		try {
			compressed = GZIPcompress(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "?gz!"+Base64.encode(compressed);
	}
	
	public static String compressOnDemand(String text)
	{
		byte[] compressed = null;
		try {
			compressed = GZIPcompress(text);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String compressedVariant = "?gz!"+Base64.encode(compressed);
		
		if(compressedVariant.length()>= text.length())
		{
			compressedVariant = text;
		}
		
		return compressedVariant;
	}
	
}
