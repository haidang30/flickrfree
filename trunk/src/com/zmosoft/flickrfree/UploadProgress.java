package com.zmosoft.flickrfree;

import java.util.LinkedList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;
import android.content.ServiceConnection;

public class UploadProgress extends Activity implements OnClickListener {

	// This is the receiver that we use to know when an upload starts or
	// finishes so we can update the progress display.
	public class UploadStatusReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_STARTED) ||
	        	intent.getAction().equals(GlobalResources.INTENT_UPLOAD_FINISHED)) {
	            updateProgress();
	        }
		}
	}

	// This receiver is necessary to let us know when the Upload service has
	// been successfully bound so we can access it and update the progress
	// display.
	public class BindUploaderReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_BIND_UPLOADER)) {
	            updateProgress();
	        }
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.upload_progress);
		
		bindUploader();
	}
	
	public void bindUploader() {
		m_bind_receiver = new BindUploaderReceiver();
		if (m_bind_receiver != null) {
			this.registerReceiver(m_bind_receiver, new IntentFilter(GlobalResources.INTENT_BIND_UPLOADER));
		}

		this.bindService(new Intent(this, Uploader.class), m_svc, Context.BIND_AUTO_CREATE);

        m_receiver = new UploadStatusReceiver();
		if (m_receiver != null) {
			this.registerReceiver(m_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_STARTED));
			this.registerReceiver(m_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_FINISHED));
		}
	}

	public void unbindUploader() {
		if (m_svc != null) {
			this.unbindService(m_svc);
		}
		if (m_receiver != null) {
			this.unregisterReceiver(m_receiver);
		}
		if (m_bind_receiver != null) {
			this.unregisterReceiver(m_bind_receiver);
		}
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindUploader();
	}
	
	@Override
	public void onClick(View v) {
	}
	
	public void addUpload(Bundle upload_info) {
		if (m_uploader != null && upload_info != null) {
			m_uploader.addUpload(upload_info);
		}
	}
	
	public void updateProgress() {
    	if (m_uploader != null) {
			LinkedList<Bundle> upload_list = m_uploader.getUploads();
			String text = "";
			for (Bundle upload_info : upload_list) {
				//TODO: Put code here to display list of uploads.
				text += upload_info.getString("filename") + "\n";
			}
			((TextView)findViewById(R.id.TextView01)).setText(text);
    	}
	}
	
	private UploadStatusReceiver m_receiver = null;
	private BindUploaderReceiver m_bind_receiver = null;
    private Uploader m_uploader = null;
    
    private ServiceConnection m_svc = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			m_uploader = ((Uploader.UploadBinder)service).getService();
			Intent broadcast_intent = new Intent();
			broadcast_intent.setAction(GlobalResources.INTENT_BIND_UPLOADER);
			getApplicationContext().sendBroadcast(broadcast_intent);
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			m_uploader = null;
		}
    };
    
}
