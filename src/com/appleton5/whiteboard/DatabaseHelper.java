/**
 * Copyright 2011 Appleton 5 Software. All rights reserved.
 */
package com.appleton5.whiteboard;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.List;

import com.appleton5.android.utils.LogUtils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import team.win.R;

public class DatabaseHelper {

	private static final String TAG = LogUtils.generateTag("Whiteboard", DatabaseHelper.class);
	private static final String DATABASE_NAME = "teamwin.db";
	private static final int DATABASE_VERSION = 1;
	
	private Context context;
	/**
	 * A helper class to manage database opening, creation and version management.
	 */
	private OpenHelper openHelper;
	/**
	 * Database reference used to manage the data in the database.
	 */
	private SQLiteDatabase database;
	private List<DatabaseHelper.Listener> listeners;
	
	public DatabaseHelper(Context context) {
		this.context = context;
		this.listeners = new LinkedList<DatabaseHelper.Listener>();
	}
	
	public void addListener(DatabaseHelper.Listener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(DatabaseHelper.Listener listener) {
		listeners.remove(listener);
	}
	
	/**
	 * Opens a writable connection to the database.
	 */
	private void open() {
		openHelper = new OpenHelper(context);
		database = openHelper.getWritableDatabase();
	}
	
	/**
	 * Checks the database connection is open, opening it if it is not.
	 */
	private void checkConnectionOpen() {
		if (openHelper == null) {
			open();
		}
	}
	
	/**
	 * Closes the database connection.
	 */
	public void close() {
		openHelper.close();
		openHelper = null;
		database = null;
	}
	
	public List<Whiteboard> getWhiteBoards() {
		checkConnectionOpen();
		List<Whiteboard> whiteBoards = new LinkedList<Whiteboard>();
		
		// For each row in the white boards table we initialise a WhiteBoard object.
		Cursor cursor = database.query(WhiteBoardsTable.TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Whiteboard nextWhiteBoard = new Whiteboard();
			nextWhiteBoard.id = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.ID));
			nextWhiteBoard.title = cursor.getString(cursor.getColumnIndex(WhiteBoardsTable.TITLE));
			nextWhiteBoard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.LAST_MODIFIED));
			whiteBoards.add(nextWhiteBoard);
		}
		cursor.close();
		
		return whiteBoards;
	}
	
	public Whiteboard getWhiteBoard(long id) {
		checkConnectionOpen();
		
		Cursor cursor = database.query(WhiteBoardsTable.TABLE_NAME, null, WhiteBoardsTable.ID + "=?", new String[] {String.valueOf(id)}, null, null, null);
		try {
			if (cursor.moveToNext()) {
				Whiteboard whiteBoard = new Whiteboard();
				whiteBoard.id = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.ID));
				whiteBoard.title = cursor.getString(cursor.getColumnIndex(WhiteBoardsTable.TITLE));
				whiteBoard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.LAST_MODIFIED));
				return whiteBoard;
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}
	
	public void addWhiteBoard(Whiteboard whiteBoard) {
		checkConnectionOpen();
		
		ContentValues content = new ContentValues();
		content.put(WhiteBoardsTable.TITLE, whiteBoard.title);
		content.put(WhiteBoardsTable.LAST_MODIFIED, whiteBoard.lastModified);
		
		if (whiteBoard.id < 0) {
			whiteBoard.id = database.insert(WhiteBoardsTable.TABLE_NAME, null, content);
		} else {
			database.update(WhiteBoardsTable.TABLE_NAME, content, WhiteBoardsTable.ID + "=?", new String[] {String.valueOf(whiteBoard.id)});
		}
		
		for (DatabaseHelper.Listener listener : listeners) {
			listener.dataChanged();
		}
	}
	
	public void deleteWhiteBoard(long id) {
		checkConnectionOpen();
		database.delete(WhiteBoardsTable.TABLE_NAME, WhiteBoardsTable.ID + "=?", new String[] {String.valueOf(id)});
		for (DatabaseHelper.Listener listener : listeners) {
			listener.dataChanged();
		}
	}
	
	private static class OpenHelper extends SQLiteOpenHelper {

		private String databaseCreateSql;
		
		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);

            // Load the database creation SQL.
            // Loaded here as we have access to the Context object.
			InputStreamReader inputStreamReader = new InputStreamReader(context.getResources().openRawResource(R.raw.database_create_sql));
            StringBuffer stringBuffer = new StringBuffer();
            int currentChar;
            try {
                while ((currentChar = inputStreamReader.read()) >= 0) {
                    stringBuffer.append(currentChar);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getLocalizedMessage());
            }
            databaseCreateSql = stringBuffer.toString();
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			Log.i(TAG, "Creating database tables");
			
			database.execSQL(databaseCreateSql);
			
			Log.i(TAG, "Database tables successfully created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// No upgrades required... yet.
		}
		
	}

    /**
     * Whiteboards table identifiers, useful for safely interacting with the database.
     */
	public static class WhiteBoardsTable {
		public static final String TABLE_NAME = "Whiteboards";
		public static final String ID = "Id";
		public static final String TITLE = "Title";
		public static final String LAST_MODIFIED = "LastModified";
	}
	
	public interface Listener {
		public void dataChanged();
	}

}
