package com.zmosoft.flickrfree;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.TreeMap;

public class ExternalHooks extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        //TEMPORARILY DISABLE EXTERNAL HOOKS UNTIL ALL CONTROLS ARE IN PLACE.
        if (false) {
            Intent intent = getIntent();
            Bundle extras = intent.getExtras();
			if (intent.getAction().equals(GlobalResources.INTENT_GET_PHOTOSTREAM)
				|| intent.getAction().equals(GlobalResources.INTENT_GET_POOL)
				|| intent.getAction().equals(GlobalResources.INTENT_FLICKR_SEARCH)) {
				
				JSONObject json_obj = null;
				int page = extras.containsKey("page") ? extras.getInt("page") : 0;
				int per_page = extras.containsKey("per_page") ? extras.getInt("per_page") : 0;
				GlobalResources.ImgSize img_size = GlobalResources.ImgSize.MED;
				if (extras.containsKey("size") && extras.getString("size") != null) {
					if (extras.getString("size").equals("smallsquare")) {
						img_size = GlobalResources.ImgSize.SMALLSQUARE;
					}
					else if (extras.getString("size").equals("thumb")) {
						img_size = GlobalResources.ImgSize.THUMB;
					}
					else if (extras.getString("size").equals("small")) {
						img_size = GlobalResources.ImgSize.SMALL;
					}
					else if (extras.getString("size").equals("medium")) {
						img_size = GlobalResources.ImgSize.MED;
					}
					else if (extras.getString("size").equals("large")) {
						img_size = GlobalResources.ImgSize.LARGE;
					}
					else if (extras.getString("size").equals("original")) {
						img_size = GlobalResources.ImgSize.ORIG;
					}
				}
				
				if (intent.getAction().equals(GlobalResources.INTENT_GET_PHOTOSTREAM)) {
					String user_id = null;
					if (extras.containsKey("user_id") && extras.getString("user_id") != null) {
						user_id = extras.getString("user_id");
					}
					else if (extras.containsKey("username") && extras.getString("username") != null) {
						try {
							user_id = APICalls.getNSIDFromName(extras.getString("username"));
						} catch (JSONException e) {
						}
					}
					
					json_obj = (user_id != null) ? APICalls.peopleGetPhotos(user_id, page, per_page) : null;
				}
				else if (intent.getAction().equals(GlobalResources.INTENT_GET_POOL)) {
					String group_id = null;
					if (extras.containsKey("group_id") && extras.getString("group_id") != null) {
						group_id = extras.containsKey("group_id") ? extras.getString("group_id") : null;
					}
					else if (extras.containsKey("group_url") && extras.getString("group_url") != null) {
						String[] group_info = APICalls.getGroupInfoFromURL(extras.getString("group_url"));
						group_id = (group_info.length > 1) ? group_info[1] : null;
					}
					
					json_obj = (group_id != null) ? APICalls.groupsPoolsGetPhotos(group_id, page, per_page) : null;
				}
				else if (intent.getAction().equals(GlobalResources.INTENT_GET_FAVORITES)) {
					json_obj = APICalls.favoritesGetList(page, per_page);
				}
				else if (intent.getAction().equals(GlobalResources.INTENT_FLICKR_SEARCH)) {
					String text = extras.containsKey("text") ? extras.getString("text") : null;
					String tags = extras.containsKey("tags") ? extras.getString("tags") : null;
					String user_id = extras.containsKey("user_id") ? extras.getString("user_id") : null;
	
					if (text != null || tags != null || user_id != null) {
						json_obj = APICalls.photosSearch(user_id, text, tags, page, per_page);
					}
				}
	
				if (json_obj == null) {
					setResult(RESULT_CANCELED);
				}
				else {
					JSONArray imglist = JSONParser.getArray(json_obj, "photos/photo");
					ArrayList<String> img_urls = new ArrayList<String>();
					String[] img_urls_arr = new String[]{};
					JSONObject img_obj = null;
					for (int i = 0; i < imglist.length(); i++) {
							try {
		    					img_obj = imglist.getJSONObject(i);
								img_urls.add(GlobalResources.getImageURL(img_obj.getString("farm"),
											                             img_obj.getString("server"),
											                             img_obj.getString("id"),
											                             img_obj.getString("secret"),
											                             img_size, "jpg"));
							} catch (JSONException e) {
								setResult(RESULT_CANCELED);
							}
					}
					setResult(RESULT_OK, (new Intent()).putExtra("image_urls", img_urls.toArray(img_urls_arr)));
				}
			}
			else if (intent.getAction().equals(GlobalResources.INTENT_GET_USERLIST)) {
				try {
					TreeMap<String, JSONObject> accounts = UserView.GetActiveAccounts(this);
					String[] accounts_arr = new String[]{};
					
					setResult(RESULT_OK, (new Intent()).putExtra("account_names", accounts.keySet().toArray(accounts_arr)));
				} catch (JSONException e) {
					setResult(RESULT_CANCELED);
				}
			}
			else if (intent.getAction().equals(GlobalResources.INTENT_SET_USER)) {
				boolean success = false;
				String username = null;
		        if (extras.containsKey("username")) {
		        	username = extras.getString("username");
		        }
		        
		        if (username != null) {
		        	success = AuthenticateActivity.SetActiveUser(getSharedPreferences("Auth", 0), username);
		        }
		        setResult(success ? RESULT_OK : RESULT_CANCELED);
			}
        }			
        finish();
	}
}
