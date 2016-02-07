package utils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Date;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import controller.Controller;

public class BuildTime
{
	private static long bufgetBuildDateTime = 0;

	public static String getBuildDateTimeString(){
		return DateTimeFormat.timestamptoString(getBuildTimestamp(), "yyyy-MM-dd HH:mm:ss z", "UTC");
	}
	
	public static long getBuildTimestamp(){
		if(bufgetBuildDateTime == 0)
	    {
			bufgetBuildDateTime = getClassBuildTime(); 
	    }
	    return bufgetBuildDateTime;
	}
	
	private static long getClassBuildTime() {
		Date d = null;
		
		long buildDateTimeStamp = 0;
		
		//GET BUILD DATE FOR COMPILED VERSION
		File file = new File("Qora.jar");
    	if(file.exists())
    	{
	    	try {
				@SuppressWarnings("resource")
				JarFile jf = new JarFile(file);
				ZipEntry ze = jf.getEntry("META-INF/MANIFEST.MF");
				d = new Date(ze.getTime ());
				buildDateTimeStamp = d.getTime();
	    	} catch (IOException e) {
				e.printStackTrace();
			}
	    }
    	else
    	{
    		//GET BUILD DATE FOR DEBUG VERSION
    		
    		URL resource = Controller.class.getResource(Controller.class.getSimpleName() + ".class");
    		
    		if (resource != null) 
    		{
    			if (resource.getProtocol().equals("file")) 
    			{
    				try 
    				{
    					d = new Date(new File(resource.toURI()).lastModified());
    					buildDateTimeStamp = d.getTime();
    		        } catch (URISyntaxException ignored) { }
    			}  
    		}
    	}
	    
	    return buildDateTimeStamp;
	}
}