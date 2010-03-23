package com.zmosoft.flickrfree;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
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
        m_comment_list = APICalls.photosCommentsGetList(m_extras.getString("photo_id"));
        FillTable();
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

			CommentLayout entry;
			String comment;
			CharSequence formatted_comment;
			for (String key : comments.keySet()) {
				// Add the title/value entry pair for the set of comments.
				entry = (CommentLayout)View.inflate(this, R.layout.image_comment_entry, null);
				((TextView)entry.findViewById(R.id.Author)).setText(key);
				
				comment = comments.get(key);
				if (ReadLinksInComment(entry, comment)) {
					// Set the "Go To" button to visible and make it a link
					// to go to the given group page.
				}
				
				//comment = ConvertURLstoURIs(comment);
				// The comment might have HTML tags, so use the Html class to handle
				// that.
				formatted_comment = Html.fromHtml(comments.get(key),null,null);
				((TextView)entry.findViewById(R.id.Comment)).setText(formatted_comment);
				
				
				//((TextView)entry.findViewById(R.id.InfoValue)).setClickable(true);
				//((TextView)entry.findViewById(R.id.InfoValue)).setOnClickListener(this);

				((LinearLayout)findViewById(R.id.ImgCommentsLayout)).addView(entry);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }
    
    public boolean ReadLinksInComment(CommentLayout entry, String comment) {
    	int links_found = 0;
    	
    	Integer[] url_pos = findURLPosInComment(comment, 0);
    	
    	while (url_pos != null) {
    		++links_found;
			String url_str = comment.substring(url_pos[0], url_pos[1]);
			URL url;
    		try {
    			if (url_str.substring(0, 1).equals("/")) {
    				// If the URL starts with "/", then it is probably a relative URL.
    				// We need to prepend the rest of the Flickr URL onto it so it can
    				// be read correctly.
    				url_str = "http://www.flickr.com" + url_str;
    			}
    			else if (url_str.substring(0, 3).equals("www")) {
    				url_str = "http://" + url_str;
    			}
    			url = new URL(url_str);
			} catch (MalformedURLException e) {
		    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
				continue;
			}
			String path = url.getPath();
			if (url.getProtocol().equals("http")) {
				String id;
				if (path.contains("groups")) {
					id = path.substring(path.indexOf("groups/") + 7);
					if (id.contains("/")) {
						id = id.substring(0, id.indexOf("/"));
					}
					// TODO Put code in here for getting the group name from the id.
					// Or, alternatively, if the group URL only gives the name, then
					// get the id.
					String group_name = APICalls.getGroupNameFromID(id);
					if (group_name.equals("")) {
						group_name = "Unnamed Group";
					}
					entry.m_group_links.put(group_name, id);
				}
				else if (path.contains("photo")) {
					id = path.substring(path.indexOf("photos/") + 7);
					if (id.contains("/")) {
						id = id.substring(id.indexOf("/") + 1);
						id = id.substring(0,id.indexOf("/"));
					}
					// TODO Put code in here for getting the photo name from the id.
					String photo_name = APICalls.getPhotoNameFromID(id);
					if (photo_name.equals("")) {
						photo_name = "Unnamed Photo";
					}
					entry.m_photo_links.put(photo_name, id);
				}
			}
	    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
    	}
    	
    	return (links_found > 0);
    }
    
    public String ConvertURLstoURIs(String comment) {
    	Integer[] url_pos = findURLPosInComment(comment, 0);
    	
    	while (url_pos != null) {
			String url_str = comment.substring(url_pos[0], url_pos[1]);
			URL url;
    		try {
    			if (url_str.substring(0, 1).equals("/")) {
    				// If the URL starts with "/", then it is probably a relative URL.
    				// We need to prepend the rest of the Flickr URL onto it so it can
    				// be read correctly.
    				url_str = "http://www.flickr.com" + url_str;
    			}
    			url = new URL(url_str);
			} catch (MalformedURLException e) {
		    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
				continue;
			}
			String path = url.getPath();
			if (url.getProtocol().equals("http") && path.contains("groups")) {
				String group_id = path.substring(path.indexOf("groups/") + 7);
				if (group_id.contains("/")) {
					group_id = group_id.substring(0, group_id.indexOf("/"));
				}
				String uri = "flickr://flickrfree/?group_id=" + group_id;
				comment = comment.substring(0, url_pos[0]) + uri + comment.substring(url_pos[1]);
			}
	    	url_pos = findURLPosInComment(comment, url_pos[1] + 1);
    	}
    	
    	return comment;
    }
    
    Integer[] findURLPosInComment(String comment) {
    	return findURLPosInComment(comment, 0);
    }
    
    Integer[] findURLPosInComment(String comment, int start) {
    	int href_start = comment.indexOf("<a href=\"", start);
    	int href_end = comment.indexOf("\">", href_start);
    	Integer[] range = null;
    	
    	if (href_start >= 0) {
    		href_start += 9;
    		range = new Integer[]{href_start, href_end};
    	}
    	
    	return range;
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
