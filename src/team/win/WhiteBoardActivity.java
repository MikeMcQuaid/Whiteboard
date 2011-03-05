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

	private enum StrokeWidth {
		SMALL(5),
		MEDIUM(10),
		LARGE(15);
		int mWidth;
		StrokeWidth(int width) {
			mWidth = width;
		}
	};
	
	private StrokeWidth mLastWidth = StrokeWidth.SMALL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWhiteBoardView = new WhiteBoardView(this, mDataStore, mLastWidth.mWidth, Color.RED);
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
		switch (item.getItemId()) {
		case R.id.menu_save:
			return true;
		case R.id.menu_stroke_width:
			switch(mLastWidth) {
			case SMALL:
				mLastWidth = StrokeWidth.MEDIUM;
				break;
			case MEDIUM:
				mLastWidth = StrokeWidth.LARGE;
				break;
			case LARGE:
				mLastWidth = StrokeWidth.SMALL;
				break;
			}
			mWhiteBoardView.setPrimStrokeWidth(mLastWidth.mWidth);
			return true;
		case R.id.menu_color:
			mWhiteBoardView.setPrimColor(
				Color.argb(mRandomSource.nextInt(255),
						   mRandomSource.nextInt(255),
						   mRandomSource.nextInt(255),
						   mRandomSource.nextInt(255)));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

}
