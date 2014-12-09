package utils;

import java.io.Serializable;
import java.util.Comparator;

public class StringComparator implements Comparator<String>, Serializable {

	private static final long serialVersionUID = 4919407223029419416L;

	@Override
	public int compare(String a, String b) 
	{	
		return a.compareTo(b);
	}

}
