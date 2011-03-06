/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class WhiteBoardActivity extends Activity {
	
	private static final int STROKE_WIDTH_DIALOG_ID = 0;
	private static final int COLOR_PICKER_DIALOG_ID = 1;
	
	private static final Integer[] COLORS = { Color.BLACK, Color.DKGRAY,
		Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA,
		0xFFFF6800, Color.YELLOW, Color.LTGRAY, Color.GRAY, Color.WHITE, };

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
		
		// Use Action Bar in Honeycomb by applying Holo theme
		Object holoThemeId = Utils.quietlyGetStaticField(android.R.style.class, "Theme_Holo");
		if (holoThemeId != null) {
			setTheme((Integer) holoThemeId);
		}
		
		mWhiteBoardView = new WhiteBoardView(this, mDataStore, mLastWidth.mWidth, Color.RED);
		setContentView(mWhiteBoardView);
		
		bindService(makeServiceIntent(), serviceConnection, 0);
	}
	
	private Intent makeServiceIntent() {
		Intent intent = new Intent();
		intent.setClass(this, HttpService.class);
		return intent;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.whiteboard_menu, menu);
		// Show menu options in Honeycomb action bar
		Utils.showMenuItemsInActionBar(menu, new int[] { R.id.menu_color, R.id.menu_stroke_width, R.id.menu_erase });
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
			showDialog(STROKE_WIDTH_DIALOG_ID);
			return true;
		case R.id.menu_color:
			showDialog(COLOR_PICKER_DIALOG_ID);
			return true;
		case R.id.menu_erase:
			Toast.makeText(getApplicationContext(), "Implement me!", 3);
			mWhiteBoardView.setPrimColor(Color.WHITE);
			return true;
		case R.id.menu_clear:
			mWhiteBoardView.resetPoints();
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
		case COLOR_PICKER_DIALOG_ID:
			final Dialog dialog = new Dialog(this);
			dialog.setTitle("Choose colour");
			dialog.setContentView(R.layout.color_picker);
			GridView gridView = (GridView) dialog.findViewById(R.id.color_picker_gridview);
			gridView.setAdapter(new ArrayAdapter<Integer>(this, 0, COLORS) {
				@Override
				public View getView(int position, View convertView, ViewGroup parent) {
					final int color = COLORS[position];
					View colorView = new View(WhiteBoardActivity.this);
					colorView.setMinimumWidth(45);
					colorView.setMinimumHeight(45);
					colorView.setBackgroundColor(color);
					return colorView;
				}
			});
			gridView.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
					mWhiteBoardView.setPrimColor(COLORS[position]);
					dialog.dismiss();
				}
			});
			return dialog;
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
