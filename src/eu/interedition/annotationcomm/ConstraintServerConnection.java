package eu.interedition.annotationcomm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.catma.ui.client.ui.tagger.shared.TagInstance;
import de.catma.ui.client.ui.tagger.shared.TextRange;

/**
 * @author marco.petris@web.de
 *
 */
public class ConstraintServerConnection {
	
	private static enum State {
		modified,
		success,
		failure,
		;
	}
	
	private static enum Field {
		state,
		position,
		uri,
		constraint, 
		context,
		;
	}
	
	private String constraintServerURLStr;

	public ConstraintServerConnection(String constraintServerURLStr) {
		this.constraintServerURLStr = constraintServerURLStr;
	}

	public List<TagInstance> validateConstraints(String uri, List<TagInstanceContext> tagInstanceContexts) 
			throws IOException, JSONException {

		List<TagInstance> result = new ArrayList<TagInstance>();

		StringBuilder request = new StringBuilder("[");
		String conc = "";
		
		for (TagInstanceContext tic : tagInstanceContexts) {
			JSONObject curConstraint = tic.getConstraint();
			
			if (curConstraint.has(Field.context.name()) 
					&& (!curConstraint.getString(Field.context.name()).isEmpty())) {
				JSONObject curRequest = new JSONObject();
				curRequest.put(Field.constraint.name(), tic.getConstraint());
				curRequest.put(Field.uri.name(), uri);
				request.append(conc);
				request.append(curRequest.toString());
				conc = ",";
			}
			else {
				result.add(tic.getTagInstance());
			}
		}
		
		request.append("]");
		
		URLConnection urlConnection = openConnection("match");
		IOUtils.write(request.toString(), urlConnection.getOutputStream());
		
		InputStream is = urlConnection.getInputStream();
		
		String validatedConstraints = IOUtils.toString(is);
		Logger.getLogger(getClass().getName()).info("ConstraintSever validation response: " + validatedConstraints);
		is.close();
		List<TagInstanceContext> toBeRemoved = new ArrayList<TagInstanceContext>();
		
		JSONArray validatedConstraintsJSON = new JSONArray(validatedConstraints);
		for (int i=0; i<validatedConstraintsJSON.length();i++) {
			JSONObject validatedConstraint = validatedConstraintsJSON.getJSONObject(i);
			String state = validatedConstraint.getString(Field.state.name());
			if (state.equals(State.failure.name())) {
				toBeRemoved.add(tagInstanceContexts.get(i));
			}
			else if (state.equals(State.modified.name())) {
				JSONObject constraint = validatedConstraint.getJSONObject(Field.constraint.name());
				
				String position = 
						constraint.getString(Field.position.name());
				String[] positionValues = position.substring(5).split(",");
				TagInstance ti = tagInstanceContexts.get(i).getTagInstance();
				ti.getRanges().clear();
				ti.getRanges().add(new TextRange(
						Integer.valueOf(positionValues[0]), 
						Integer.valueOf(positionValues[1])));
			}
		}
		
		
		for (TagInstanceContext tic : tagInstanceContexts) {
			if (!toBeRemoved.contains(tic)) {
				result.add(tic.getTagInstance());
			}
		}
		
		return result;
	}
	
	public String createConstraint(String uri, TextRange textRange) 
			throws IOException, JSONException {
		
		URLConnection urlConnection = openConnection("create");
		
		JSONObject request = new JSONObject();
		request.put(Field.uri.name(), uri);
		
		JSONObject constraintArgs = new JSONObject();
		constraintArgs.put(
				Field.position.name(), 
				"char="+textRange.getStartPos()+","+textRange.getEndPos());
		
		request.put(Field.constraint.name(), constraintArgs);
		JSONArray requestArray = new JSONArray();
		requestArray.put(request);
		
		IOUtils.write(requestArray.toString(), urlConnection.getOutputStream());
		
		InputStream is = urlConnection.getInputStream();
		
		String constraint = IOUtils.toString(is);
		is.close();
		
		return constraint;
	}
	
	private URLConnection openConnection(String command) throws IOException {
		URL constraintServerURL = new URL(constraintServerURLStr+command);
		URLConnection urlConnection = constraintServerURL.openConnection();
		urlConnection.setDoOutput(true);
		urlConnection.setRequestProperty("Accept", "application/json");
		urlConnection.setRequestProperty("content-type", "application/json");
		return urlConnection;
	}
}
