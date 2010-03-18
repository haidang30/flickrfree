package com.zmosoft.flickrfree;

import java.util.HashMap;
import java.util.LinkedHashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ImageInfo extends Activity implements OnClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imageinfo);
        m_extras = getIntent().getExtras();
        try {
			m_imginfo = m_extras.containsKey("imginfo") ? new JSONObject(m_extras.getString("imginfo"))
						: new JSONObject("");
			m_exif = m_extras.containsKey("exif") ? new JSONObject(m_extras.getString("exif"))
						: new JSONObject("");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        FillTable();
    }

    private void FillTable() {
    	try {
			LinkedHashMap<String,String> info = new LinkedHashMap<String,String>();
			HashMap<String,String> exif = new LinkedHashMap<String,String>();
			
			if (m_exif.has("photo") && m_exif.getJSONObject("photo").has("exif")) {
				JSONArray exif_arr = m_exif.getJSONObject("photo").getJSONArray("exif");
				JSONObject exif_entry;
				for (int i = 0; i < exif_arr.length(); i++) {
					exif_entry = null;
					exif_entry = exif_arr.getJSONObject(i);
					if (exif_entry != null && exif_entry.has("label") && exif_entry.has("raw") && exif_entry.getJSONObject("raw").has("_content")) {
						exif.put(exif_entry.getString("label"), exif_entry.getJSONObject("raw").getString("_content"));
					}
				}
			}

			if (m_imginfo.has("photo")) {
				JSONObject photo = m_imginfo.getJSONObject("photo");
				if (photo.has("owner")) {
					JSONObject owner = photo.getJSONObject("owner");
					String owner_str = "";
					if (owner.has("realname") && !owner.getString("realname").equals("")) {
						owner_str += owner.getString("realname");
						if (owner.has("username") && !owner.getString("username").equals("")) {
							owner_str += " (" + owner.getString("username") + ")";
						}
					}
					else if (owner.has("username") && !owner.getString("username").equals("")) {
						owner_str = owner.getString("username");
					}
					if (!owner_str.equals("")) {
						info.put(getResources().getString(R.string.imageinfo_owner),owner_str);
					}

					if (owner.has("location") && !owner.getString("location").equals("")) {
						info.put(getResources().getString(R.string.imageinfo_owner_location),owner.getString("location"));
					}
				}

				if (photo.has("dates")) {
					JSONObject dates = photo.getJSONObject("dates");
					if (dates.has("taken") && !dates.getString("taken").equals("")) {
						info.put(getResources().getString(R.string.imageinfo_datetaken),dates.getString("taken"));
					}
				}

				if (exif.containsKey("GPS Latitude") && exif.containsKey("GPS Longitude")) {
					String latitude = String.valueOf(GlobalResources.LatLongToDecimal(exif.get("GPS Latitude")));
					String longitude = String.valueOf(GlobalResources.LatLongToDecimal(exif.get("GPS Longitude")));
					if (exif.containsKey("GPSLatitudeRef") && exif.containsKey("GPSLongitudeRef")) {
						latitude += exif.get("GPSLatitudeRef").substring(0, 1); 
						longitude += exif.get("GPSLongitudeRef").substring(0, 1); 
					}
					info.put(getResources().getString(R.string.imageinfo_locationtaken),
							 latitude + ", " + longitude);
				}
				else if (photo.has("location")) {
					JSONObject location = photo.getJSONObject("location");
					if (location.has("latitude") && location.has("longitude")
						&& !location.getString("latitude").equals("")
						&& !location.getString("longitude").equals("")) {
						info.put(getResources().getString(R.string.imageinfo_locationtaken),
								location.getString("latitude") + ", " + location.getString("longitude"));
					}
				}
				
				String camera = (exif.containsKey("Make") ? exif.get("Make") : "")
							+	(exif.containsKey("Model") ? " " + exif.get("Model") : "");
				if (!camera.equals("")) {
					info.put(getResources().getString(R.string.imageinfo_camera), camera);
				}
				
				if (photo.has("description")) {
					JSONObject t = photo.getJSONObject("description");
					if (t.has("_content") && !t.getString("_content").equals("")) {
						info.put(getResources().getString(R.string.imageinfo_description), t.getString("_content"));
					}
				}
	    	}
	
			View entry;
			for (String key : info.keySet()) {
				// Add the title/value entry pair for this set of information.
				entry = View.inflate(this, R.layout.image_info_entry, null);
				((TextView)entry.findViewById(R.id.InfoTitle)).setText(key);
				((TextView)entry.findViewById(R.id.InfoValue)).setText(info.get(key));
				
				if (key.equals(getResources().getString(R.string.imageinfo_owner))) {
					((TextView)entry.findViewById(R.id.InfoValue)).setClickable(true);
					((TextView)entry.findViewById(R.id.InfoValue)).setOnClickListener(this);
				}

				((LinearLayout)findViewById(R.id.ImgInfoLayout)).addView(entry);

				if (key.equals(getResources().getString(R.string.imageinfo_locationtaken))) {
					// If this value is a geo location, add a button to take the user to that
					// location in Google Maps.
					((LinearLayout)findViewById(R.id.ImgInfoLayout)).addView(View.inflate(this, R.layout.image_info_map_button, null));
					Button b = ((Button)findViewById(R.id.ImgInfoMapButton));
					b.setOnClickListener(this);
					b.setEnabled(false);
				}
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
	@Override
	public void onClick(View v) {
		if (v instanceof TextView) {
			try {
				String username = m_imginfo.getJSONObject("photo").getJSONObject("owner").getString("username");
				String nsid = GlobalResources.getNSIDFromName(username);
	
				Intent i = new Intent(this, UserView.class);
				i.putExtra("nsid", nsid);
				try {
					startActivity(i);
				} catch (ActivityNotFoundException e) {
					e.printStackTrace();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (v.getId() == R.id.ImgInfoMapButton) {
			// TODO Add code to load Google Maps and move to given location.
		}
	}

	Bundle m_extras;
    JSONObject m_imginfo;
    JSONObject m_exif;
}
