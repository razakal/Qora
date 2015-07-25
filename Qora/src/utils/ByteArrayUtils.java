package utils;

import java.util.Arrays;
import java.util.List;

public class ByteArrayUtils {

	public static boolean contains(List<byte[]> arrays, byte[] other) {
		for (byte[] b : arrays)
			if (Arrays.equals(b, other))
				return true;
		return false;
	}

	public static void remove(List<byte[]> list, byte[] toremove) {
		byte[] result = null;
		for (byte[] bs : list) {
			if (Arrays.equals(bs, toremove)) {
				result = bs;
				break;
			}
		}

		list.remove(result);
	}

	public static int indexOf(List<byte[]> list, byte[] bytearray) {
		int i = -1;
		for (int j = 0; j < list.size(); j++) {
			if (Arrays.equals(list.get(j), bytearray)) {
				i = j;
				break;
			}
		}

		return i;
	}

}
