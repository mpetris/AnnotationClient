package eu.interedition.annotationcomm;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import de.catma.ui.client.ui.tagger.shared.TextRange;

public class ConstraintServerConnection {
	
	private static enum Field {
		position,
		uri,
		constraint,
		;
	}
	
	private String constraintServerURLStr;

	public ConstraintServerConnection(String constraintServerURLStr) {
		this.constraintServerURLStr = constraintServerURLStr;
	}

	public TextRange validateConstraint(String uri, String constraintJson) 
			throws IOException, JSONException {
		JSONObject request = new JSONObject();
		request.put(Field.constraint.name(), new JSONObject(constraintJson));
		request.put(Field.uri.name(), uri);
		
		URLConnection urlConnection = openConnection("match");
		IOUtils.write(request.toString(), urlConnection.getOutputStream());
		
		InputStream is = urlConnection.getInputStream();
		
		String valiatedConstraint = IOUtils.toString(is);
		is.close();
		System.out.println("VALIDATED: " + valiatedConstraint);
		JSONObject constraint = new JSONObject(valiatedConstraint).getJSONObject("constraint");
		System.out.println("the constr: " + constraint);
//		String position = 
//				new JSONObject(constraint.getString(Field.constraint.name())).getString(Field.position.name());
		String position = 
				constraint.getString(Field.position.name());
		String[] positionValues = position.substring(5).split(",");
		
		return new TextRange(
				Integer.valueOf(positionValues[0]), 
				Integer.valueOf(positionValues[1]));
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
		
		IOUtils.write(request.toString(), urlConnection.getOutputStream());
		
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
