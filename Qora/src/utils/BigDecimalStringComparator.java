package utils;

import java.math.BigDecimal;
import java.util.Comparator;

public class BigDecimalStringComparator implements Comparator<String> {

	
	@Override
	public int compare(String a, String b) 
	{	
		try
		{
			String a2 = a.replace("," , "");
			String b2 = b.replace("," , "");
					
			BigDecimal one = new BigDecimal(a2);
			BigDecimal two = new BigDecimal(b2);
			return one.compareTo(two);
		}
		catch(Exception e)
		{
			return 0;
		}
	}

}
