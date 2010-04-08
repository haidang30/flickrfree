package com.zmosoft.flickrfree;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;

public class RestClient {

    public static String m_apikey = "";
    public static String m_secret = "";
    public static String m_fulltoken = "";

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

	public static JSONObject CallFunction(String methodName, String[] paramNames, String[] paramVals)
	{
		return CallFunction(methodName, paramNames, paramVals, true);
	}
	

	public static JSONObject CallFunction(String methodName, String[] paramNames, String[] paramVals, boolean authenticated)
	{
		JSONObject json = new JSONObject();
		HttpClient httpclient = new DefaultHttpClient();

		if (paramNames == null) {
			paramNames = new String[0];
		}
		if (paramVals == null) {
			paramVals = new String[0];
		}
		
		if (paramNames.length != paramVals.length) {
			return json;
		}

		String url = m_RESTURL + "?method=" + methodName
					+ "&api_key=" + m_apikey;
		for (int i = 0; i < paramNames.length; i++) {
			url += "&" + paramNames[i] + "=" + paramVals[i];
		}
		
		authenticated = authenticated && !m_fulltoken.equals("");
		if (authenticated) {
			url += "&auth_token=" + m_fulltoken;
		}
		
		// Generate the signature
		String signature = "";
		SortedMap<String,String> sig_params = new TreeMap<String,String>();
		sig_params.put("api_key", m_apikey);
		sig_params.put("method", methodName);
		sig_params.put("format", "json");
		for (int i = 0; i < paramNames.length; i++) {
			sig_params.put(paramNames[i],paramVals[i]);
		}
		if (authenticated) {
			sig_params.put("auth_token",m_fulltoken);
		}
		signature = m_secret;
		for (Map.Entry<String,String> entry : sig_params.entrySet()) {
			signature = signature + entry.getKey() + entry.getValue();
		}		
		try {
			signature = JavaMD5Sum.computeSum(signature).toLowerCase();
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		url += "&api_sig=" + signature + "&format=json";

		// Replace any spaces in the URL with "+".
		url = url.replace(" ", "+");
		
		// Prepare a request object
		HttpGet httpget = new HttpGet(url); 

		// Execute the request
		HttpResponse response;
		try {
			response = httpclient.execute(httpget);
			// Examine the response status

			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
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
			
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return json;
	}
	
    private static String m_RESTURL = "http://api.flickr.com/services/rest/";
}
