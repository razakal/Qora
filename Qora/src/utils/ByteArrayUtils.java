package utils;

import java.util.Arrays;
import java.util.List;

public class ByteArrayUtils {
	
	public static boolean  contains(List<byte[]> arrays, byte[] other) {
	    for (byte[] b : arrays)
	        if (Arrays.equals(b, other)) return true;
	    return false;
	}
	
	
	public static void remove(List<byte[]> list, byte[] toremove)
	{
		byte[] result = null;
		for (byte[] bs : list) {
			if(Arrays.equals(bs, toremove))
			{
				result = bs;
			}
		}
		
		list.remove(result);
	}

}
