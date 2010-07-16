package com.zmosoft.flickrfree;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import java.util.ArrayList;

public class ExternalHooks extends Activity {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
		if (intent.getAction().equals(GlobalResources.INTENT_GET_PHOTOSTREAM)
			|| intent.getAction().equals(GlobalResources.INTENT_GET_POOL)
			|| intent.getAction().equals(GlobalResources.INTENT_FLICKR_SEARCH)) {
			
			JSONObject json_obj = null;
			int page = extras.containsKey("page") ? extras.getInt("page") : 0;
			int per_page = extras.containsKey("per_page") ? extras.getInt("per_page") : 0;
			
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
				
				if (user_id != null) {
					json_obj = APICalls.peopleGetPhotos(user_id, page, per_page);
				}
			}
			else if (intent.getAction().equals(GlobalResources.INTENT_GET_POOL)) {
				String group_id = null;
				if (extras.containsKey("group_id") && extras.getString("group_id") != null) {
					group_id = extras.getString("group_id");
				}
				else if (extras.containsKey("group_url") && extras.getString("group_url") != null) {
					String[] group_info = APICalls.getGroupInfoFromURL(extras.getString("group_url"));
					group_id = group_info[1];
				}
				if (group_id != null) {
					json_obj = APICalls.groupsPoolsGetPhotos(group_id, page, per_page);
				}
			}
			else if (intent.getAction().equals(GlobalResources.INTENT_GET_FAVORITES)) {
				json_obj = APICalls.favoritesGetList(page, per_page);
			}
			else if (intent.getAction().equals(GlobalResources.INTENT_FLICKR_SEARCH)) {
				String text = extras.containsKey("text") ? extras.getString("text") : null;
				String tags = extras.containsKey("tags") ? extras.getString("tags") : null;
				String userid = extras.containsKey("user_id") ? extras.getString("user_id") : null;

				if (text != null || tags != null || userid != null) {
					json_obj = APICalls.photosSearch(userid, text, tags, page, per_page);
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
										                             GlobalResources.ImgSize.MED, "jpg"));
						} catch (JSONException e) {
						}
				}
				setResult(RESULT_OK, (new Intent()).putExtra("image_urls", img_urls.toArray(img_urls_arr)));
			}
		}
		
		finish();
	}
}
