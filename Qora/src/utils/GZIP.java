package utils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import qora.crypto.Base64;


public class GZIP {
	private static byte[] GZIPcompress(String str) throws Exception {
		ByteArrayOutputStream obj= new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(obj);
		gzip.write(str.getBytes("UTF-8"));
		gzip.close();
		return obj.toByteArray();
	}

	private static String GZIPdecompress(byte[] bytes) throws Exception {
		GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(bytes));
		BufferedReader bf = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
		String outStr = "";
		String line;
		while ((line=bf.readLine())!=null) {
			outStr += line + "\r\n";
		}
		return outStr;
	}
      
	public static String webDecompress(String Value) 
	{
		if(Value.startsWith("?gz!"))
		{
			Value = Value.substring(4, Value.length());
	      	
			byte[] compressed = Base64.decode(Value);
	          
			try {
				Value = GZIPdecompress(compressed);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return Value;
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
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return text;
        }
		else
		{
			byte[] compressed = null;
			try {
				compressed = GZIPcompress(text);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return "?gz!"+Base64.encode(compressed); 
		}	
	}
}
