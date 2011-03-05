/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;

public class WhiteBoardActivity extends Activity {

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
			saveToSdcard();
			return true;
		case R.id.menu_load:
			loadFromSdcard();
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
				Color.argb(255,
						   mRandomSource.nextInt(255),
						   mRandomSource.nextInt(255),
						   mRandomSource.nextInt(255)));
			return true;
		default:
			return super.onOptionsItemSelected(item);
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

	private boolean saveToSdcard() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			System.out.println("External storage not available");
			return false;
		}

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath(); 
		File createDirs[] = {
			new File(baseDir + "/Android"),
			new File(baseDir + "/Android/data"),
			new File(baseDir + "/Android/data/" + getClass().getPackage().getName()),
			new File(baseDir + "/Android/data/" + getClass().getPackage().getName() + "/files")
		};

		for (File createDir : createDirs) {
			if(!createDir.exists()) {
				if(!createDir.mkdir()) {
					System.out.println("Couldn't mkdir " + createDir.getAbsolutePath());
					return false;
				}
			}
		}

		File file = new File(createDirs[createDirs.length - 1], "save.dat");
		try {
			mDataStore.serializeDataStore(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private boolean loadFromSdcard() {
		if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			System.out.println("External storage not available");
			return false;
		}

		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		File file = new File(baseDir + "/Android/data/" + getClass().getPackage().getName() + "/files", "save.dat");
		try {
			mDataStore.deserializeDataStore(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			System.out.println("Could not load save file");
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			System.out.println("I/O error loading save file");
			e.printStackTrace();
			return false;
		}

		return true;
	}
}
