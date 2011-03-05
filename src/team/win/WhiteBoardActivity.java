/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.util.Random;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
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
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.whiteboard_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		System.out.println("got here" + item);
		switch (item.getItemId()) {
		case R.id.menu_save:
			return true;
		case R.id.menu_widget:
			return true;
		case R.id.menu_color:
			mWhiteBoardView.setPrimColor(Color.argb(mRandomSource.nextInt(255), mRandomSource.nextInt(255), mRandomSource.nextInt(255), mRandomSource.nextInt(255)));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
