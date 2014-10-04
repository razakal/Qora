package utils;

import java.util.Comparator;

public class LongComparator implements Comparator<Long> {

	@Override
	public int compare(Long a, Long b) 
	{	
		//int one = Integer.parseInt(a);
		//int two = Integer.parseInt(b);
		
		return Long.compare(a, b);		
	}

}
