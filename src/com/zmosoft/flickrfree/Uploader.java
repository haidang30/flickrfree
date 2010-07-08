package com.zmosoft.flickrfree;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;

public class Uploader extends Service {

	// AsyncTask to upload a picture in the background.
	private class UploadPictureTask extends AsyncTask<Bundle, String, Object> {
		
		@Override
		protected Object doInBackground(Bundle... params) {
			m_upload_info = params.length > 0 ? params[0] : null;
			
			if (m_upload_info != null) {
				publishProgress(m_upload_info.getString("title"));
				
		        RestClient.UploadPicture(m_upload_info.getString("filename"),
						    			 m_upload_info.getString("title"),
							    		 m_upload_info.getString("comment"),
							    		 m_upload_info.getString("tags"),
							    		 m_upload_info.getBoolean("is_public"),
							    		 m_upload_info.getBoolean("is_friend"),
							    		 m_upload_info.getBoolean("is_family"),
							    		 m_upload_info.getInt("safety_level"));
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... progress) {
			String title = progress.length > 0 ? progress[0] : "";
			m_notification.setLatestEventInfo(getApplicationContext(),
					  getApplicationContext().getString(R.string.app_name),
					  getApplicationContext().getString(R.string.uploadingpicture) + " \"" + title + "\"",
					  m_notify_activity);
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.UPLOADER_ID, m_notification);
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
		
		public Bundle getUploadInfo() {
			return m_upload_info;
		}
		
		Bundle m_upload_info = null;
	}
	
	public class UploadBinder extends Binder {
        Uploader getService() {
            return Uploader.this;
        }
    }

	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent, int startId) {
		
		int icon = android.R.drawable.stat_sys_upload;
		CharSequence tickerText = "Uploading Picture";
		m_notification = new Notification(icon, tickerText, System.currentTimeMillis());
		m_notify_activity = PendingIntent.getActivity(this, 0, new Intent(this, Uploader.class), 0);
		
		if (m_notification != null) {
			m_notification.setLatestEventInfo(getApplicationContext(),
											  this.getString(R.string.app_name),
											  this.getString(R.string.uploadingpicture),
											  m_notify_activity);
			m_notification.flags = Notification.FLAG_NO_CLEAR;
		}
		
		m_upload_task = (UploadPictureTask)new UploadPictureTask().execute(intent.getExtras());
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return m_binder;
	}

	public String getFilename() {
		String rval = "";
		if (m_upload_task != null) {
			Bundle upload_info = m_upload_task.getUploadInfo();
			if (upload_info != null) {
				rval = upload_info.getString("filename");
			}
		}
		
		return rval;
	}
	
	private Notification m_notification = null;
	private PendingIntent m_notify_activity = null;
	private final IBinder m_binder = new UploadBinder();
	private UploadPictureTask m_upload_task = null;
}
