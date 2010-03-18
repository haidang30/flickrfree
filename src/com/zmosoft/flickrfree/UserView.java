package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class UserView extends Activity implements OnItemClickListener, OnClickListener {

	private class GetExtraInfoTask extends AsyncTask<Object, Object, Object> {
		@Override
		protected Object doInBackground(Object... params) {
			// The "nsid" string contains the User ID of the user that this view represents.
			// Look in the "extras" bundle for that ID. If it doesn't exist, then nsid will
			// be an empty string, indicating no user.
			String nsid = m_extras.containsKey("nsid") ? m_extras.getString("nsid") : "";

			try {
				// Get the user's buddy icon and display it.
				if (m_userinfo != null) {
					int iconserver = m_userinfo.getJSONObject("person").getInt("iconserver");
					int iconfarm = m_userinfo.getJSONObject("person").getInt("iconfarm");
					String icon_url = "";
					if (iconserver > 0 && iconfarm > 0) {
						icon_url = "http://farm"
									+ iconfarm
									+ ".static.flickr.com/"
									+ iconserver + "/buddyicons/"
									+ nsid + ".jpg";
					}
					else {
						icon_url = "http://www.flickr.com/images/buddyicon.jpg";
					}
					
					if (icon_url != "" && GlobalResources.CacheImage(icon_url, m_activity, false)) {
						Bitmap buddyicon = GlobalResources.GetCachedImage(icon_url, m_activity);
						if (buddyicon != null) {
							publishProgress(buddyicon);
						}
					}
				}
			} catch (JSONException e) {
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
			
			// Get the number of accounts and publish that result to the listview.
			int nAccounts = 0;
			String nAccounts_str = "";
			SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
			Map<String, ?> auth_prefs_map = auth_prefs.getAll();
			for (String key : auth_prefs_map.keySet()) {
				if (key.contains("FlickrUsername_")) {
					++nAccounts;
				}
			}
			if (nAccounts > 0) {
				nAccounts_str = nAccounts + " Account";
				if (nAccounts > 1) {
					nAccounts_str += "s";
				}
			}
			publishProgress(m_actionnames[ACTION_ACCOUNTS], nAccounts_str);
			
			// Get the number of photos in the user's photostream and publish that
			// result to the listview.
			try {
				String nPhotos_str = "";
				if (m_userinfo != null) {
					int nPhotos = m_userinfo.getJSONObject("person")
					   .getJSONObject("photos")
					   .getJSONObject("count")
					   .getInt("_content");
					if (nPhotos > 0) {
						nPhotos_str = nPhotos + " Photo";
						if (nPhotos > 1) {
							nPhotos_str += "s";
						}
					}
					publishProgress(m_actionnames[ACTION_PHOTOSTREAM], nPhotos_str);
				}
			} catch (JSONException e) {
			}
			
			if (!nsid.equals("")) {
				// Get the number of sets in the user's account and publish that
				// result to the listview.
				try {
					String nSets_str = "";
					m_photosets = RestClient.CallFunction("flickr.photosets.getList",
														  new String[]{"user_id"},
														  new String[]{nsid});
					if (m_photosets != null) {
						int nSets = m_photosets.getJSONObject("photosets")
						   					   .getJSONArray("photoset").length();
						if (nSets > 0) {
							nSets_str = nSets + " Set";
							if (nSets > 1) {
								nSets_str += "s";
							}
						}
						publishProgress(m_actionnames[ACTION_SETS], nSets_str);
					}
				} catch (JSONException e) {
				}

				try {
					String nCollections_str = "";
					m_collections = RestClient.CallFunction("flickr.collections.getTree",
														  new String[]{"user_id"},
														  new String[]{nsid});
					if (m_collections != null) {
						int nCollections = m_collections.getJSONObject("collections")
						   .getJSONArray("collection").length();
						if (nCollections > 0) {
							nCollections_str = nCollections + " Collection";
							if (nCollections > 1) {
								nCollections_str += "s";
							}
						}
						publishProgress(m_actionnames[ACTION_COLLECTIONS], nCollections_str);
					}
				} catch (JSONException e) {
				}

				String nTags_str = "";
				int nTags = !m_tags.equals("") ? m_tags.split(" ").length : 0;
				if (nTags > 0) {
					nTags_str = nTags + " Tag";
					if (nTags > 1) {
						nTags_str += "s";
					}
				}
				publishProgress(m_actionnames[ACTION_TAGS], nTags_str);

				try {
					String nFavorites_str = "";
					m_favorites = RestClient.CallFunction(GlobalResources.isAppUser(m_activity, nsid) 
														  ? "flickr.favorites.getList"
														  : "flickr.favorites.getPublicList",
														  new String[]{"user_id"},
														  new String[]{nsid});
					if (m_favorites != null) {
						int nFavorites = Integer.valueOf(m_favorites.getJSONObject("photos").getString("total"));
						if (nFavorites > 0) {
							nFavorites_str = nFavorites + " Favorite";
							if (nFavorites > 1) {
								nFavorites_str += "s";
							}
						}
						publishProgress(m_actionnames[ACTION_FAVORITES], nFavorites_str);
					}
				} catch (JSONException e) {
				}
			}

			return null;
		}
		
		@Override
		protected void onProgressUpdate (Object... values) {
			if (values.length > 1 && values[0] instanceof String && values[1] instanceof String) {
				String action = (String)values[0];
				String info = (String)values[1];
				
				ListView listview = (ListView)findViewById(R.id.UserListView);
				HashMap <String, String> m = new HashMap<String, String>();
				if (m_extrainfomap.containsKey(action)) {
					if (info.equals("")) {
						listview.getChildAt(m_extrainfomap.get(action)).setEnabled(false);
					}
					else {
						listview.getChildAt(m_extrainfomap.get(action)).setEnabled(true);
						m = new HashMap<String, String>();
						m.put("action_name", action);
						m.put("extra_info", info);
						m_extrainfolist.set(m_extrainfomap.get(action), m);
					}
					((SimpleAdapter)listview.getAdapter()).notifyDataSetChanged();
				}
			}
			if (values.length == 1 && values[0] instanceof Bitmap) {
				((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap((Bitmap)values[0]);	
			}
		}
		
		@Override
		protected void onPreExecute() {
	    	setProgressBarIndeterminateVisibility(true);
		}

		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarIndeterminateVisibility(false);
		}
	}
	
	// Issues an API call to Flickr to retrieve information about a
	// given user. That information is stored as a JSON object called
	// m_userinfo.
	private class GetUserInfoTask extends AsyncTask<Bundle, Void, Object> {
		
		@Override
		protected Object doInBackground(Bundle... params) {
			Bundle extras = params.length > 0 ? params[0] : null;
			
			if (extras == null) {
				return null;
			}

			try {
				CheckAuthentication(getSharedPreferences("Auth",0));
				String nsid = extras.getString("nsid");
				m_userinfo = GetUserInfo(nsid);
				m_tags = GetTags(nsid);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			ClearUserDisplay();
	    	setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarIndeterminateVisibility(false);
	    	try {
				SetUserDisplay();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		private JSONObject GetUserInfo(String nsid) {
			JSONObject userinfo = new JSONObject();
			
			if (!nsid.equals("")) {
				userinfo = RestClient.CallFunction("flickr.people.getInfo",
												    new String[]{"user_id"},
													new String[]{nsid});
			}
			
			return userinfo;
		}
		
	    private String GetTags(String nsid) throws JSONException {
			String tags_str = "";
			JSONObject tag_obj = RestClient.CallFunction("flickr.tags.getListUser", new String[]{"user_id"}, new String[]{nsid});
			if (tag_obj.has("who")) {
				JSONArray tag_arr = tag_obj.getJSONObject("who").getJSONObject("tags").getJSONArray("tag");
				for (int i = 0; i < tag_arr.length(); i++) {
					tags_str = tags_str + tag_arr.getJSONObject(i).getString("_content");
					if (i < tag_arr.length() - 1) {
						tags_str = tags_str + " ";
					}
				}
	    	}
	    	return tags_str;
	    }
	}
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.userview);

		((ListView)findViewById(R.id.UserListView)).setOnItemClickListener(this);
//		((CheckBox)findViewById(R.id.CheckBoxFriend)).setOnClickListener(this);
//		((CheckBox)findViewById(R.id.CheckBoxFamily)).setOnClickListener(this);

        // If getExtras() returns null, then this is the root activity.
        // Create a new Bundle for m_extras, and see if we can find an
        // nsid to put in it.
    	m_extras = getIntent().getExtras();
    	if (m_extras == null) {
    		m_extras = new Bundle();
    		m_extras.putString("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
    	}

		refresh();
	}
	
	private void refresh() {
        m_extrainfotask = null;
        m_photosets = null;
        m_favorites = null;
        
		m_actionnames = getResources().getStringArray(R.array.main_user_view_list);
    	
       	new GetUserInfoTask().execute(m_extras);
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GlobalResources.MANAGE_ACCOUNTS_REQ) {
			SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
			m_extras.putString("nsid", auth_prefs.getString("nsid", ""));

	        m_photosets = null;
	        m_favorites = null;
	        ((ImageView)findViewById(R.id.BuddyIcon)).setImageBitmap(null);
        	new GetUserInfoTask().execute(m_extras);
		}
	}
	
	// This method takes the authentication token stored in memory and checks it against
	// the Flickr API.
	private boolean CheckAuthentication(SharedPreferences auth_prefs) throws JSONException {
        boolean auth_ok = false;
        
    	GlobalResources.m_fulltoken = auth_prefs.getString("full_token", "");
    	
        if (!GlobalResources.m_fulltoken.equals("")) {
        	// If there is a token, then check to make sure it is still valid. 
			JSONObject json_obj = RestClient.CallFunction("flickr.auth.checkToken",null,null);
			auth_ok = json_obj.getString("stat").equals("ok");
        }

        // If the authentication failed, then the token is invalid, so clear it and set
        // the app to a logged-out state.
        if (!auth_ok) {
			AuthenticateActivity.LogOut(auth_prefs);
			m_extras.putString("nsid","");
		}
		
		return auth_ok;
	}

	private void ClearUserDisplay() {
		((TextView)findViewById(R.id.TextUsername)).setText("");
		
		ListView lv = ((ListView)findViewById(R.id.UserListView));
		lv.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, new String[]{}));
		lv.setTextFilterEnabled(true);
	}

	private void SetUserDisplay() throws JSONException {
		String nsid = "";
		String username = "";
		String location = "";
		JSONObject userinfo = new JSONObject();
		int array_resource = R.array.no_user_view_list;
		if (m_userinfo.has("stat") && m_userinfo.getString("stat").equals("ok")) {
			userinfo = m_userinfo.getJSONObject("person");
			nsid = userinfo.getString("nsid");
		}
		
    	if (nsid.equals("")) {
    		m_usertype = UsrType.NOUSER;
    	}
    	else {
    		m_usertype  = GlobalResources.isAppUser(this,nsid)
    					? UsrType.APPUSER
    					: UsrType.OTHERUSER;
        	username = userinfo.getJSONObject("username").getString("_content");
        	if (userinfo.has("location")) {
            	location = userinfo.getJSONObject("location").getString("_content");
        	}
    	}

//		LinearLayout cl = (LinearLayout)findViewById(R.id.LayoutContact);
//		cl.setVisibility(View.GONE);
		if (m_usertype == UsrType.APPUSER) {
			//TODO: Set background color of R.id.TextUsernameLabel.
			array_resource = R.array.main_user_view_list;
    	}
    	else if (m_usertype == UsrType.OTHERUSER) {
   			array_resource = R.array.user_view_list;

//    		cl.setVisibility(View.VISIBLE);
//    		boolean contact = userinfo.has("contact")
//    						&& userinfo.getInt("contact") == 1;
//   			boolean friend = userinfo.has("friend")
//   							&& userinfo.getInt("friend") == 1;
//   			boolean family = userinfo.has("family")
//   							&& userinfo.getInt("family") == 1;
//   			((CheckBox)findViewById(R.id.CheckBoxContact)).setChecked(contact);
//   			if (contact) {
//   				((CheckBox)findViewById(R.id.CheckBoxFriend)).setEnabled(true);
//   				((CheckBox)findViewById(R.id.CheckBoxFamily)).setEnabled(true);
//	   			((CheckBox)findViewById(R.id.CheckBoxFriend)).setChecked(friend);
//	   			((CheckBox)findViewById(R.id.CheckBoxFamily)).setChecked(family);
//   			}
//   			else {
//   				((CheckBox)findViewById(R.id.CheckBoxFriend)).setEnabled(false);
//   				((CheckBox)findViewById(R.id.CheckBoxFamily)).setEnabled(false);
//   			}
    	}

		((TextView)findViewById(R.id.TextUsername)).setText(username);
		((TextView)findViewById(R.id.TextLocation)).setText(location);
		
		m_extrainfolist = new ArrayList < Map<String,String> >();
		m_extrainfomap = new HashMap <String, Integer>();
		String[] action_array = getResources().getStringArray(array_resource);
		Map<String, String> m;
		for (int i = 0; i < action_array.length; i++) {
			m = new HashMap<String, String>();
			m.put("action_name", action_array[i]);
			m.put("extra_info", "");
			m_extrainfolist.add(m);
			m_extrainfomap.put(action_array[i], i);
		}
		
		ListView lv = ((ListView)findViewById(R.id.UserListView));
        lv.setAdapter(new SimpleAdapter(
							this,
							m_extrainfolist,
							R.layout.userview_list_item,
							new String[]{"action_name","extra_info"},
							new int[]{R.id.ActionTitle, R.id.ExtraInfo}));
        m_extrainfotask = new GetExtraInfoTask();
        m_extrainfotask.execute();
	}

	@Override
	public void onClick(View v) {
//		if (v.getId() == R.id.CheckBoxContact) {
//			
//		}
//		else if (v.getId() == R.id.CheckBoxFriend) {
//			
//		}
//		else if (v.getId() == R.id.CheckBoxFamily) {
//			
//		}
	}
	
	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String command = ((TextView)view.findViewById(R.id.ActionTitle)).getText().toString();
		if (command.equals(m_actionnames[ACTION_ACCOUNTS])) {
			Intent i = new Intent(this, AccountView.class);
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivityForResult(i,GlobalResources.MANAGE_ACCOUNTS_REQ);
		}
		else if (command.equals(m_actionnames[ACTION_PHOTOSTREAM])) {		
			Intent i = new Intent(this, ImageGrid.class);
			i.putExtra("type", "photostream");
			i.putExtra("nsid", m_extras.getString("nsid"));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_SETS])) {
			Intent i = new Intent(this, ImageSets.class);
			if (m_photosets == null) {
				i.putExtra("type","by_nsid");
				i.putExtra("nsid", m_extras.getString("nsid"));
			}
			else {
				i.putExtra("type","by_setlist");
				try {
					i.putExtra("setlist", m_photosets.getJSONObject("photosets").getJSONArray("photoset").toString());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_COLLECTIONS])) {
			//TODO: This is borken right now. Fix it.
			Intent i = new Intent(this, ImageCollections.class);
			i.putExtra("nsid", m_extras.getString("nsid"));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_TAGS])) {
			Intent i = new Intent(this, ImageTags.class);
			i.putExtra("nsid", m_extras.getString("nsid"));
			i.putExtra("tags", m_tags);
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_FAVORITES])) {
			Intent i = new Intent(this, ImageGrid.class);
			try {
				i.putExtra("type", "favorites");
				i.putExtra("nsid", m_extras.getString("nsid"));
				if (m_favorites != null) {
					i.putExtra("title", "Favorites for \"" + GlobalResources.getNameFromNSID(m_extras.getString("nsid")) + "\"");
					i.putExtra("list_obj", m_favorites.toString());
				}
				if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
					m_extrainfotask.cancel(true);
					while (!m_extrainfotask.isCancelled()) {
						GlobalResources.sleep(50);
					}
				}
				startActivity(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (command.equals(m_actionnames[ACTION_CONTACTS])) {
			Intent i = new Intent(this, ContactsView.class);
			i.putExtra("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
		else if (command.equals(m_actionnames[ACTION_SEARCH])) {
			Intent i = new Intent(this, SearchView.class);
			i.putExtra("nsid", getSharedPreferences("Auth",0).getString("nsid", ""));
			if (m_extrainfotask != null && m_extrainfotask.getStatus() != AsyncTask.Status.FINISHED) {
				m_extrainfotask.cancel(true);
				while (!m_extrainfotask.isCancelled()) {
					GlobalResources.sleep(50);
				}
			}
			startActivity(i);
		}
	}

    private enum UsrType {
    	APPUSER, OTHERUSER, NOUSER;
    }

    static final int ACTION_ACCOUNTS = 0;
    static final int ACTION_PHOTOSTREAM = 1;
    static final int ACTION_SETS = 2;
    static final int ACTION_COLLECTIONS = 3;
    static final int ACTION_TAGS = 4;
    static final int ACTION_FAVORITES = 5;
    static final int ACTION_CONTACTS = 6;
    static final int ACTION_SEARCH = 7;
    
	Bundle m_extras;
	Activity m_activity = this;
	UsrType m_usertype;
	HashMap <String, Integer> m_extrainfomap;
	List < Map<String,String> > m_extrainfolist;
	GetExtraInfoTask m_extrainfotask;
	String[] m_actionnames;
	String m_tags;
	JSONObject m_userinfo;
	JSONObject m_photosets;
	JSONObject m_collections;
	JSONObject m_favorites;
}
