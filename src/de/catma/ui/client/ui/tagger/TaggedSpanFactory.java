package de.catma.ui.client.ui.tagger;

import java.util.Date;

import com.google.gwt.dom.client.Element;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Random;

public class TaggedSpanFactory {

	private String instanceID;
	private int instanceReferenceCounter = 1;
	private int colorCode;
	
	public TaggedSpanFactory() {
		this(String.valueOf(new Date().getTime()));
	}
	
	public TaggedSpanFactory(String instanceID) {
		super();
		this.instanceID = instanceID;
		this.colorCode = Random.nextInt(11)+1;
	}

	public Element createTaggedSpan(String innerHtml) {
		Element taggedSpan = DOM.createSpan();
		taggedSpan.addClassName("tag_"+colorCode);
		taggedSpan.setId(instanceID + "_" + instanceReferenceCounter++);
		taggedSpan.setInnerHTML(innerHtml);
		return taggedSpan;
	}

	public String getInstanceID() {
		return instanceID;
	}
	
}
