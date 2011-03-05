package team.win;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class TeamWinActivity extends Activity {
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		startService(makeServiceIntent());
		logIpAddresses();
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
	
}