package com.zmosoft.flickrfree;

import java.util.TreeMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.zmosoft.flickrfree.GlobalResources.ImgSize;

public class AccountView extends Activity implements OnItemClickListener {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.accountview);
    	((ListView)findViewById(R.id.AccountListView)).setOnItemClickListener(this);

    	try {
    		GetActiveAccounts();
    	} catch (JSONException e) {
    		e.printStackTrace();
    	}

		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		SetCurrentAccount(auth_prefs.getString("username", ""));
    }

	private void SetCurrentAccount(int pos) {
		String[] account_names = new String[]{};
		
		account_names = m_accounts.keySet().toArray(account_names);
		if (pos < account_names.length) {
			SetCurrentAccount(account_names[pos]);
		}
	}
	
	private void SetCurrentAccount(String username) {
		((TextView)findViewById(R.id.TextAuthUsernameLabel)).setVisibility(username.equals("") ? View.GONE : View.VISIBLE);
		((TextView)findViewById(R.id.TextAuthUsername)).setVisibility(username.equals("") ? View.GONE : View.VISIBLE);
    	((TextView)findViewById(R.id.TextAuthUsername)).setText(username);
		//((Button)findViewById(R.id.BtnLogOut)).setEnabled(!username.equals(""));

		AuthenticateActivity.SetActiveUser(getSharedPreferences("Auth",0), username);
	}
	
	private void GetActiveAccounts() throws JSONException {
		SharedPreferences auth_prefs = getSharedPreferences("Auth",0);
		m_accounts = new TreeMap<String, JSONObject>();
		
		for (String key : auth_prefs.getAll().keySet()) {
			if (key.contains("FlickrUsername_") && key.indexOf("FlickrUsername_") == 0) {
				m_accounts.put(key.substring(15), new JSONObject(auth_prefs.getString(key, "")));
			}
		}
	}
	
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog = null;
    	AlertDialog.Builder dbuilder;
    	
    	switch(id) {
    	case DIALOG_WARN_LOGOUT:
    		dbuilder = new AlertDialog.Builder(this);
    		dbuilder.setMessage("Are you sure you want to log out?")
    		        .setCancelable(false)
    		        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
    		        	public void onClick(DialogInterface dialog, int id) {
    		    			SetCurrentAccount("");
    					}
    		        })
    		        .setNegativeButton("No", new DialogInterface.OnClickListener() {
    		        	public void onClick(DialogInterface dialog, int id) {
    						dialog.dismiss();
    					}
    		        });
			dialog = dbuilder.create();
			break;
		case DIALOG_SWITCH_ACCOUNT:
			CharSequence[] account_names = new CharSequence[]{};
			account_names = m_accounts.keySet().toArray(account_names);
			
			dbuilder = new AlertDialog.Builder(this)
				.setTitle(R.string.dlg_switch_accounts)
				.setSingleChoiceItems(account_names, ImgSize.MED.getNum(),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int choice) {
							SetCurrentAccount(choice);
							dialog.dismiss();
						}
					})
		        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		        	public void onClick(DialogInterface dialog, int id) {
						dialog.dismiss();
					}
		        });
			dialog = dbuilder.create();
			break;
		}
		return dialog;
    }
    
	@Override
	public void onItemClick(AdapterView parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		if (id == 0) {
			Intent i = new Intent(this,AuthenticateActivity.class);
			startActivityForResult(i,GlobalResources.ADD_ACCOUNT_REQ);
		}		
		else if (id == 1) {
			showDialog(DIALOG_SWITCH_ACCOUNT);
		}
		else if (id == 2) {
    		showDialog(DIALOG_WARN_LOGOUT);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == GlobalResources.ADD_ACCOUNT_REQ) {
			try {
				GetActiveAccounts();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			SetCurrentAccount(getSharedPreferences("Auth",0).getString("username", ""));
		}
	}
	
    TreeMap<String, JSONObject> m_accounts;
    String m_currentAccount;
	
	static final int DIALOG_WARN_LOGOUT = 0;
	static final int DIALOG_SWITCH_ACCOUNT = 1;
}
