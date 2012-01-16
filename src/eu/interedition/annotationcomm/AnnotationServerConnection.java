package eu.interedition.annotationcomm;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import de.catma.ui.client.ui.tagger.shared.TagInstance;
import de.catma.ui.client.ui.tagger.shared.TextRange;

public class AnnotationServerConnection {

	private String annotationServer = "50.56.215.106:80";
//	private String constraintServer = "http://87.106.12.254:8182/oac-constraint/";
	private String constraintServer = "http://localhost:8182/oac-constraint/";
	
	public AnnotationServerConnection() {
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
		byte[] buf = new byte[32768];
		int cnt = 0;
		for (int n = 0; (n = is.read(buf, cnt, buf.length - cnt)) >= 0; cnt += n) {
			System.out.println(n);
//			System.out.println("snippet: " + new String(buf, cnt, n));
		}

		String jsonStr = new String(buf,0,cnt);

		return jsonStr;
	}

	private String fetchJson (String urlStr) throws IOException {
		return putUrlJson(urlStr, null);
	}

	public TagInstance getSingleAnnotation(String uri, String instanceID) throws IOException {
		try {
			String s = fetchJson(uri); // get the annotation object
			debug(s);
			JSONObject x = new JSONObject(s);

			JSONObject anno = x.getJSONObject("annotation");
			String body_uri = anno.getJSONObject("annotation_body").getString("uri");
			debug("body uri: " + body_uri);
			JSONArray arr = anno.getJSONArray("annotation_target_instances");
			System.out.println("arr value: "+ arr);
			JSONObject annotationTargetInstance = arr.getJSONObject(0).getJSONObject(
					"annotation_target_instance");
			String targetURI = annotationTargetInstance.getJSONObject("annotation_target_info").getString("uri");
			
			JSONObject annotationConstraint = 
					annotationTargetInstance.getJSONObject("annotation_constraint");
			
			ConstraintServerConnection constraintServerConnection = new ConstraintServerConnection(constraintServer);
			
			TextRange validatedRange = 
					constraintServerConnection.validateConstraint(
							targetURI, annotationConstraint.getString("constraint"));
			
			s = fetchJson(body_uri); // get the annotation body object
			debug(s);
			JSONObject bodyJson = new JSONObject (s);
			JSONObject annoBody = bodyJson.getJSONObject("annotation_body");
			String content = annoBody.getString("content");
			debug(content);
			String body_id = Integer.toString(annoBody.getInt("annotation_id"));
			debug(body_id);
			List<TextRange> textRanges = new ArrayList<TextRange> (1);
			textRanges.add(validatedRange);
			TagInstance rval = new TagInstance (content, instanceID, "FF0000", textRanges); //new TextRange(1, 2));
			// 2,5,8,12 ==> 2-5  8-12
			return rval;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
			//throw new IOException (e.getMessage(), e);
		}
	}

	public int putAnnotation (TagInstance annotation) throws IOException {
 		String json = "{ \"mime_type\" : \"text/html\", \"content\" : \""+annotation.getBody()+"\" }"; 

		try {
			JSONObject bodyJson = new JSONObject(putUrlJson("http://" + annotationServer + "/annotation_bodies", json));
			String body_uri = bodyJson.getJSONObject("annotation_body").getString("uri");
			System.out.println("body_uri: " + body_uri);
			
			ConstraintServerConnection constraintServerConnection = 
					new ConstraintServerConnection(this.constraintServer);
			
			JSONObject constraintResult = 
					new JSONObject(constraintServerConnection.createConstraint(
							annotation.getTargetURI(), annotation.getRanges().get(0)));
			
			
			System.out.println( "constr: " + constraintResult);
//			String anno = "{ \"author_uri\" : \"http://people.org/asaf_bartov\", " +
//					"\"body_uri\" : \""+body_uri+"\", " +
//					"\"targets\" : [ { \"uri\" : \""+annotation.getTargetURI()+"\", " +
//					"\"constraint\" : { \"constraint_type\" : \"RFC5147\", \"constraint\" : \""+constraint.toString()+"\" } } ] } ";

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
			
//			String anno = "{ \"author_uri\" : \""+ annotation.getAuthorURI() + "\", " +
//				"\"body_uri\" : \""+body_uri+"\", " +
//				"\"targets\" : [ { \"uri\" : \""+annotation.getTargetURI()+"\", " +
//				"\"constraint\" : "+ new JSONObject(constraintResult).getJSONObject("constraint") +" } ] } ";

			String annotation_uri = 
					putUrlJson("http://"+annotationServer+"/annotations", annotationServerArgs.toString());
			System.out.println("anno_uri" + annotation_uri);
		} catch (JSONException e) {
			e.printStackTrace();
			throw new IOException (e.getMessage(), e);
		}
		
		return 0; // RFC5147
	}

	public List<TagInstance> getAnnotations (String uri) throws IOException {
		List<TagInstance> rval = new ArrayList<TagInstance> (8);
		try {
			String jsonStr = fetchJson("http://"+annotationServer+"/annotations/query?q=" + uri);

			JSONArray jsonArray = new org.json.JSONArray(jsonStr);
			//System.out.println(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				int id = (jsonArray.getJSONObject(i).getJSONObject("annotation").getInt("id"));
				String singleuri = (jsonArray.getJSONObject(i).getJSONObject("annotation").getString("uri"));
				TagInstance tag = getSingleAnnotation (singleuri, Integer.toString(id));
				if (tag != null) {
					rval.add(tag);
				}
			}
		} catch (MalformedURLException e) {
			throw new IOException (e.getMessage(), e);
		} catch (org.json.JSONException je) {
			throw new IOException (je.getMessage(), je);
		}

		return rval;
	}

	private static boolean debugFlag = true;
	private static void debug (String s) {
		if (debugFlag)
			System.out.println(s);
	}	
	
}
