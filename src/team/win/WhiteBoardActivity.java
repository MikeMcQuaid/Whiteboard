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
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.Toast;

public class WhiteBoardActivity extends Activity {
	
	private static final int STROKE_WIDTH_DIALOG_ID = 0;
	private static final int COLOR_PICKER_DIALOG_ID = 1;
	
	private static final String TAG = "TW_WhiteBoardActivity";
	
	private static final String WHITEBOARD_DATA_FOLDER_PATH;
	static {
		StringBuffer whiteBoardFolderPath = new StringBuffer(Environment.getExternalStorageDirectory().getAbsolutePath());
		whiteBoardFolderPath.append(File.separatorChar).append("Android").append(File.separatorChar).append("data").append(File.separatorChar)
						  .append(WhiteBoardActivity.class.getPackage().getName())
						  .append(File.separatorChar).append("whiteboards").append(File.separatorChar);
		WHITEBOARD_DATA_FOLDER_PATH = whiteBoardFolderPath.toString();
	}
	
	private static final Integer[] COLORS = { Color.BLACK, Color.DKGRAY,
		Color.BLUE, Color.GREEN, Color.CYAN, Color.RED, Color.MAGENTA,
		0xFFFF6800, Color.YELLOW, Color.LTGRAY, Color.GRAY, Color.WHITE, };

	private DataStore mDataStore = new DataStore();
	private WhiteBoardView mWhiteBoardView;
	private DatabaseHelper databaseHelper;
	private WhiteBoard whiteBoard;

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
		
		databaseHelper = new DatabaseHelper(this);
		if (getIntent().hasExtra("ID")) {
			whiteBoard = databaseHelper.getWhiteBoard(getIntent().getLongExtra("ID", -1));
			try {
				loadFromSdCard();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
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
			save();
			return true;
		case R.id.menu_load:
			//loadFromSdCard();
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
	
	private void save() {
		try {
			// If this is the first save then we need to create the database entry before saving the 
			// data file as we need the white board id to create the data file.
			if (whiteBoard == null) {
				whiteBoard = new WhiteBoard();
				whiteBoard.title = getResources().getString(R.string.label_defaultWhiteBoardTitle);
				databaseHelper.addWhiteBoard(whiteBoard);
			}
			
			saveToSdCard();
			
			whiteBoard.lastModified = System.currentTimeMillis();
			databaseHelper.addWhiteBoard(whiteBoard);
		} catch (IOException e) {
			new AlertDialog.Builder(this)
				.setCancelable(false)
				.setMessage(e.getMessage())
				.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		}
	}

	private String saveToSdCard() throws IOException {
		// Is the SD card available.
		// Note: if it isn't available then the device might be connected to a PC
		// with the SD card mounted.
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new IOException(getResources().getString(R.string.error_noExternalStorage));
		}

		StringBuffer whiteBoardFilePath = new StringBuffer(WHITEBOARD_DATA_FOLDER_PATH);
		whiteBoardFilePath.append(whiteBoard.id).append(".dat");
		
		// Create the white board file if it doesn't exist.
		File whiteBoardFile = new File(whiteBoardFilePath.toString());
		if (!whiteBoardFile.exists()) {
			// Make sure the path to the file exists.
			new File(WHITEBOARD_DATA_FOLDER_PATH).mkdirs();
			
			if (!whiteBoardFile.createNewFile()) {
				Log.e(TAG, "Failed to create new white board data file. " + whiteBoardFilePath.toString());
				throw new IOException(getResources().getString(R.string.error_savingWhiteBoard));
			}
		}
		
		// Serialise the white board to the file.
		try {
			mDataStore.serializeDataStore(new FileOutputStream(whiteBoardFile));
		} catch (FileNotFoundException e) {
			Log.e(TAG, "Failed to write white board data file. " + e.getMessage());
			throw new IOException(getResources().getString(R.string.error_savingWhiteBoard));
		} catch (IOException e) {
			Log.e(TAG, "Failed to write white board data file. " + e.getMessage());
			throw new IOException(getResources().getString(R.string.error_savingWhiteBoard));
		}
		return whiteBoardFile.getAbsolutePath();
	}
	
	private void loadFromSdCard() throws IOException {
		// Is the SD card available.
		// Note: if it isn't available then the device might be connected to a PC
		// with the SD card mounted.
		if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			throw new IOException(getResources().getString(R.string.error_noExternalStorage));
		}

		StringBuffer whiteBoardFilePath = new StringBuffer(WHITEBOARD_DATA_FOLDER_PATH);
		whiteBoardFilePath.append(whiteBoard.id).append(".dat");
		
		try {
			mDataStore.deserializeDataStore(new FileInputStream(new File(whiteBoardFilePath.toString())));
		} catch (FileNotFoundException e) {
			System.out.println("Could not load save file");
			e.printStackTrace();
		} catch (IOException e) {
			System.out.println("I/O error loading save file");
			e.printStackTrace();
		}
	}

}
