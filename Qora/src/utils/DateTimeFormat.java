package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import settings.Settings;

public class DateTimeFormat
{
	public static String timestamptoString(long timestamp){

		String timeZone = Settings.getInstance().getTimeZone(); 
		String strTimeFormat = Settings.getInstance().getTimeFormat();
		
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
}