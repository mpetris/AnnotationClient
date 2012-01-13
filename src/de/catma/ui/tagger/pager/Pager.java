package de.catma.ui.tagger.pager;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Pager {
	
	static final String LINE_CONTENT_PATTERN = 
			"(\\S+)|(\\p{Blank}+)|(\r\n|[\n\r\u2028\u2029\u0085])";
	
	static int WORDCHARACTER_GROUP = 1;
	static int WHITESPACE_GROUP = 2;
	static int LINE_SEPARATOR_GROUP = 3;


	private ArrayList<Page> pages;
	private int current;
	
	public Pager(String text, int approxMaxLineLength, int maxPageLengthInLines) {
		pages = new ArrayList<Page>();
		buildPages(text, approxMaxLineLength, maxPageLengthInLines);
	}

	private void buildPages(String text, int approxMaxlineLength, int maxPageLengthInLines) {
		Matcher matcher = Pattern.compile(LINE_CONTENT_PATTERN).matcher(text);

		int pageStart = 0;
		int pageEnd = 0;
		int pageLines = 0;
		
		int lineLength = 0;

		while(matcher.find()) {
			if (lineLength + matcher.group().length()>approxMaxlineLength) {
				pageLines++;
				pageEnd+=lineLength;
				lineLength = 0;
			}			
			
			if (pageLines >= maxPageLengthInLines) {
				pages.add(new Page(text.substring(pageStart, pageEnd), pageStart, pageEnd));
				pageLines = 0;
				pageStart = pageEnd;
			}

			lineLength += matcher.group().length();
			
			if (matcher.group(LINE_SEPARATOR_GROUP) != null) {
				pageLines++;
				pageEnd+=lineLength;
				lineLength = 0;
			}
		}
		
		if (lineLength != 0) {
			pageEnd+=lineLength;
			pageLines++;
		}
		if (pageLines != 0) {
			pages.add(new Page(text.substring(pageStart, pageEnd), pageStart, pageEnd));
		}
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (Page page : pages) {
			builder.append(page);
		}
		return builder.toString();
	}
	
}
