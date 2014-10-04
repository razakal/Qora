package utils;

import java.util.Comparator;

public class IntegerComparator implements Comparator<Integer> {

	@Override
	public int compare(Integer a, Integer b) 
	{	
		//int one = Integer.parseInt(a);
		//int two = Integer.parseInt(b);
		
		return Integer.compare(a, b);		
	}

}
