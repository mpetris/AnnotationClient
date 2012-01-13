package eu.interedition.test;

import org.junit.Test;

import de.catma.ui.tagger.pager.Pager;


public class PagerTest {

	@Test
	public void testPager() throws Exception {
		
		String data = "This eBook is for the use of anyone anywhere at no cost and with\nalmost no restrictions whatsoever.  You may copy it, give it away or\nre-use it under the terms of the Project Gutenberg License included\nwith this eBook or online at www.gutenberg.org";
		
		Pager pager = new Pager(data, 80, 2);
		
		System.out.println(pager);	
		
		
	}
}
