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
import android.content.ServiceConnection;

public class UploadProgress extends Activity implements OnClickListener {

	public class UploadBroadcastReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals(GlobalResources.INTENT_UPLOAD_STARTED)) {
	            updateProgress();
	        }
		}
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.upload_progress);
		
        m_svc = new ServiceConnection() {
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				m_uploader = ((Uploader.UploadBinder)service).getService();
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				m_uploader = null;
			}
        };
        
        this.bindService(new Intent(this, Uploader.class), m_svc, 0);
        m_receiver = new UploadBroadcastReceiver();
		if (m_receiver != null) {
			this.registerReceiver(m_receiver, new IntentFilter(GlobalResources.INTENT_UPLOAD_STARTED));
		}
		updateProgress();
	}

	@Override
	protected void onDestroy() {
		if (m_svc != null) {
			this.unbindService(m_svc);
		}
		if (m_receiver != null) {
			this.unregisterReceiver(m_receiver);
		}
	}
	
	@Override
	public void onClick(View v) {
	}
	
	public void addUpload(Bundle upload_info) {
		if (m_uploader != null) {
			m_uploader.addUpload(upload_info);
		}
	}
	
	public void updateProgress() {
    	if (m_uploader != null) {
			LinkedList<Bundle> upload_list = m_uploader.getUploads();
			for (Bundle upload_info : upload_list) {
				//TODO: Put code here to display list of uploads.
			}
    	}
	}
	
	private UploadBroadcastReceiver m_receiver = null;
    private Uploader m_uploader = null;
    private ServiceConnection m_svc = null;
}
