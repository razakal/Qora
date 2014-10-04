package utils;

import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

public class DateStringComparator implements Comparator<String> {

	private DateFormat format;
	
	public DateStringComparator()
	{
		this.format = DateFormat.getDateTimeInstance();
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
