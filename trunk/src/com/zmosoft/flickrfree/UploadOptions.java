package com.zmosoft.flickrfree;

import android.app.Activity;
import android.content.Intent;
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
		Intent uploader = new Intent(this, Uploader.class);
		String[] safety_levels = getResources().getStringArray(R.array.safety_levels_list);

		uploader.putExtra("filename", m_extras.getString("filepath"));
		uploader.putExtra("title", ((EditText)findViewById(R.id.txtPhotoTitle)).getText().toString());
		uploader.putExtra("comment", ((EditText)findViewById(R.id.txtPhotoComment)).getText().toString());
		uploader.putExtra("tags", ((EditText)findViewById(R.id.txtPhotoTags)).getText().toString());
		uploader.putExtra("is_public", ((CheckBox)findViewById(R.id.chkEveryone)).isChecked());
		uploader.putExtra("is_friend", ((CheckBox)findViewById(R.id.chkFriends)).isChecked());
		uploader.putExtra("is_family", ((CheckBox)findViewById(R.id.chkFamily)).isChecked());
		String sl = ((TextView)((Spinner)findViewById(R.id.spnSafetyLevel)).getSelectedView()).getText().toString();
		uploader.putExtra("safety_level", 1);
		for (int i = 0; i < safety_levels.length; ++i) {
			if (safety_levels[i].equals(sl)) {
				uploader.putExtra("safety_level", i + 1);
				break;
			}
		}

		startService(uploader);
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
