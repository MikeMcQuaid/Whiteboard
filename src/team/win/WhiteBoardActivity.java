/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WhiteBoardActivity extends Activity {
	
	private static final String TAG = "WhiteBoardActivity";

	private DataStore mDataStore = new DataStore();
	private WhiteBoardView mWhiteBoardView;
	
	// FIXME: temporary
	private Random mRandomSource = new Random();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWhiteBoardView = new WhiteBoardView(this, mDataStore);
		setContentView(mWhiteBoardView);
		
		bindService(makeServiceIntent(), serviceConnection, 0);
	}
	
	private Intent makeServiceIntent() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), HttpService.class);
		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.whiteboard_menu, menu);
		return true;
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			return true;
		case R.id.menu_widget:
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("teamwin", "Service connected");
			mWhiteBoardView.setHttpService(((HttpService.HttpServiceBinder) service).getService());
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("teamwin", "Service disconnected");
		}
	};
}
