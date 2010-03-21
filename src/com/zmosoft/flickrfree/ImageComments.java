package com.zmosoft.flickrfree;

import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageComments extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagecomments);
        m_extras = getIntent().getExtras();
        m_comment_list = GetCommentList();
        FillTable();
    }

    private JSONObject GetCommentList() {
    	String[] paramNames = {"photo_id"};
        String[] paramVals = {m_extras.getString("photo_id")};
        return RestClient.CallFunction("flickr.photos.comments.getList",paramNames,paramVals);
    }
    
    private void FillTable() {
    	try {
			LinkedHashMap<String,String> comments = new LinkedHashMap<String,String>();
			
			if (m_comment_list.has("comments") && m_comment_list.getJSONObject("comments").has("comment")) {
				JSONArray comment_arr = m_comment_list.getJSONObject("comments").getJSONArray("comment");
				JSONObject comment_obj;
				for (int i = 0; i < comment_arr.length(); i++) {
					comment_obj = null;
					comment_obj = comment_arr.getJSONObject(i);
					if (comment_obj != null
						&& comment_obj.has("authorname")
						&& comment_obj.has("_content")) {
						comments.put(comment_obj.getString("authorname"), comment_obj.getString("_content"));
					}
				}
			}

			View entry;
			for (String key : comments.keySet()) {
				// Add the title/value entry pair for the set of comments.
				entry = View.inflate(this, R.layout.image_comment_entry, null);
				((TextView)entry.findViewById(R.id.Author)).setText(key);
				((TextView)entry.findViewById(R.id.Comment)).setText(comments.get(key));
				
				//((TextView)entry.findViewById(R.id.InfoValue)).setClickable(true);
				//((TextView)entry.findViewById(R.id.InfoValue)).setOnClickListener(this);

				((LinearLayout)findViewById(R.id.ImgCommentsLayout)).addView(entry);
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void onClick(View v) {
//		if (v instanceof TextView) {
//			try {
//				String username = m_imginfo.getJSONObject("photo").getJSONObject("owner").getString("username");
//				String nsid = GlobalResources.getNSIDFromName(username);
//	
//				Intent i = new Intent(this, UserView.class);
//				i.putExtra("nsid", nsid);
//				try {
//					startActivity(i);
//				} catch (ActivityNotFoundException e) {
//					e.printStackTrace();
//				}
//			} catch (JSONException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//		}
//		else if (v.getId() == R.id.ImgInfoMapButton) {
//			// TODO Add code to load Google Maps and move to given location.
//		}
	}

	Bundle m_extras;
	JSONObject m_comment_list;
}
