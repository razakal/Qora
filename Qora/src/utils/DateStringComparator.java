package utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;

import settings.Settings;

public class DateStringComparator implements Comparator<String> {

	private DateFormat format;
	
	public DateStringComparator()
	{
		String strTimeFormat = Settings.getInstance().getTimeFormat();
		
		if(strTimeFormat.equals("")) {
			this.format = DateFormat.getDateTimeInstance();
		} else {
			this.format = new SimpleDateFormat(strTimeFormat);
		}
	}
	
	@Override
	public int compare(String a, String b) 
	{	
		try
		{
			Date one = this.format.parse(a);
			Date two = this.format.parse(b);
			
			return one.compareTo(two);
		}
		catch(Exception e)
		{
			return 0;
		}
	}

}
