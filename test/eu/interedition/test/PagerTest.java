package eu.interedition.test;

import java.net.URI;

import org.junit.Test;

import de.catma.ui.tagger.pager.Pager;
import eu.interedition.annotationclient.AnnotationTargetLoader;


public class PagerTest {

	@Test
	public void testPager() throws Exception {
		String uri = "http://www.gutenberg.org/cache/epub/11/pg11.txt";
		AnnotationTargetLoader annotationTargetLoader = 
				new AnnotationTargetLoader(
						new URI(uri));		
		Pager pager = new Pager(80, 50);
		pager.setText(annotationTargetLoader.getTargetText());
		
		System.out.println(pager);	
		
		
	}
}
