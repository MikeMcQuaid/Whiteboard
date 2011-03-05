package team.win;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import org.json.JSONException;

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

public class TeamWinActivity extends Activity {
	
	private static final String TAG = "TeamWinActivity";

	private HttpService httpService;
	private WhiteBoardView whiteBoardView;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		logIpAddresses();
		
		whiteBoardView = new WhiteBoardView(this, new DataStore());
		setContentView(whiteBoardView);
		
		startService(makeServiceIntent()); // so that it doesn't die
		bindService(makeServiceIntent(), serviceConnection, 0);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_shutdown:
			// TODO We need to properly shutdown the HTTP server.
			// We want to allow the user to switch to other applications
			// whilst the whiteboard is running and still give the user the ability to
			// explicitly shutdown the application and stop the web server.
			finish();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (isFinishing()) {
			stopService(makeServiceIntent());
		}
	}

	private Intent makeServiceIntent() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), HttpService.class);
		return intent;
	}
	
	private void logIpAddresses() {
		// TODO expose this in the UI
		try {
			for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements();) {
					Log.e("teamwin", inetAddresses.nextElement().toString());
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.w("teamwin", "Service connected");
			whiteBoardView.setHttpService(((HttpService.HttpServiceBinder) service).getService());
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.w("teamwin", "Service disconnected");
		}
	};
}
