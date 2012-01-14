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

	public static String server = "50.56.215.106:80";
	public static void main (String[] args) throws Exception {
//		String server = "87.106.12.254:3000";

		// STEP 1 create annotation body
		// 66 67
		String json = "{ \"mime_type\" : \"text/html\", \"content\" : \"This is a <b>bold</b> annotation!\" }"; 
		System.out.println(putUrlJson("http://"+server+"/annotation_bodies", json));

//		{"annotation_body":{"annotation_id":null,"content":"This is a <b>bold</b> annotation!","created_at":"2012-01-14T10:45:20Z","id":130,"mime_type":"text/html","updated_at":"2012-01-14T10:45:20Z","uri":"http://50.56.215.106/annotation_bodies/130"}}

//		http://87.106.12.254:3000/annotation_bodies/46
//		http://50.56.215.106/annotation_bodies/3


		// STEP 2 link annotation body to annotation
		//http://50.56.215.106/annotations/query?q=http://example.com/texts/Alice_in_Wonderland
		String anno = "{ \"author_uri\" : \"http://people.org/asaf_bartov\", " +
				"\"body_uri\" : \"http://50.56.215.106/annotation_bodies/67\", " +
				"\"targets\" : [ { \"uri\" : \"http://google.com/abc\", " +
				"\"constraint\" : { \"constraint_type\" : \"xpath\", \"constraint\" : \"1\" } } ] } ";

//		System.out.println(putUrlJson("http://"+server+"/annotations", anno));
		// 7, 8 -> 66

		//{"annotation":{"author_uri":"http://people.org/asaf_bartov","created_at":"2012-01-13T21:22:17Z","id":6,"updated_at":"2012-01-13T21:22:17Z","uri":"http://50.56.215.106/annotations/6"}}



		// STEP 3 fetch an annotation
//		System.out.println(fetchUrlJson("http://"+server+"/annotations/66"));
//		System.out.println(fetchUrlJson("http://"+server+"/annotation_bodies/66"));



		// STEP 4 query annotations
		String s = "http://"+server+"/annotations/query?q=http://google.com/abc";
//		getAnnotations(s);
//	URL url = new URL ("http://"+server+"/annotation_bodies/3");

//		System.out.println(getSingleAnnotation("http://"+server+"/annotations/7"));
//		System.out.println(getSingleAnnotation("http://"+server+"/annotation_bodies/66"));

//		[{"annotation":{"author_uri":"http://people.org/asaf_bartov","created_at":"2012-01-13T20:50:48Z","id":3,
//		"updated_at":"2012-01-13T20:50:48Z","uri":null}},
//		{"annotation":{"author_uri":"http://people.org/asaf_bartov","created_at":"2012-01-13T20:51:16Z","id":4,
//		"updated_at":"2012-01-13T20:51:16Z","uri":null}},
//		{"annotation":{"author_uri":"http://people.org/asaf_bartov","created_at":"2012-01-13T20:53:42Z","id":5,
//		"updated_at":"2012-01-13T20:53:42Z","uri":null}},
//		{"annotation":{"author_uri":"http://people.org/asaf_bartov","created_at":"2012-01-13T21:22:17Z","id":6,
//		"updated_at":"2012-01-13T21:22:17Z","uri":"http://50.56.215.106/annotations/6"}}]

	}

	private static String putUrlJson (String urlStr, String json) throws IOException {
		URL url = new URL (urlStr);
		URLConnection conn = url.openConnection();
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

	private static String fetchJson (String urlStr) throws IOException {
		return putUrlJson(urlStr, null);
	}

	public static TagInstance getSingleAnnotation(String uri, String instanceID) throws IOException {
		try {
			String s = fetchJson(uri); // get the annotation object
			debug(s);
			JSONObject x = new JSONObject(s);

			JSONObject anno = x.getJSONObject("annotation");
			String body_uri = anno.getJSONObject("annotation_body").getString("uri");
			debug("body uri: " + body_uri);
			JSONArray arr = anno.getJSONArray("annotation_target_instances");
			String constraint0 = null;
			try {
				constraint0 = new JSONObject(arr.getJSONObject(0).getJSONObject("annotation_target_instance").getJSONObject("annotation_constraint").getString("constraint")).getString("position");
			}
			catch(Exception e) {
				constraint0 = arr.getJSONObject(0).getJSONObject("annotation_target_instance").getJSONObject("annotation_constraint").getString("constraint");
			}
			debug ("constraint " + constraint0);

			s = fetchJson(body_uri); // get the annotation body object
			debug(s);
			JSONObject bodyJson = new JSONObject (s);
			JSONObject annoBody = bodyJson.getJSONObject("annotation_body");
			String content = annoBody.getString("content");
			debug(content);
			String body_id = Integer.toString(annoBody.getInt("annotation_id"));
			debug(body_id);
			List<TextRange> textRanges = new ArrayList<TextRange> (1);
			String item0 = constraint0.split(",")[0];
			String[] ar = item0.split("-");
			int start = 1;
			int end = 1;
			if (ar != null && ar.length > 0) {
				start = Integer.parseInt(ar[0]);
				if (ar.length > 1)
					end = Integer.parseInt(ar[1]);
			}
			textRanges.add(new TextRange(start, end));
			TagInstance rval = new TagInstance (content, instanceID, "FF0000", textRanges); //new TextRange(1, 2));
			// 2,5,8,12 ==> 2-5  8-12
			return rval;
		} catch (org.json.JSONException e) {
			throw new IOException (e.getMessage(), e);
		}
	}

	public static int putAnnotation (TagInstance annotation) throws IOException {
 		String json = "{ \"mime_type\" : \"text/html\", \"content\" : \""+annotation.getBody()+"\" }"; 

		try {
			JSONObject bodyJson = new JSONObject(putUrlJson("http://" + server + "/annotation_bodies", json));
			String body_uri = bodyJson.getJSONObject("annotation_body").getString("uri");

			String sep = "";
			StringBuilder constraint = new StringBuilder(64);
			for (TextRange r : annotation.getRanges()) {
				constraint.append(sep);
				constraint.append(r.getStartPos());
				constraint.append('-');
				constraint.append(r.getEndPos());
				sep = ",";
			}
			String anno = "{ \"author_uri\" : \"http://people.org/asaf_bartov\", " +
					"\"body_uri\" : \""+body_uri+"\", " +
					"\"targets\" : [ { \"uri\" : \""+annotation.getTargetURI()+"\", " +
					"\"constraint\" : { \"constraint_type\" : \"RFC5147\", \"constraint\" : \""+constraint.toString()+"\" } } ] } ";

			putUrlJson("http://"+server+"/annotations", anno);
		} catch (JSONException e) {
			throw new IOException (e.getMessage(), e);
		}
		// annotation_body.uri
		return 0; // RFC5147
	}

	public static List<TagInstance> getAnnotations (String uri) throws IOException {
		List<TagInstance> rval = new ArrayList<TagInstance> (8);
		try {
			String jsonStr = fetchJson("http://"+server+"/annotations/query?q=" + uri);

			JSONArray jsonArray = new org.json.JSONArray(jsonStr);
			//System.out.println(jsonArray.length());
			for (int i = 0; i < jsonArray.length(); i++) {
				int id = (jsonArray.getJSONObject(i).getJSONObject("annotation").getInt("id"));
				String singleuri = (jsonArray.getJSONObject(i).getJSONObject("annotation").getString("uri"));
				TagInstance tag = getSingleAnnotation (singleuri, Integer.toString(id));
				rval.add(tag);
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
