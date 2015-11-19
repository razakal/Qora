package utils;

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

public class BlogUtilsTest {

	@Test
	public void testGetHashTags() {
		
		List<String> hashTags = BlogUtils.getHashTags("");
		assertEquals(hashTags.size(), 0);
		
		
		hashTags = BlogUtils.getHashTags("#Test and so \non #skerberus2k qwertz");
		assertEquals(hashTags.size(), 2);
		assertEquals(hashTags.get(0), "#Test");
		assertEquals(hashTags.get(1), "#skerberus2k");
		
		hashTags = BlogUtils.getHashTags(" #Test #qwertz");
		assertEquals(hashTags.size(), 2);
		assertEquals(hashTags.get(0), "#Test");
		assertEquals(hashTags.get(1), "#qwertz");
		
		
		
	}
	
	@Test
	public void testGetBlogTags() throws Exception {
		List<String> blogTags = BlogUtils.getBlogTags("");
		assertEquals(blogTags.size(), 0);
		
		
		blogTags = BlogUtils.getBlogTags("@Test and #so \non @skerberus2k qwertz");
		assertEquals(blogTags.size(), 2);
		assertEquals(blogTags.get(0), "@Test");
		assertEquals(blogTags.get(1), "@skerberus2k");
		
		blogTags = BlogUtils.getBlogTags(" @Test @qwertz");
		assertEquals(blogTags.size(), 2);
		assertEquals(blogTags.get(0), "@Test");
		assertEquals(blogTags.get(1), "@qwertz");
	}

}
