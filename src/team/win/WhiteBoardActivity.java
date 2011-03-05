/**
 * Copyright 2011 TeamWin
 */
package team.win;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class WhiteBoardActivity extends Activity {
	
	private static final String TAG = "WhiteBoardActivity";

	private DataStore mDataStore = new DataStore();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(new WhiteBoardView(this, mDataStore));
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
		default:
			return super.onContextItemSelected(item);
		}
	}

}
