package com.zmosoft.flickrfree;

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
        
        // TODO I have a serious problem here. It seems that if the app force closes
        // at any point, when it reloads, the API key information is wiped out. I
        // don't yet know how to fix this.
        
        // Get the api key and secret.
        GlobalResources.m_apikey = getResources().getString(R.string.apikey);
        GlobalResources.m_secret = getResources().getString(R.string.secret);
        GlobalResources.m_AUTHURL = getResources().getString(R.string.auth_url);
    }
}
