package com.zmosoft.flickrfree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ImageContext extends Activity implements OnItemClickListener{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.imagecontext);
        m_extras = getIntent().getExtras();
		m_isprivate = false;
    	if (m_extras.containsKey("isprivate")) {
    		m_isprivate = m_extras.getBoolean("isprivate");
    	}
    	((TextView)findViewById(R.id.TextSetsLabel)).setVisibility(View.GONE);
    	((TextView)findViewById(R.id.TextPoolsLabel)).setVisibility(View.GONE);
    	((ListView)findViewById(R.id.ContextSetsListView)).setVisibility(View.GONE);
    	((ListView)findViewById(R.id.ContextPoolsListView)).setVisibility(View.GONE);

    	try {
    		if (m_extras.containsKey("contexts")) {
    			JSONObject contexts = new JSONObject(m_extras.getString("contexts"));
		    	FillSetsMap(contexts);
		        FillPoolsMap(contexts);
    		}
    		else {
    			finish();
    		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
        DisplayContexts();
    }

	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		String title = "";
		Intent i = new Intent(this,ImageGrid.class);
		if (parent.getAdapter() == ((ListView)findViewById(R.id.ContextSetsListView)).getAdapter()) {
			title = ((TextView)view.findViewById(R.id.SetTitle)).getText().toString();
			i.putExtra("photoset_id", m_set_ids.get(title));
			i.putExtra("type", "set");
			i.putExtra("isprivate", m_isprivate);
		}
		else if (parent.getAdapter() == ((ListView)findViewById(R.id.ContextPoolsListView)).getAdapter()) {
			title = ((TextView)view.findViewById(R.id.PoolTitle)).getText().toString();
			i.putExtra("group_id", m_pool_ids.get(title));
			i.putExtra("type", "pool");
		}
		i.putExtra("title", title);
		
		try {
			startActivity(i);
		} catch (ActivityNotFoundException e) {
			e.printStackTrace();
		}
	}

    private void DisplayContexts() {
		ListView lv;
		
		if (m_set_ids.size() > 0) {
			List < Map<String,String> > setList = new ArrayList < Map<String,String> >();
	    	lv = (ListView)findViewById(R.id.ContextSetsListView);
	    	((TextView)findViewById(R.id.TextSetsLabel)).setVisibility(View.VISIBLE);
	    	((ListView)findViewById(R.id.ContextSetsListView)).setVisibility(View.VISIBLE);
			for (String key : m_set_ids.keySet()) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("setname", key);
				m.put("nphotos", (m_set_sizes.containsKey(key) && !m_set_sizes.get(key).equals("")
									? m_set_sizes.get(key) + " Photos"
									: ""));
				setList.add(m);
			}
			
	        lv.setAdapter(new SimpleAdapter(
							this,
							setList,
							R.layout.sets_list_item,
							new String[]{"setname","nphotos"},
							new int[]{R.id.SetTitle, R.id.SetNPhotos}));
	        lv.setOnItemClickListener(this);
		}

		if (m_pool_ids.size() > 0) {
			List < Map<String,String> > poolList = new ArrayList < Map<String,String> >();
	    	lv = (ListView)findViewById(R.id.ContextPoolsListView);
	    	((TextView)findViewById(R.id.TextPoolsLabel)).setVisibility(View.VISIBLE);
	    	((ListView)findViewById(R.id.ContextPoolsListView)).setVisibility(View.VISIBLE);
			for (String key : m_pool_ids.keySet()) {
				Map<String, String> m = new HashMap<String, String>();
				m.put("poolname", key);
				m.put("nphotos", (m_pool_sizes.containsKey(key) && !m_pool_sizes.get(key).equals("")
									? m_pool_sizes.get(key) + " Photos"
									: ""));
				poolList.add(m);
			}
			
	        lv.setAdapter(new SimpleAdapter(
							this,
							poolList,
							R.layout.pools_list_item,
							new String[]{"poolname","nphotos"},
							new int[]{R.id.PoolTitle, R.id.PoolNPhotos}));
	        lv.setOnItemClickListener(this);
		}
    }
    
    private void FillSetsMap(JSONObject imgcontexts) throws JSONException {
    	m_set_ids = new TreeMap<String,String>();
    	m_set_sizes = new TreeMap<String,String>();
    	if (imgcontexts.has("set") && !imgcontexts.getString("set").equals("")) {
			JSONArray sets = imgcontexts.getJSONArray("set");
			String photoset_id, nphotos;
			for (int i = 0; i < sets.length(); i++) {
				photoset_id = sets.getJSONObject(i).getString("id");
				nphotos = GetPhotosetSize(photoset_id);
				m_set_ids.put(sets.getJSONObject(i).getString("title"),
						   sets.getJSONObject(i).getString("id"));
				m_set_sizes.put(sets.getJSONObject(i).getString("title"),nphotos);
			}
    	}
    }
    
    private void FillPoolsMap(JSONObject imgcontexts) throws JSONException {
    	m_pool_ids = new TreeMap<String,String>();
    	m_pool_sizes = new TreeMap<String,String>();
    	if (imgcontexts.has("pool") && !imgcontexts.getString("pool").equals("")) {
			JSONArray pools = imgcontexts.getJSONArray("pool");
			for (int i = 0; i < pools.length(); i++) {
				m_pool_ids.put(pools.getJSONObject(i).getString("title"),
							pools.getJSONObject(i).getString("id"));
				m_pool_sizes.put(pools.getJSONObject(i).getString("title"),"");
			}
    	}
    }

    private String GetPhotosetSize(String id) throws JSONException {
    	JSONObject json_obj = APICalls.photosetsGetInfo(id);
    	return (json_obj.has("photoset") && json_obj.getJSONObject("photoset").has("photos"))
    			? json_obj.getJSONObject("photoset").getString("photos")
    			: "";
    }
    
    Bundle m_extras;
    TreeMap<String,String> m_set_ids;
    TreeMap<String,String> m_set_sizes;
    TreeMap<String,String> m_pool_ids;
    TreeMap<String,String> m_pool_sizes;
    boolean m_isprivate;
}
