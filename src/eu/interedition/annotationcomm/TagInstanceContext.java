package eu.interedition.annotationcomm;

import org.json.JSONObject;

import de.catma.ui.client.ui.tagger.shared.TagInstance;

public class TagInstanceContext {

	private JSONObject constraint;
	private TagInstance tagInstance;
	public TagInstanceContext(JSONObject constraint, TagInstance tagInstance) {
		super();
		this.constraint = constraint;
		this.tagInstance = tagInstance;
	}
	public JSONObject getConstraint() {
		return constraint;
	}
	public TagInstance getTagInstance() {
		return tagInstance;
	}
	
	
}
