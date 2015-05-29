package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

public class BuildTime
{
	private static String bufgetBuildDateTime = "";

	public static String getBuildTime(){
		bufgetBuildDateTime = getClassBuildTime();
		return bufgetBuildDateTime.substring(bufgetBuildDateTime.indexOf(" ")+1);
	}
	
	public static String getBuildDate(){
		bufgetBuildDateTime = getClassBuildTime();
		return bufgetBuildDateTime.substring(0, bufgetBuildDateTime.indexOf(" "));
	}
	
	public static String getBuildDateTime(){
		return getClassBuildTime();
	}
	
	private static String getClassBuildTime() {
		Date d = null;
		if(bufgetBuildDateTime.equals(""))
	    {
			//GET BUILD DATE FOR COMPILED VERSION
			File file = new File("Qora.jar");
	    	if(file.exists())
	    	{
		    	try {
					@SuppressWarnings("resource")
					JarFile jf = new JarFile(file);
					ZipEntry ze = jf.getEntry("Start.class");
					d = new Date(ze.getTime ());

					SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					//f.setTimeZone(TimeZone.getTimeZone("UTC"));
					bufgetBuildDateTime = f.format(d);
		    	} catch (IOException e) {
					e.printStackTrace();
				}
		    }
	    	else
	    	{
	    		//GET BUILD DATE FOR DEBUG VERSION
	    		Class<?> currentClass = new Object() {}.getClass().getEnclosingClass();
	    		URL resource = currentClass.getResource(currentClass.getSimpleName() + ".class");
	    		if (resource != null) 
	    		{
	    			if (resource.getProtocol().equals("file")) 
	    			{
	    				try 
	    				{
	    					d = new Date(new File(resource.toURI()).lastModified());
	    					SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	    					f.setTimeZone(TimeZone.getTimeZone("UTC"));
	    					bufgetBuildDateTime = f.format(d);
	    					
	    		        } catch (URISyntaxException ignored) { }
	    			}  
	    		}
	    	}
	    }
	    return bufgetBuildDateTime;
	}
}