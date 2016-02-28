package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import lang.Lang;
import ntp.NTP;
import settings.Settings;

public class DateTimeFormat
{
	public static String timestamptoString(long timestamp){

		String timeZone = Settings.getInstance().getTimeZone(); 
		String strTimeFormat = Settings.getInstance().getTimeFormat();

		return timestamptoString(timestamp, strTimeFormat, timeZone);
	}
	
	public static String timestamptoString(long timestamp, String strTimeFormat, String timeZone){

		DateFormat dateFormat;
		
		Date date = new Date(timestamp);
		
		if(strTimeFormat.equals("")) {
			dateFormat = DateFormat.getDateTimeInstance();
		} else {
			dateFormat = new SimpleDateFormat(strTimeFormat);
		}
			
		if(!timeZone.equals("")) {
			dateFormat.setTimeZone( TimeZone.getTimeZone( timeZone ));
		}
		
		return dateFormat.format(date);
	}
	
	public static String timeAgo(long timestamp)
	{
		long now = NTP.getTime();
		
		long timeDiff = now - timestamp;

		if(timeDiff<60*1000)
		{
			return String.valueOf(timeDiff/1000) + Lang.getInstance().translate("s");
		}
		else if(timeDiff < 60*60*1000)  
		{
			return String.valueOf(timeDiff/(60*1000)) + Lang.getInstance().translate("m") + " " + String.valueOf(timeDiff/1000 - timeDiff/(60*1000)*60) + Lang.getInstance().translate("s");
		}
		else if(timeDiff<24*60*60*1000)  
		{
			return String.valueOf(timeDiff/(60*60*1000)) + Lang.getInstance().translate("h") + " " + String.valueOf(timeDiff/(60*1000) - timeDiff/(60*60*1000)*60) + Lang.getInstance().translate("m");
		}
		else
		{
			return String.valueOf(timeDiff/(24*60*60*1000)) + Lang.getInstance().translate("d") + " " + String.valueOf(timeDiff/(60*60*1000) - timeDiff/(24*60*60*1000)*24) + Lang.getInstance().translate("h");
		}
	}
}