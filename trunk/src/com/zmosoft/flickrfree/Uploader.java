package com.zmosoft.flickrfree;

import java.util.LinkedList;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Uploader extends Service {

	// This is the receiver that we use to update the percentage progress display
    // for the current upload.
	public class NotificationProgressUpdateReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE)) {
	        	Bundle extras = intent.getExtras();
	        	if (extras != null && extras.containsKey("percent")) {
					RemoteViews nView = m_notification.contentView;
					nView.setProgressBar(R.id.prgNotification, 100, (int)extras.getLong("percent"), false);
					m_notification.contentView = nView;
					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.UPLOADER_ID, m_notification);
	        	}
	        }
		}
	}

	// AsyncTask to upload a picture in the background.
	private class UploadPictureTask extends AsyncTask<Void, String, Object> {
		
		@Override
		protected Object doInBackground(Void... params) {
			Bundle upload_info = null;
			Intent broadcast_intent = new Intent();
			while (m_uploads.size() > 0) {
				upload_info = m_uploads.get(0);
				if (upload_info != null) {
					// Send out a broadcast to let us know that an upload is starting.
					broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_STARTED);
					getApplicationContext().sendBroadcast(broadcast_intent);
					
					publishProgress(upload_info.getString("title"));
			        JSONObject result = RestClient.UploadPicture(upload_info.getString("filename"),
							    			 upload_info.getString("title"),
								    		 upload_info.getString("comment"),
								    		 upload_info.getString("tags"),
								    		 upload_info.getBoolean("is_public"),
								    		 upload_info.getBoolean("is_friend"),
								    		 upload_info.getBoolean("is_family"),
								    		 upload_info.getInt("safety_level"),
								    		 getApplicationContext());
			        if (result == null || result.has("fail")) {
			        	String err_str = null;
						try {
							err_str = (result == null) ? "Unknown Failure" : result.getString("fail");
						} catch (JSONException e) {
							err_str = "Unknown Failure";
						}

						broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_FAILED);
						broadcast_intent.putExtra("error", err_str);
						getApplicationContext().sendBroadcast(broadcast_intent);

						publishProgress("fail", err_str);
			        	m_uploads.clear();
			        }
				}
				if (!m_uploads.isEmpty()) {
					m_uploads.remove(0);
					// Send out a broadcast to let us know that an upload has finished.
			        broadcast_intent.setAction(GlobalResources.INTENT_UPLOAD_FINISHED);
					getApplicationContext().sendBroadcast(broadcast_intent);
				}
			}
			
			return null;
		}
		
		@Override
		protected void onProgressUpdate(String... progress) {
			// onProgressUpdate is called each time a new upload starts. This allows
			// us to update the notification text to let the user know which picture
			// is being uploaded.
			String title = progress.length > 0 ? progress[0] : "";
			if (title == "fail") {
				String err_str =  (progress.length > 1) ? progress[1] : "Unknown Error";
				((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
				Toast.makeText(getApplicationContext(), "Flickr Upload Failed:\n" + err_str, Toast.LENGTH_SHORT).show();
				stopSelf();
			}
			else {
				if (m_notification != null) {
					RemoteViews nView = m_notification.contentView;
					nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.uploading) + " \"" + title + "\"");
					nView.setProgressBar(R.id.prgNotification, 100, 0, false);
					m_notification.contentView = nView;
	
					((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).notify(GlobalResources.UPLOADER_ID, m_notification);
				}
			}
		}
		
		@Override
		protected void onPreExecute() {
		}
		
		@Override
		protected void onPostExecute(Object result) {
			// When all uploads are finished, kill the status bar upload notification and stop the
			// Uploader service.
			Toast.makeText(getApplicationContext(), "Upload(s) Completed", Toast.LENGTH_SHORT).show();
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
	public void onDestroy () {
		super.onDestroy();
		((NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE)).cancel(GlobalResources.UPLOADER_ID);
		if (m_update_receiver != null) {
			this.unregisterReceiver(m_update_receiver);
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Bundle extras = intent.getExtras();
		if (extras != null) {
			addUpload(intent.getExtras());
		}
		m_update_receiver = new NotificationProgressUpdateReceiver();
		if (m_update_receiver != null) {
			this.registerReceiver(m_update_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_PROGRESS_UPDATE));
		}
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		return m_binder;
	}

	public void addUpload(Bundle upload_info) {
		if (m_upload_task == null || (m_upload_task.getStatus() == AsyncTask.Status.FINISHED)) {
			// If the upload task has not yet been created or if it is finished, then create
			// a new upload task, add the upload to it, and execute.
			m_upload_task = (UploadPictureTask)new UploadPictureTask();
			m_upload_task.addUpload(upload_info);
			m_upload_task.execute();
		}
		else {
			// Otherwise, the upload task is currently running, so add the new upload to the
			// list.
			m_upload_task.addUpload(upload_info);
		}

		if (m_notification == null) {
			// Create the status bar notification that will be displayed.
			CharSequence tickerText = this.getString(R.string.uploadingpicture);
			m_notification = new Notification(android.R.drawable.stat_sys_upload, tickerText, System.currentTimeMillis());
			m_notify_activity = PendingIntent.getActivity(this, 0, new Intent(this, TransferProgress.class), 0);
			m_notification.contentIntent = m_notify_activity;

			RemoteViews nView = new RemoteViews(getPackageName(), R.layout.progress_notification_layout);
			//nView.setImageViewResource(R.id.imgIcon, R.drawable.icon);
			nView.setTextViewText(R.id.txtNotificationTitle, getResources().getString(R.string.uploading) + " \"" + upload_info.getString("title") + "\"");
			m_notification.contentView = nView;
			m_notification.flags = Notification.FLAG_NO_CLEAR;
		}
	}
	
	public LinkedList<Bundle> getUploads() {
		if (m_upload_task == null) {
			return new LinkedList<Bundle>();
		}
		else {
			return m_upload_task.getUploads();
		}
	}
	
	private Notification m_notification = null;
	private NotificationProgressUpdateReceiver m_update_receiver = null;
	private PendingIntent m_notify_activity = null;
	private final IBinder m_binder = new UploadBinder();
	private UploadPictureTask m_upload_task = null;
}
