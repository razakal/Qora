package test;

import static org.junit.Assert.*;
import kryo.DiffHelper;

import org.junit.Test;

public class DiffhelperTest {
	
	
	@Test
	public void testDiffs() throws Exception {
		
		String source = "skerberus\nvbcs";
		String destination = "skerberus\nvrontis\nvbcs";
		String diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		destination = "skerberus";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		
		destination = "asdf\nwayne";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
		
		destination = "asdf\nwayne\n \na\ne\ndffdkf";
		
		diff = DiffHelper.getDiff(source, destination);
		
		assertEquals(destination, DiffHelper.patch(source, diff));
	}
	
}
