package com.zmosoft.flickrfree;

import java.util.LinkedList;

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
	private class UploadPictureTask extends AsyncTask<Void, String, Object> {
		
		@Override
		protected Object doInBackground(Void... params) {
			Bundle upload_info = null;
			Intent broadcast_intent = new Intent();
			while (m_uploads.size() > 0) {
				upload_info = m_uploads.remove(0);
				if (upload_info != null) {
					broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_STARTED);
					getApplicationContext().sendBroadcast(broadcast_intent);
					
					publishProgress(upload_info.getString("title"));
			        RestClient.UploadPicture(upload_info.getString("filename"),
							    			 upload_info.getString("title"),
								    		 upload_info.getString("comment"),
								    		 upload_info.getString("tags"),
								    		 upload_info.getBoolean("is_public"),
								    		 upload_info.getBoolean("is_friend"),
								    		 upload_info.getBoolean("is_family"),
								    		 upload_info.getInt("safety_level"));

			        broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_FINISHED);
					getApplicationContext().sendBroadcast(broadcast_intent);
				}
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
		}
		
		@Override
		protected void onPostExecute(Object result) {
			((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
			stopSelf();
		}
		
		public void addUpload(Bundle upload_info) {
			if (m_uploads == null) {
				m_uploads = new LinkedList<Bundle>();
			}
			m_uploads.add(upload_info);
		}
		
		public LinkedList<Bundle> getUploads() {
			return m_uploads;
		}
		
		LinkedList<Bundle> m_uploads = null;
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
		m_notify_activity = PendingIntent.getActivity(this, 0, new Intent(this, UploadProgress.class), 0);
		
		if (m_notification != null) {
			m_notification.setLatestEventInfo(getApplicationContext(),
											  this.getString(R.string.app_name),
											  this.getString(R.string.uploadingpicture),
											  m_notify_activity);
			m_notification.flags = Notification.FLAG_NO_CLEAR;
		}
		
		m_upload_task = (UploadPictureTask)new UploadPictureTask();
		m_upload_task.addUpload(intent.getExtras());
		m_upload_task.execute();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return m_binder;
	}

	public void addUpload(Bundle upload_info) {
		m_upload_task.addUpload(upload_info);
	}
	
	public LinkedList<Bundle> getUploads() {
		return m_upload_task.getUploads();
	}
	
	private Notification m_notification = null;
	private PendingIntent m_notify_activity = null;
	private final IBinder m_binder = new UploadBinder();
	private UploadPictureTask m_upload_task = null;
}
