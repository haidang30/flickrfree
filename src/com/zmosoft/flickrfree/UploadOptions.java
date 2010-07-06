package com.zmosoft.flickrfree;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class UploadOptions extends Activity implements OnClickListener {
	
	// AsyncTask to upload a picture in the background.
	private class UploadPicture extends AsyncTask<Bundle, Void, Object> {
		
		@Override
		protected Object doInBackground(Bundle... params) {
			Bundle upload_info = params.length > 0 ? params[0] : null;
			
			if (upload_info != null) {
		        RestClient.UploadPicture(upload_info.getString("filename"),
						    			 upload_info.getString("title"),
							    		 upload_info.getString("comment"),
							    		 upload_info.getString("tags"),
							    		 upload_info.getBoolean("is_public"),
							    		 upload_info.getBoolean("is_friend"),
							    		 upload_info.getBoolean("is_family"),
							    		 upload_info.getInt("safety_level"));
			}
			
			return null;
		}
		
		@Override
		protected void onPreExecute() {
			// TODO: Set progress indicator to display in Android notification bar while uploading picture.
	    	setProgressBarIndeterminateVisibility(true);
		}
		
		@Override
		protected void onPostExecute(Object result) {
	    	setProgressBarIndeterminateVisibility(false);
		}
	}
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: Add thumbnail of image to upload options screen.
		setContentView(R.layout.upload_options);
        m_extras = getIntent().getExtras();
		
		((Button)findViewById(R.id.btnUpload)).setOnClickListener(this);
		((Button)findViewById(R.id.btnCancel)).setOnClickListener(this);
		
		String filepath = m_extras.getString("filepath");
		String title = filepath.substring(filepath.lastIndexOf("/") + 1,
											 filepath.lastIndexOf("."));
		((EditText)findViewById(R.id.txtPhotoTitle)).setText(title);
		((EditText)findViewById(R.id.txtPhotoTitle)).selectAll();
		
		Spinner spnSafety = ((Spinner)findViewById(R.id.spnSafetyLevel));
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.safety_levels_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSafety.setAdapter(adapter);
	}
	
	private void InitiateUpload() {		
		Bundle picture_info = new Bundle();
		String[] safety_levels = getResources().getStringArray(R.array.safety_levels_list);

		picture_info.putString("filename", m_extras.getString("filepath"));
		picture_info.putString("title", ((EditText)findViewById(R.id.txtPhotoTitle)).getText().toString());
		picture_info.putString("comment", ((EditText)findViewById(R.id.txtPhotoComment)).getText().toString());
		picture_info.putString("tags", ((EditText)findViewById(R.id.txtPhotoTags)).getText().toString());
		picture_info.putBoolean("is_public", ((CheckBox)findViewById(R.id.chkEveryone)).isChecked());
		picture_info.putBoolean("is_friend", ((CheckBox)findViewById(R.id.chkFriends)).isChecked());
		picture_info.putBoolean("is_family", ((CheckBox)findViewById(R.id.chkFamily)).isChecked());
		String sl = ((TextView)((Spinner)findViewById(R.id.spnSafetyLevel)).getSelectedView()).getText().toString();
		picture_info.putInt("safety_level", 1);
		for (int i = 0; i < safety_levels.length; ++i) {
			if (safety_levels[i].equals(sl)) {
				picture_info.putInt("safety_level", i + 1);
				break;
			}
		}

		new UploadPicture().execute(picture_info);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.btnUpload) {
			InitiateUpload();
			finish();
		}
		else if (v.getId() == R.id.btnCancel) {
			finish();
		}
	}

	Bundle m_extras;
}
