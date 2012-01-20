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

import de.catma.backgroundservice.ProgressListener;
import de.catma.ui.client.ui.tagger.shared.TagInstance;
import de.catma.ui.client.ui.tagger.shared.TextRange;

/**
 * @author Jim Connor
 * 
 * @author marco.petris@web.de (modifications)
 *
 */
public class AnnotationServerConnection {

	private String annotationServer = "http://demo.interedition.eu/raxld/";
	private String constraintServer = "http://demo.interedition.eu/fragment-context/";
//	private String constraintServer = "http://localhost:8080/fragment-context/";
	
	
	public AnnotationServerConnection(String annotationServer,
			String constraintServer) {
		super();
		this.annotationServer = annotationServer;
		this.constraintServer = constraintServer;
	}

	private String putUrlJson (String urlStr, String json) throws IOException {
		URL url = new URL (urlStr);
		URLConnection conn = url.openConnection();
		conn.setDoInput(true);
		conn.setDoInput(true);
		boolean doOutput = json != null;
		conn.setDoOutput(doOutput);
		conn.setRequestProperty("Accept", "application/json");
		if (doOutput) {
			conn.setRequestProperty("content-type", "application/json");
			conn.getOutputStream().write(json.getBytes());
		}

		InputStream is = conn.getInputStream();
		String jsonStr = IOUtils.toString(is);
		is.close();
		
		return jsonStr;
	}


	private String fetchJson (String urlStr) throws IOException {
		return putUrlJson(urlStr, null);
	}

	private TagInstanceContext getSingleAnnotation(String uri, String instanceID) throws IOException {
		try {
			String result = fetchJson(uri); // get the annotation object
			
			JSONObject jsonResult = new JSONObject(result);

			JSONObject anno = jsonResult.getJSONObject("annotation");
			String body_uri = anno.getJSONObject("annotation_body").getString("uri");
			
			JSONArray arr = anno.getJSONArray("annotation_target_instances");
			
			Logger.getLogger(getClass().getName()).info("annotation_target_instances: " + arr);
			
			JSONObject annotationTargetInstance = arr.getJSONObject(0).getJSONObject(
					"annotation_target_instance");

			JSONObject annotationConstraint = 
					annotationTargetInstance.getJSONObject("annotation_constraint");
			
			JSONObject constraint = 
					new JSONObject(annotationConstraint.getString("constraint"));
			
			String constraintType = 
					annotationConstraint.getString("constraint_type");
			
			if ((constraintType != null) && (constraintType.equals("RFC_5147"))) {
				String position = 
						constraint.getString("position");
				int start = (position.contains("char=")? 5 : 0); //Hack
				String[] positionValues = position.substring(start).split(",");
				
				result = fetchJson(body_uri); // get the annotation body object
				
				JSONObject bodyJson = new JSONObject (result);
				JSONObject annoBody = bodyJson.getJSONObject("annotation_body");
				
				String mimeType = annoBody.getString("mime_type");
				String color = "FF0000";
				String content = annoBody.getString("content");
				
				if (mimeType.equals("application/json")) {
					JSONObject contentJSON = new JSONObject(content);
					if (contentJSON.has("color")) {
						color = contentJSON.getString("color");
					}
					if (contentJSON.has("html")) {
						content = contentJSON.getString("html");
					}
				}
				
				String body_id = Integer.toString(annoBody.getInt("annotation_id"));
				
				List<TextRange> textRanges = new ArrayList<TextRange> (1);
				textRanges.add(new TextRange(
						Integer.valueOf(positionValues[0]), 
						Integer.valueOf(positionValues[1])));
				TagInstance ti = new TagInstance (content, instanceID, color, textRanges); 
	
				return new TagInstanceContext(constraint, ti);
			}
			
			return null;
			
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			//throw new IOException (e.getMessage(), e);
		}
	}

	public String putAnnotation (TagInstance annotation) throws IOException, JSONException {
		
		JSONObject json = new JSONObject();
		json.put("mime_type", "application/json");
		JSONObject content = new JSONObject();
		content.put("html", annotation.getBody());
		content.put("color", annotation.getColor());
		json.put("content", content.toString());
		
		try {
			JSONObject bodyJson =
					new JSONObject(
							putUrlJson(annotationServer + "annotation_bodies", json.toString()));
			String body_uri = bodyJson.getJSONObject("annotation_body").getString("uri");
			
			ConstraintServerConnection constraintServerConnection = 
					new ConstraintServerConnection(this.constraintServer);
			
			JSONObject constraintResult = 
					new JSONArray(constraintServerConnection.createConstraint(
							annotation.getTargetURI(), annotation.getRanges().get(0))).getJSONObject(0);
			
			JSONObject annotationServerArgs = new JSONObject();
			
			annotationServerArgs.put("author_uri", annotation.getAuthorURI());
			annotationServerArgs.put("body_uri", body_uri);
			
			JSONObject target = new JSONObject();
			target.put("uri", annotation.getTargetURI());
			JSONObject constraint = new JSONObject();
			constraint.put("constraint_type", constraintResult.getString("constraint_type"));
			constraint.put("constraint", constraintResult.getJSONObject("constraint").toString());
			target.put("constraint", constraint);
			
			JSONArray targets = new JSONArray();
			targets.put(target);
			
			annotationServerArgs.put("targets", targets);
			
			String annotation_uri = 
					putUrlJson(
						annotationServer+"annotations", annotationServerArgs.toString());
			
			return annotation_uri;
			
		} catch (JSONException e) {
			e.printStackTrace();
			throw new IOException (e.getMessage(), e);
		}
	}

	public List<TagInstance> getAnnotations (String uri, ProgressListener progressListener) throws IOException {
		Logger.getLogger(this.getClass().getName()).info("starting to load annotations");

		progressListener.setIndeterminate(true, "Loading annotations...");

		List<TagInstanceContext> tagInstanceContexts = new ArrayList<TagInstanceContext> ();
		try {
			String jsonStr = fetchJson(annotationServer+"annotations/query?q=" + uri);
			Logger.getLogger(this.getClass().getName()).info("Result from annotaton server: " + jsonStr );
			JSONArray jsonArray = new JSONArray(jsonStr);
			
			for (int i = 0; i < jsonArray.length(); i++) {
				int id = (jsonArray.getJSONObject(i).getJSONObject("annotation").getInt("id"));
				String singleuri = (jsonArray.getJSONObject(i).getJSONObject("annotation").getString("uri"));
				TagInstanceContext tagIntanceContext = getSingleAnnotation (singleuri, Integer.toString(id));
				if (tagIntanceContext != null) {
					tagInstanceContexts.add(tagIntanceContext);
				}
			}
			Logger.getLogger(this.getClass().getName()).info("finished loading of annotations");
			progressListener.setIndeterminate(false, "Finished loading of annotations!");

			ConstraintServerConnection constraintServerConnection = 
					new ConstraintServerConnection(constraintServer);
			Logger.getLogger(this.getClass().getName()).info("starting to validate constraints");
			progressListener.setIndeterminate(true, "Validating constraints...");

			List<TagInstance> validatedTagInstances = 
					constraintServerConnection.validateConstraints(uri, tagInstanceContexts);
			Logger.getLogger(this.getClass().getName()).info("finished validation of constraints");
			progressListener.setIndeterminate(false, "Finished validation of constraints!");

			return validatedTagInstances;
		} catch (Exception e) {
			e.printStackTrace();
			throw new IOException (e.getMessage(), e);
		}
	}
}
