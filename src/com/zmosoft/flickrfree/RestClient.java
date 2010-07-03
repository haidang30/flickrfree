package com.zmosoft.flickrfree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreProtocolPNames;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

public class RestClient {

    public static String m_apikey = "";
    public static String m_secret = "";
    public static String m_fulltoken = "";
    private static String m_UPLOADURL = "http://api.flickr.com/services/upload/";


    public static void setAuth(Activity activity) {
        m_apikey = activity.getResources().getString(R.string.apikey);
        m_secret = activity.getResources().getString(R.string.secret);
        m_fulltoken = activity.getSharedPreferences("Auth",0).getString("full_token", "");
    }
    
    private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}

	public static JSONObject UploadPicture(String filename, String title, String description, String tags,
									 boolean is_public, boolean is_friend, boolean is_family,
									 int safety_level){
		String safety = "1";
		if (safety_level > 0 && safety_level < 4) {
			safety = Integer.toString(safety_level);
		}

		Vector<String> pNames = new Vector<String>();
		Vector<String> pVals = new Vector<String>();

		pNames.add("photo");
		pVals.add("");
		if (!title.equals("")) {
			pNames.add("title");
			pVals.add(title);
		}
		if (!description.equals("")) {
			pNames.add("description");
			pVals.add(description);
		}
		if (!tags.equals("")) {
			pNames.add("tags");
			pVals.add(tags);
		}
		pNames.add("is_public");
		pVals.add(is_public ? "1" : "0");
		pNames.add("is_friend");
		pVals.add(is_friend ? "1" : "0");
		pNames.add("is_family");
		pVals.add(is_family ? "1" : "0");
		pNames.add("safety_level");
		pVals.add(safety);
		pNames.add("content_type");
		pVals.add("1");
		pNames.add("hidden");
		pVals.add("1");

		String [] paramNames, paramVals;
		paramNames = paramVals = new String[]{};
		paramNames = pNames.toArray(paramNames);
		paramVals = pVals.toArray(paramVals);
		
		return CallFunction("", paramNames, paramVals, true, true, filename);
	}
	
	public static JSONObject CallFunction(String methodName, String[] paramNames, String[] paramVals)
	{
		return CallFunction(methodName, paramNames, paramVals, true, false, "");
	}
	
	public static JSONObject CallFunction(String methodName, String[] paramNames, String[] paramVals, boolean authenticated)
	{
		return CallFunction(methodName, paramNames, paramVals, authenticated, false, "");
	}
	
	public static JSONObject CallFunction(String methodName, String[] paramNames, String[] paramVals,
										  boolean authenticated, boolean ispost, String filename)
	{
		JSONObject json = new JSONObject();
		HttpClient httpclient = new DefaultHttpClient();
	    httpclient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);

		if (paramNames == null) {
			paramNames = new String[0];
		}
		if (paramVals == null) {
			paramVals = new String[0];
		}
		
		if (paramNames.length != paramVals.length) {
			return json;
		}

		String url;
		// Set the base of the URL. If this is a POST upload, then use the upload
		// URL. Otherwise, use the standard REST URL and add the API key and parameter
		// names and values.
		if (ispost) {
			url = m_UPLOADURL;
		}
		else {
			url = m_RESTURL + "?method=" + methodName + "&api_key=" + m_apikey;
			for (int i = 0; i < paramNames.length; i++) {
				url += "&" + paramNames[i] + "=" + paramVals[i];
			}
		}

		// Check to see if this is an authenticated call and, if so, make sure
		// that there is a token.
		authenticated = authenticated && !m_fulltoken.equals("");
		
		// If properly authenticated, and if this is a GET call, add the
		// token to the URL.
		if (authenticated && !ispost) {
			url += "&auth_token=" + m_fulltoken;
		}
		
		// Generate the signature
		String signature = "";
		SortedMap<String,String> sig_params = new TreeMap<String,String>();
		sig_params.put("api_key", m_apikey);
		// Only add the method name to the signature if this is a GET call.
		// If it is a POST call, there is no method name.
		if (!ispost) {
			sig_params.put("method", methodName);
			sig_params.put("format", "json");
		}
		// Add the parameter names and values. If this is a POST upload,
		// do not add the "photo" parameter.
		for (int i = 0; i < paramNames.length; i++) {
			if (!ispost || !paramNames[i].equals("photo")) {
				sig_params.put(paramNames[i],paramVals[i]);
			}
		}
		if (authenticated) {
			sig_params.put("auth_token",m_fulltoken);
		}
		signature = m_secret;
		for (Map.Entry<String,String> entry : sig_params.entrySet()) {
			signature += entry.getKey() + entry.getValue();
		}		
		try {
			signature = JavaMD5Sum.computeSum(signature).toLowerCase();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		if (ispost) {
			sig_params.put("api_sig", signature);
		}
		else {
			url += "&api_sig=" + signature + "&format=json";
		}

		// Replace any spaces in the URL with "+".
		url = url.replace(" ", "+");
		
		HttpResponse response = null;

		try {
			// Prepare a request object
			if (ispost) {
			    File file = null;
			    // If this is a POST call, then it is a file upload. Check to see if a
			    // filename is given, and if so, open that file.
		    	if (ispost && !filename.equals("")) {
		    		file = new File(filename);
		    	}
			    
			    HttpPost httppost = new HttpPost(url);
			    MultipartEntity mp_entity = new MultipartEntity();

			    mp_entity.addPart("photo", new FileBody(file));
				for (Map.Entry<String,String> entry : sig_params.entrySet()) {
					mp_entity.addPart(entry.getKey(), new StringBody(entry.getValue()));
				}
			    httppost.setEntity(mp_entity);
				
				response = httpclient.execute(httppost);
				HttpEntity resEntity = response.getEntity();
			    if (resEntity != null) {
			    	resEntity.consumeContent();
				}

			}
			else {
				HttpGet httpget = new HttpGet(url);
				response = httpclient.execute(httpget);
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (!ispost) {
			try {
				// Get hold of the response entity
				HttpEntity entity = null;
				if (response != null) {
					entity = response.getEntity();
				}
	
				// If the response does not enclose an entity, there is no need
				// to worry about connection release
				if (entity != null) {
					// A Simple JSON Response Read
					InputStream instream = entity.getContent();
					String result = convertStreamToString(instream);
					result = result.substring(result.indexOf("{"),result.lastIndexOf("}") + 1);
					// A Simple JSONObject Creation
					json = new JSONObject(result);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		httpclient.getConnectionManager().shutdown();

		return json;
	}
	
    private static String m_RESTURL = "http://api.flickr.com/services/rest/";
}
