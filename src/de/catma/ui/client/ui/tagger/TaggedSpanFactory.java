package de.catma.ui.client.ui.tagger;

import java.util.Date;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;

public class TaggedSpanFactory {

	private String instanceID;
	private int instanceReferenceCounter = 1;
	private String color;
	
	public TaggedSpanFactory(String color) {
		this(String.valueOf(new Date().getTime()), color);
	}
	
	public TaggedSpanFactory(String instanceID, String color) {
		super();
		this.instanceID = instanceID;
		this.color = color;
	}

	public Element createTaggedSpan(String innerHtml) {
		Element taggedSpan = DOM.createSpan();
		String style = 
				"display:inline-block; border-bottom:5px; border-bottom-color:#" 
				+ color
				+ ";border-bottom-style:solid;";
		
		taggedSpan.setAttribute("style", style);
		taggedSpan.setId(instanceID + "_" + instanceReferenceCounter++);
		taggedSpan.setInnerHTML(innerHtml);
		return taggedSpan;
	}

	public String getInstanceID() {
		return instanceID;
	}

	public String getColor() {
		return color;
	}
	
}
