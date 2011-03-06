/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.util.Date;
import java.util.List;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class TeamWinActivity extends ListActivity implements DatabaseHelper.Listener {
	
	private static final String TAG = "TW_TeamWinActivity";
	
	private static final int ID_CONTEXTMENU_CHANGE_TITLE = 0;
	private static final int ID_CONTEXTMENU_DELETE_WHITEBOARD = 1;
	
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
		
		registerForContextMenu(findViewById(android.R.id.list));
		
		startService(makeServiceIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
		existingWhiteBoards = databaseHelper.getWhiteBoards();
		dataChanged();
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
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (position == 0) {
			startActivity(new Intent(TeamWinActivity.this, WhiteBoardActivity.class));
		} else {
			startActivity(new Intent(this, WhiteBoardActivity.class).putExtra("ID", existingWhiteBoards.get(position - 1).id));
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		if (v.getId() == android.R.id.list && ((AdapterContextMenuInfo) menuInfo).id > 0) {
			super.onCreateContextMenu(menu, v, menuInfo);
			menu.setHeaderTitle(R.string.title_contextmenu_whiteboards);
			menu.add(0, ID_CONTEXTMENU_CHANGE_TITLE, 0, R.string.contextmenu_change_title);
			menu.add(0, ID_CONTEXTMENU_DELETE_WHITEBOARD, 1, R.string.contextmenu_delete_whiteboard);
		}
	}

	@Override
	public boolean onContextItemSelected(final MenuItem item) {
		switch (item.getItemId()) {
		case ID_CONTEXTMENU_CHANGE_TITLE:
			final EditText inputField = new EditText(this);
			final AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
			final WhiteBoard currentWhiteBoard = existingWhiteBoards.get((int) info.id);
			inputField.setText(currentWhiteBoard.title);
			
			new AlertDialog.Builder(this)
				.setTitle(R.string.title_dialog_change_title)
				.setView(inputField)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						currentWhiteBoard.title = inputField.getText().toString();
						databaseHelper.addWhiteBoard(currentWhiteBoard);
						dialog.dismiss();
					}
				})
				.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
			return true;
		case ID_CONTEXTMENU_DELETE_WHITEBOARD:
			new AlertDialog.Builder(this)
				.setTitle(R.string.title_dialog_delete)
				.setMessage(R.string.confirm_deleteWhiteBoard)
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
						databaseHelper.deleteWhiteBoard(existingWhiteBoards.get((int) info.id).id);
						dialog.dismiss();
					}
				})
				.setNeutralButton(R.string.button_cancel, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				})
				.show();
			return true;
		default:
			return super.onContextItemSelected(item);
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
		intent.setClass(this, HttpService.class);
		return intent;
	}
	
	/**
	 * Displays the remote URL in the activity to access the white board.
	 */
	private void displayRemoteUrl() {
		TextView remoteUrlTextView = (TextView) findViewById(R.id.header_remoteurl);
		remoteUrlTextView.setText(Utils.getFormattedUrl(getResources()));
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
			return existingWhiteBoards.size() + 1;
		}

		@Override
		public Object getItem(int position) {
			if (position == 0) {
				return null;
			} else {
				return existingWhiteBoards.get(position - 1);
			}
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
			
			String title;
			String subtitle;
			if (position == 0) {
				title = "Create White Board";
				subtitle = "";
			} else {
				title = existingWhiteBoards.get(position - 1).title;
				Date date = new Date(existingWhiteBoards.get(position - 1).lastModified * 1000L);
				subtitle = DateFormat.getDateFormat(TeamWinActivity.this).format(date);
			}
			
			((TextView) convertView.findViewById(R.id.title_whiteboard)).setText(title);
			((TextView) convertView.findViewById(R.id.subtitle_whiteboard)).setText(subtitle);
			
			return convertView;
		}
		
	}
	
}
