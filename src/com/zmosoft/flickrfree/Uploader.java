package com.zmosoft.flickrfree;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;

public class Uploader extends Service {

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
			if (m_notification != null) {
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.UPLOADER_ID, m_notification);
			}
		}
		
		@Override
		protected void onPostExecute(Object result) {
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
			stopSelf();
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		int icon = android.R.drawable.stat_sys_upload;
		CharSequence tickerText = "FlickrFree Uploading Picture";
		long when = System.currentTimeMillis();

		m_notification = new Notification(icon, tickerText, when);
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "FlickrFree";
		CharSequence contentText = "Uploading Picture";
		Intent notificationIntent = new Intent(this, Uploader.class);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
		if (m_notification != null) {
			m_notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
		}
		
		new UploadPicture().execute(intent.getExtras());
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	Notification m_notification = null;
}
