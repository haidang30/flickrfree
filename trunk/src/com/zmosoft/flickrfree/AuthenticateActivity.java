package com.zmosoft.flickrfree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class AuthenticateActivity extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    	auth_prefs = getSharedPreferences("Auth",0);
		setResult(Activity.RESULT_CANCELED);

        setContentView(R.layout.authenticate);
        
		((Button)findViewById(R.id.btnAuthenticate)).setEnabled(checkAuthCode());

		((Button)findViewById(R.id.btnAuthenticate)).setOnClickListener(this);
        ((Button)findViewById(R.id.btnGetCode)).setOnClickListener(this);
        
        ((EditText)findViewById(R.id.authnum1)).addTextChangedListener(
        		new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {
						if (s.toString().length() == 3) {
							((EditText)findViewById(R.id.authnum2)).requestFocus();
						}
						((Button)findViewById(R.id.btnAuthenticate)).setEnabled(checkAuthCode());
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
        			
        		}
        );

        ((EditText)findViewById(R.id.authnum2)).addTextChangedListener(
        		new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {
						if (s.toString().length() == 3) {
							((EditText)findViewById(R.id.authnum3)).requestFocus();
						}
						((Button)findViewById(R.id.btnAuthenticate)).setEnabled(checkAuthCode());
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
        			
        		}
        );

        ((EditText)findViewById(R.id.authnum3)).addTextChangedListener(
        		new TextWatcher() {

					@Override
					public void afterTextChanged(Editable s) {
						((Button)findViewById(R.id.btnAuthenticate)).setEnabled(checkAuthCode());
					}

					@Override
					public void beforeTextChanged(CharSequence s, int start,
							int count, int after) {
					}

					@Override
					public void onTextChanged(CharSequence s, int start,
							int before, int count) {
					}
        			
        		}
        );
    }
    
    public boolean checkAuthCode() {
		return (((EditText)findViewById(R.id.authnum1)).getText().toString().length() == 3
				&& ((EditText)findViewById(R.id.authnum2)).getText().toString().length() == 3
				&& ((EditText)findViewById(R.id.authnum3)).getText().toString().length() == 3);
    }
    
    public void onClick(View v) {
    	if (v.getId() == R.id.btnAuthenticate) {
    		String miniToken;
    		miniToken = ((EditText)findViewById(R.id.authnum1)).getText().toString()
    					+ "-" + ((EditText)findViewById(R.id.authnum2)).getText().toString()
    					+ "-" + ((EditText)findViewById(R.id.authnum3)).getText().toString();
    		
	        String[] paramNames = {"mini_token"};
	        String[] paramVals = {miniToken};
			JSONObject json_obj = RestClient.CallFunction("flickr.auth.getFullToken",paramNames,paramVals);
			try {
				// Check that authentication was successful
				if (json_obj.getString("stat").equals("ok")) {
					// Retrieve the username and fullname from the object.
					String username = json_obj.getJSONObject("auth").getJSONObject("user").getString("username");
					String fullname = json_obj.getJSONObject("auth").getJSONObject("user").getString("fullname");
					
					// Get the "Auth" Shared preferences object to save authentication information
					auth_prefs = getSharedPreferences("Auth",0);
					
					// Get the editor for auth_prefs
					SharedPreferences.Editor auth_prefs_editor = auth_prefs.edit();
					
					// Save all of the current authentication information. This will be the default account
					// the next time the app is started.
					auth_prefs_editor.putString("full_token", json_obj.getJSONObject("auth").getJSONObject("token").getString("_content"));
					auth_prefs_editor.putString("perms", json_obj.getJSONObject("auth").getJSONObject("perms").getString("_content"));
					auth_prefs_editor.putString("nsid", json_obj.getJSONObject("auth").getJSONObject("user").getString("nsid"));
					auth_prefs_editor.putString("username", username);
					auth_prefs_editor.putString("realname", fullname);
					auth_prefs_editor.putString("displayname", fullname.equals("") ? username : fullname + " (" + username + ")");
					
					// Save the entire JSON Authentication object under the username so it can be retrieved
					// when switching accounts.
					auth_prefs_editor.putString("FlickrUsername_" + username, json_obj.toString());
					
					// Attempt to save all changes to Shared Preferences. If successful, set result to RESULT_OK.
					if (auth_prefs_editor.commit()) {
						setResult(Activity.RESULT_OK);
					}
				}
				finish();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	else if (v.getId() == R.id.btnGetCode) {
    		startActivity(new Intent(Intent.ACTION_VIEW,
    				Uri.parse(GlobalResources.m_AUTHURL)));
    	}
    }
    
    public static void SetActiveUser(SharedPreferences prefs, String username) {
    	try {
			SharedPreferences.Editor prefs_editor = prefs.edit();
			String user_obj_str = username.equals("") ? "" : prefs.getString("FlickrUsername_" + username, "");

			if (user_obj_str.equals("")) {
				AuthenticateActivity.LogOut(prefs);
			}
			else {
				JSONObject user_obj = new JSONObject(user_obj_str);
				
				// Retrieve the full name from the object.
				String fullname = user_obj.getJSONObject("auth").getJSONObject("user").getString("fullname");

				// Save all of the current authentication information. This will be the default account
				// the next time the app is started.
				prefs_editor.putString("full_token", user_obj.getJSONObject("auth").getJSONObject("token").getString("_content"));
				prefs_editor.putString("perms", user_obj.getJSONObject("auth").getJSONObject("perms").getString("_content"));
				prefs_editor.putString("nsid", user_obj.getJSONObject("auth").getJSONObject("user").getString("nsid"));
				prefs_editor.putString("username", username);
				prefs_editor.putString("realname", fullname);
				prefs_editor.putString("displayname", fullname.equals("") ? username : fullname + " (" + username + ")");
				prefs_editor.commit();
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    public static void RemoveUser(SharedPreferences prefs, String username) {
		// Get the editor for prefs
		SharedPreferences.Editor prefs_editor = prefs.edit();
		
		prefs_editor.remove("FlickrUsername_" + username);
		prefs_editor.commit();
		if (prefs.getString("username", "").equals(username)) {
			AuthenticateActivity.LogOut(prefs);
		}
    }
    
    public static void LogOut(SharedPreferences prefs) {
		// Get the editor for prefs
		SharedPreferences.Editor prefs_editor = prefs.edit();
		
		prefs_editor.remove("full_token");
		prefs_editor.remove("perms");
		prefs_editor.remove("nsid");
		prefs_editor.remove("username");
		prefs_editor.remove("realname");
		prefs_editor.remove("displayname");
		prefs_editor.commit();
    }
    
    public static void ExportAuth(SharedPreferences auth_prefs, String path) {
    	Map<String, ?> m = auth_prefs.getAll();
		try {
			File f = new File(path);
			if (!f.exists()) {
				f.createNewFile();
			}
			FileOutputStream of = new FileOutputStream(f);
	    	for (String key : m.keySet()) {
	    		if (key.contains("FlickrUsername_")) {
	    			new PrintStream(of).println(key + " : " + m.get(key).toString());
	    		}
	    	}
	    	of.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public static void ImportAuth(SharedPreferences auth_prefs, String path) {
		try {
			BufferedReader read_buf = new BufferedReader(new FileReader(path));
			SharedPreferences.Editor auth_prefs_edit = auth_prefs.edit();
			String s = read_buf.readLine();
			String[] parsed = null;
			while (s != null) {
				parsed = s.split(" : ", 2);
				if (parsed.length == 2 && parsed[0].contains("FlickrUsername_")) {
					auth_prefs_edit.putString(parsed[0], parsed[1]);
				}
				s = read_buf.readLine();
			}
			auth_prefs_edit.commit();
			read_buf.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	SharedPreferences auth_prefs;
}
