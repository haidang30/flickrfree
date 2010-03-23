package com.zmosoft.flickrfree;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.TreeMap;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ImageView;

import com.zmosoft.flickrfree.GlobalResources.ImgSize;

public class ImageFullScreen extends Activity {

	private class GetImageInfoTask extends AsyncTask<Object, Object, Object> {
		
		@Override
		protected Object doInBackground(Object... params) {
	    	m_imginfo = APICalls.photosGetInfo(m_extras.getString("photo_id"));
	    	m_exif =  APICalls.photosGetExif(m_extras.getString("photo_id"));
    		m_imgcontexts = APICalls.photosGetAllContexts(m_extras.getString("photo_id"));
	    	try {
	    		m_tags = GetTags();
				m_imgsizes = GetImgSizes();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Object result) {
			try {
				ShowImage();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	    private String GetTags() throws JSONException {
			String tags_str = "";
			if (m_imginfo.has("photo")) {
				JSONObject photo = m_imginfo.getJSONObject("photo");
				if (photo.has("tags")) {
					JSONObject t = photo.getJSONObject("tags");
					if (t.has("tag") && !t.getString("tag").equals("")) {
						JSONArray tags = t.getJSONArray("tag");
						for (int i = 0; i < tags.length(); i++) {
							if (tags.getJSONObject(i).has("raw") && !tags.getJSONObject(i).getString("_content").equals("")) {
								tags_str = tags_str + tags.getJSONObject(i).getString("_content");
								if (i < tags.length() - 1) {
									tags_str = tags_str + " ";
								}
							}
						}
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
        requestWindowFeature(Window.FEATURE_PROGRESS);
        requestWindowFeature(Window.FEATURE_CONTEXT_MENU);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.imgfullscreen);
        
    	m_extras = getIntent().getExtras();
    	setProgressBarIndeterminateVisibility(true);
    	
    	if (savedInstanceState == null) {
			m_downloadSize = ImgSize.MED;
    		m_isprivate = m_extras.containsKey("isprivate") ? m_extras.getBoolean("isprivate") : false;
    		new GetImageInfoTask().execute();
        }
        else {
    		m_isprivate = savedInstanceState.getBoolean("isprivate");
    		m_tags = savedInstanceState.getString("tags");
    		int dlsize = savedInstanceState.getInt("downloadsize");
    		if (dlsize == 0) {
    			m_downloadSize = ImgSize.SMALLSQUARE;
    		}
    		else if (dlsize == 1) {
    			m_downloadSize = ImgSize.THUMB;
    		}
    		else if (dlsize == 2) {
    			m_downloadSize = ImgSize.SMALL;
    		}
    		else if (dlsize == 3) {
    			m_downloadSize = ImgSize.MED;
    		}
    		else if (dlsize == 4) {
    			m_downloadSize = ImgSize.LARGE;
    		}
    		else if (dlsize == 5) {
    			m_downloadSize = ImgSize.ORIG;
    		}
	    	try {
		    	m_imginfo = new JSONObject(savedInstanceState.getString("imginfo"));
		    	m_exif = new JSONObject(savedInstanceState.getString("exif"));
		    	m_imgcontexts = new JSONObject(savedInstanceState.getString("imgcontexts"));
				m_imgsizes = GetImgSizes();
				ShowImage();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
    	
		registerForContextMenu(findViewById(R.id.imgview));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    	super.onSaveInstanceState(outState);
    	outState.putBoolean("isprivate", m_isprivate);
    	outState.putInt("downloadsize", m_downloadSize.getNum());
    	outState.putString("imginfo", m_imginfo != null ? m_imginfo.toString() : "");
    	outState.putString("exif", m_exif != null ? m_exif.toString() : "");
    	outState.putString("imgcontexts", m_imgcontexts != null ? m_imgcontexts.toString() : "");
    	outState.putString("tags", m_tags);
    }
    
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		try {
			if (m_imginfo.has("photo")) {
				JSONObject photo_obj = m_imginfo.getJSONObject("photo");
				if (photo_obj.has("isfavorite")) {
					menu.add(0, MENU_SETFAVE, 0,  R.string.mnu_favorite).setCheckable(true)
						.setChecked(photo_obj.getInt("isfavorite") == 1);
				}
			}
			menu.add(Menu.NONE, MENU_DOWNLOAD, Menu.NONE, R.string.mnu_imgdownload);
	    	menu.add(Menu.NONE, MENU_IMGINFO, Menu.NONE, R.string.mnu_imginfo);
	    	menu.add(Menu.NONE, MENU_IMGCOMMENTS, Menu.NONE, R.string.mnu_imgcomments);
	    	if (!m_tags.equals("")) {
	    		menu.add(Menu.NONE, MENU_IMGTAGS, Menu.NONE, R.string.mnu_imgtags);
	    	}
    		if (m_imgcontexts.has("set") || m_imgcontexts.has("pool")) {
    			menu.add(Menu.NONE, MENU_IMGCONTEXT, Menu.NONE, R.string.mnu_imgcontext);
    		}
		} catch (JSONException e) {
			e.printStackTrace();
		}
    }

    public boolean onContextItemSelected(MenuItem item) {
    	Intent i;
    	try {
			switch (item.getItemId()) {
			case MENU_DOWNLOAD:
				showDialog(DIALOG_DOWNLOAD_IMG_SIZE);
				return true;
			case MENU_SETFAVE:
				if (m_extras.containsKey("photo_id")) {
					if (item.isChecked()) {
						APICalls.favoritesRemove(m_extras.getString("photo_id"));
					}
					else {
						APICalls.favoritesAdd(m_extras.getString("photo_id"));
					}
					m_imginfo = APICalls.photosGetInfo(m_extras.getString("photo_id"));
				}
				return true;
			case MENU_IMGINFO:
				i = new Intent(this, ImageInfo.class);
				i.putExtra("photo_id",m_extras.getString("photo_id"));
				i.putExtra("isprivate", m_isprivate);
				i.putExtra("imginfo", m_imginfo.toString());
				i.putExtra("exif", m_exif.toString());
				startActivity(i);
				return true;
			case MENU_IMGCOMMENTS:
				i = new Intent(this, ImageComments.class);
				i.putExtra("photo_id",m_extras.getString("photo_id"));
				i.putExtra("imginfo", m_imginfo.toString());
				startActivity(i);
				return true;
			case MENU_IMGCONTEXT:
				i = new Intent(this, ImageContext.class);
				i.putExtra("photo_id",m_extras.getString("photo_id"));
				i.putExtra("isprivate", m_isprivate);
				i.putExtra("contexts", m_imgcontexts.toString());
				startActivity(i);
				return true;
			case MENU_IMGTAGS:
				i = new Intent(this, ImageTags.class);
				try {
					i.putExtra("tags", m_tags);
					if (m_imginfo.has("photo") && m_imginfo.getJSONObject("photo").has("owner")
						&& m_imginfo.getJSONObject("photo").getJSONObject("owner").has("nsid")) {
						i.putExtra("nsid", m_imginfo.getJSONObject("photo").getJSONObject("owner").getString("nsid"));
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				startActivity(i);
			default:
				return super.onContextItemSelected(item);
			}
		}
		catch (ActivityNotFoundException e) {
				e.printStackTrace();
		}
		return false;
    }
    
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	
    	switch(id) {
    	case DIALOG_DOWNLOAD_IMG_SIZE:
			CharSequence[] size_names = GetImageSizeNames();
			
    		AlertDialog.Builder dbuilder = new AlertDialog.Builder(this)
    			.setTitle(R.string.dlg_imgsize_title)
    			.setSingleChoiceItems(size_names, ImgSize.MED.getNum(),
    				new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int choice) {
							ImgSize size_chosen = ImgSize.MED;
							if (choice == 0) {
								size_chosen = ImgSize.SMALL;
							}
							else if (choice == 1) {
								size_chosen = ImgSize.MED;
							}
							else if (choice == 2) {
								size_chosen = ImgSize.ORIG;
							}
							m_downloadSize = size_chosen;
						}
    				})
		        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
						ImgSize size_chosen = m_downloadSize;

						try {
							String url = m_imgsizes.get(size_chosen);
							GlobalResources.downloadImage(url, "", GlobalResources.m_imgDownloadDir, null, false);
							dialog.dismiss();
						} catch (MalformedURLException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
		        })
		        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
		        });
			dialog = dbuilder.create();
			break;
    	}
    	return dialog;
    }
    
    private TreeMap<ImgSize, String> GetImgSizes() throws JSONException {
    	TreeMap<ImgSize, String> imgsizesmap = new TreeMap<ImgSize, String>();
    	if (m_extras.containsKey("photo_id")) {
        	// Get the list of available image sizes for this photo.
	        
			imgsizesmap = new TreeMap<ImgSize, String>();
			JSONObject imgsizes_obj = APICalls.photosGetSizes(m_extras.getString("photo_id"));
			if (imgsizes_obj.has("sizes")) {
				JSONArray imgsizes = imgsizes_obj.getJSONObject("sizes").getJSONArray("size");
				// Iterate through the Image Sizes array and fill the imgsizesmap hash map.
				for (int i = 0; i < imgsizes.length(); i++) {
					JSONObject imgsize = imgsizes.getJSONObject(i);
					if (imgsize.getString("label").equals("Square")) {
						imgsizesmap.put(ImgSize.SMALLSQUARE, imgsize.getString("source"));
					}
					else if (imgsize.getString("label").equals("Thumbnail")) {
						imgsizesmap.put(ImgSize.THUMB, imgsize.getString("source"));
					}
					else if (imgsize.getString("label").equals("Small")) {
						imgsizesmap.put(ImgSize.SMALL, imgsize.getString("source"));
					}
					else if (imgsize.getString("label").equals("Medium")) {
						imgsizesmap.put(ImgSize.MED, imgsize.getString("source"));
					}
					else if (imgsize.getString("label").equals("Original")) {
						imgsizesmap.put(ImgSize.ORIG, imgsize.getString("source"));
					}
				}
			}
    	}
    	return imgsizesmap;
    }
    
    private void ShowImage() throws JSONException, IOException {
    	String img_url = "";
    	if (m_imgsizes.containsKey(ImgSize.MED)) {
    		img_url = m_imgsizes.get(ImgSize.MED);
    	}
    	else if (m_imgsizes.containsKey(ImgSize.SMALL)) {
    		img_url = m_imgsizes.get(ImgSize.SMALL);
    	}
    	else {
    		//TODO: Display an error image to indicate that the image URL cannot be found
    	}
    	
    	if (img_url != "") {
    		setProgressBarVisibility(true);
    		setProgressBarIndeterminateVisibility(true);
    		setProgress(Window.PROGRESS_START);
    		new GetCachedImageTask().execute(this, (ImageView)findViewById(R.id.imgview), img_url, true);
    	}
    	
		if (m_imginfo.has("photo") && m_imginfo.getJSONObject("photo").has("title")) {
    		setTitle("\t" + m_imginfo.getJSONObject("photo").getJSONObject("title").getString("_content"));
    	}
    }

    private CharSequence[] GetImageSizeNames() {
    	CharSequence[] size_names_array = {};
		Vector<CharSequence> size_names = new Vector<CharSequence>();
		for (ImgSize key : m_imgsizes.keySet()) {
			if (key != ImgSize.SMALLSQUARE && key != ImgSize.THUMB) {
				size_names.add(key.toString());
			}
		}
		size_names_array = size_names.toArray(size_names_array);
		return size_names_array;
    }
    
	Bundle m_extras;
	String m_tags;
	TreeMap<ImgSize, String> m_imgsizes;
	ImgSize m_downloadSize;
	JSONObject m_imginfo;
	JSONObject m_exif;
	JSONObject m_imgcontexts;
	JSONObject m_comment_list;
	boolean m_isprivate;
	
	static final int MENU_IMGINFO = 0;
	static final int MENU_IMGCOMMENTS = 1;
	static final int MENU_IMGCONTEXT = 2;
	static final int MENU_IMGTAGS = 3;
	static final int MENU_DOWNLOAD = 4;
	static final int MENU_SETFAVE = 5;
	
	static final int DIALOG_DOWNLOAD_IMG_SIZE = 6;
}
