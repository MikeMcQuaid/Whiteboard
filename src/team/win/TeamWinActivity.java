/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class TeamWinActivity extends ListActivity implements DatabaseHelper.Listener {
	
	private static final String TAG = "TW_TeamWinActivity";
	
	private DatabaseHelper databaseHelper;
	private WhiteBoardListAdapter listAdapter;
	private List<WhiteBoard> existingWhiteBoards;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		displayRemoteUrl();
		
		databaseHelper = new DatabaseHelper(this);
		existingWhiteBoards = databaseHelper.getWhiteBoards();
		databaseHelper.addListener(this);
		listAdapter = new WhiteBoardListAdapter();
		setListAdapter(listAdapter);
		
		final Button addWhiteboardButton = (Button) findViewById(R.id.button_add_whiteboard);
		addWhiteboardButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				startActivity(new Intent(TeamWinActivity.this, WhiteBoardActivity.class));
			}
		});
		
		startService(makeServiceIntent());
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
			//finish();
			WhiteBoard newWhiteBoard = new WhiteBoard();
			newWhiteBoard.title = "Test";
			newWhiteBoard.lastModified = 12324;
			databaseHelper.addWhiteBoard(newWhiteBoard);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		databaseHelper.removeListener(this);
		
		if (isFinishing()) {
			stopService(makeServiceIntent());
		}
	}

	private Intent makeServiceIntent() {
		Intent intent = new Intent();
		intent.setClass(getApplicationContext(), HttpService.class);
		return intent;
	}
	
	/**
	 * Displays the remote URL in the activity to access the white board.
	 */
	private void displayRemoteUrl() {
		TextView remoteUrlTextView = (TextView) findViewById(R.id.header_remoteurl);
		String remoteUrlFormat = getResources().getString(R.string.label_remoteurl);
		
		try {
			for (Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces(); networkInterfaces.hasMoreElements();) {
				NetworkInterface networkInterface = networkInterfaces.nextElement();
				for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements();) {
					InetAddress inetAddress = inetAddresses.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						remoteUrlTextView.setText(String.format(remoteUrlFormat, inetAddress.toString(), HttpService.PORT_NUMBER));
					}
				}
			}
		} catch (SocketException e) {
			Log.e(TAG, e.getMessage());
			remoteUrlTextView.setText(getResources().getString(R.string.error_remoteurl));
		}
	}
	
	@Override
	public void dataChanged() {
		existingWhiteBoards = databaseHelper.getWhiteBoards();
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				listAdapter.notifyDataSetChanged();
			}
		});
	}

	private class WhiteBoardListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return existingWhiteBoards.size();
		}

		@Override
		public Object getItem(int position) {
			return existingWhiteBoards.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(R.layout.listitem_whiteboard, null);
			}
			
			String title = existingWhiteBoards.get(position).title;
			String lastModifiedDateTime = String.valueOf(existingWhiteBoards.get(position).lastModified);
			
			((TextView) convertView.findViewById(R.id.title_whiteboard)).setText(title);
			((TextView) convertView.findViewById(R.id.subtitle_whiteboard)).setText(lastModifiedDateTime);
			
			return convertView;
		}
		
	}
	
}
