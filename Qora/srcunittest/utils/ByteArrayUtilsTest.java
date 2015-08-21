package utils;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import qora.crypto.Base58;

public class ByteArrayUtilsTest {

	@Test
	public void testRemove() {
		List<byte[]> bslist = new ArrayList<byte[]>();

		byte[] decode = Base58
				.decode("38Wdm3bCw1KQzEREEd6UQDSZ9UBhPTSz1xa9cQkgJmX2iTK6BixBE2jAifaWFYCgYreWuykpMwix2JiCNE3y8xSD");
		bslist.add(decode);

		assertEquals(bslist.size(), 1);

		byte[] decode2 = Base58
				.decode("38Wdm3bCw1KQzEREEd6UQDSZ9UBhPTSz1xa9cQkgJmX2iTK6BixBE2jAifaWFYCgYreWuykpMwix2JiCNE3y8xSD");
		ByteArrayUtils.remove(bslist, decode2);

		assertEquals(bslist.size(), 0);
	}

}
