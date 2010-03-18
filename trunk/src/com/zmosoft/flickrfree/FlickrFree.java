package com.zmosoft.flickrfree;

import java.io.File;

import android.os.Bundle;
import android.os.Environment;

// This is the main class for the application. It is an extension of the UserView
// class, so when this is loaded the app will show the UserView screen.
public class FlickrFree extends UserView {
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the image download directory.
        GlobalResources.m_imgDownloadDir = Environment.getExternalStorageDirectory().toString() + "/download";
        
        // If an authorization file exists, load it.
        String auth_filename = Environment.getExternalStorageDirectory().getAbsolutePath() + "/AuthInfo.txt";
        File auth_file = new File(auth_filename);
        if (auth_file.exists()) {
        	AuthenticateActivity.ImportAuth(getSharedPreferences("Auth",0), auth_filename);
        }
//        else {
//            AuthenticateActivity.ExportAuth(getSharedPreferences("Auth",0), auth_filename);
//        }
    }
}
