package com.zmosoft.flickrfree;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class UploadOptions extends Activity implements OnClickListener, OnCheckedChangeListener {
	
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
		
		((CheckBox)findViewById(R.id.chkEveryone)).setOnCheckedChangeListener(this);
		
		Spinner spnSafety = ((Spinner)findViewById(R.id.spnSafetyLevel));
        ArrayAdapter adapter = ArrayAdapter.createFromResource(
                this, R.array.safety_levels_list, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spnSafety.setAdapter(adapter);
	}
	
	private void InitiateUpload() {
		String[] safety_levels = getResources().getStringArray(R.array.safety_levels_list);

		// uploader_intent will contain all of the necessary information about this
		// upload in the Extras Bundle.
		Intent uploader_intent = new Intent(this, Uploader.class);
		uploader_intent.putExtra("filename", m_extras.getString("filepath"));
		uploader_intent.putExtra("title", ((EditText)findViewById(R.id.txtPhotoTitle)).getText().toString());
		uploader_intent.putExtra("comment", ((EditText)findViewById(R.id.txtPhotoComment)).getText().toString());
		uploader_intent.putExtra("tags", ((EditText)findViewById(R.id.txtPhotoTags)).getText().toString());
		uploader_intent.putExtra("is_public", ((CheckBox)findViewById(R.id.chkEveryone)).isChecked());
		CheckBox cb = ((CheckBox)findViewById(R.id.chkFriends));
		uploader_intent.putExtra("is_friend", cb.isEnabled() && cb.isChecked());
		cb = ((CheckBox)findViewById(R.id.chkFamily));
		uploader_intent.putExtra("is_family", cb.isEnabled() && cb.isChecked());
		String sl = ((TextView)((Spinner)findViewById(R.id.spnSafetyLevel)).getSelectedView()).getText().toString();
		uploader_intent.putExtra("safety_level", 1);
		for (int i = 0; i < safety_levels.length; ++i) {
			if (safety_levels[i].equals(sl)) {
				uploader_intent.putExtra("safety_level", i + 1);
				break;
			}
		}
		
		// Start the uploader service and pass in the intent containing
		// the upload information.
		startService(uploader_intent);
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

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		if (buttonView.getId() == R.id.chkEveryone) {
			((CheckBox)findViewById(R.id.chkFamily)).setEnabled(!isChecked);
			((CheckBox)findViewById(R.id.chkFriends)).setEnabled(!isChecked);
		}
	}

	Bundle m_extras;
}
