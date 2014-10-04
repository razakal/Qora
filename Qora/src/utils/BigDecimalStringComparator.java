package utils;

import java.math.BigDecimal;
import java.util.Comparator;

public class BigDecimalStringComparator implements Comparator<String> {

	
	@Override
	public int compare(String a, String b) 
	{	
		try
		{
			BigDecimal one = new BigDecimal(a);
			BigDecimal two = new BigDecimal(b);
			
			return one.compareTo(two);
		}
		catch(Exception e)
		{
			return 0;
		}
	}

}
