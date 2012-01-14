package de.catma.ui.client.ui.tagger.shared;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



public class TagInstance {
	
	private static enum SerializationField {
		body,
		instanceID,
		color,
		startPos,
		endPos,
		;
	}
	
	private String color;
	private String body;
	private String bodyURI;
	private String instanceID;
	private String targetURI;
	private List<TextRange> ranges;
	private String authorURI;

	public TagInstance(String body, String instanceID, String color, List<TextRange> ranges) {
		super();
		this.body = body;
		this.instanceID = instanceID;
		this.color = color;
		this.ranges = ranges;
	}
	
	public TagInstance(Map<String,Object> serializedEvent) {
		this.body = (String)serializedEvent.get(SerializationField.body.name());
		this.instanceID = (String)serializedEvent.get(SerializationField.instanceID.name());
		this.color = (String)serializedEvent.get(SerializationField.color.name());

		ranges = new ArrayList<TextRange>();
		int i=0;
		while(serializedEvent.containsKey(SerializationField.startPos.name()+i)){
			ranges.add(
				new TextRange(
					Integer.valueOf(
							(String)serializedEvent.get(SerializationField.startPos.name()+i)),
					Integer.valueOf(
							(String)serializedEvent.get(SerializationField.endPos.name()+i))));
			
			i++;
		}
	}
	
	public TagInstance(TagInstance tagInstanceToCopy, int base) {
		this(tagInstanceToCopy.body, tagInstanceToCopy.instanceID, 
				tagInstanceToCopy.color, new ArrayList<TextRange>());
		for (TextRange tr : tagInstanceToCopy.getRanges()) {
			ranges.add(new TextRange(tr.getStartPos()+base, tr.getEndPos()+base));
		}
	}

	public Map<String,Object> toMap() {
		Map<String, Object> result =
				new HashMap<String, Object>();
		
		int i=0;
		result.put(SerializationField.body.name(), body);
		result.put(SerializationField.instanceID.name(), instanceID);
		result.put(SerializationField.color.name(), color);

		for (TextRange tr : ranges) {
			result.put(SerializationField.startPos.name()+i, String.valueOf(tr.getStartPos()));
			result.put(SerializationField.endPos.name()+i, String.valueOf(tr.getEndPos()));
			i++;
		}
		
		return result;
	}

	public String getBody() {
		return body;
	}
	
	public String getInstanceID() {
		return instanceID;
	}
	
	public String getColor() {
		return color;
	}
	
	public List<TextRange> getRanges() {
		return ranges;
	}
	
	public String getTargetURI() {
		return targetURI;
	}
	
	public void setTargetURI(String targetURI) {
		this.targetURI = targetURI;
	}
	
	@Override
	public String toString() {
		return "#" + instanceID + " " + body + ((getRanges().size()>0)? getRanges().get(0) :"");
	}

	public void setAuthorURI(String authorURI) {
		this.authorURI = authorURI;
	}
	
	public String getAuthorURI() {
		return authorURI;
	}
}
