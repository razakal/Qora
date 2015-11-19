package test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import utils.DiffHelper;

public class DiffhelperTest {
	
	
	@Test
	public void testDiffs() throws Exception {
		
		String source = "skerberus\nvbcs\n" + "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8";
		String destination = "skerberus\nvrontis\nvbcs\n" + "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8";
		String diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		destination = "skerberus";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		
		destination = "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8" + "asdf\nwayne";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		destination = "\uAA75" + "\uBCFA" + "\u5902" + "\u2ed8" + "asdf\nwayne\n \na\ne\ndffdkf";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
	}
	
}
