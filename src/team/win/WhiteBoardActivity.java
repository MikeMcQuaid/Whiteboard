/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.util.Arrays;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class WhiteBoardActivity extends Activity implements ColorPickerDialog.OnColorChangedListener {
	
	private static final int STROKE_WIDTH_DIALOG_ID = 1;

	private DataStore mDataStore = new DataStore();
	private WhiteBoardView mWhiteBoardView;

	private enum StrokeWidth {
		NARROW(5, "Narrow"),
		NORMAL(10, "Medium"),
		THICK(15, "Thick"),
		;
		
		static final String[] AS_STRINGS = new String[values().length];
		static {
			StrokeWidth[] values = values();
			for (int i = 0; i < AS_STRINGS.length; i++) {
				AS_STRINGS[i] = values[i].mDisplayName;
			}
		}
		
		final int mWidth;
		final String mDisplayName;
		
		StrokeWidth(int width, String displayName) {
			mWidth = width;
			mDisplayName = displayName;
		}
		
		static int indexOf(StrokeWidth strokeWidth) {
			return Arrays.binarySearch(values(), strokeWidth);
		}
	};
	
	private StrokeWidth mLastWidth = StrokeWidth.NORMAL;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mWhiteBoardView = new WhiteBoardView(this, mDataStore, mLastWidth.mWidth, Color.RED);
		setContentView(mWhiteBoardView);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
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
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_save:
			return true;
		case R.id.menu_stroke_width:
			showDialog(STROKE_WIDTH_DIALOG_ID);
			return true;
		case R.id.menu_color:
			new ColorPickerDialog(this, this, mWhiteBoardView.getPaint().getColor()).show();
			return true;
		case R.id.menu_undo:
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case STROKE_WIDTH_DIALOG_ID:
			final CharSequence[] items = StrokeWidth.AS_STRINGS;

			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Line thickness");
			builder.setSingleChoiceItems(items, StrokeWidth.indexOf(mLastWidth), new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					mLastWidth = StrokeWidth.values()[item];
					mWhiteBoardView.setPrimStrokeWidth(mLastWidth.mWidth);
					dialog.dismiss();
				}
			});
			return builder.create();
		default:
			return super.onCreateDialog(id);
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

	@Override
	public void colorChanged(int color) {
		mWhiteBoardView.setPrimColor(color);
	}
}
