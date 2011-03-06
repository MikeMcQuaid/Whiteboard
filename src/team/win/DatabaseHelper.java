/**
 * Copyright 2011 TeamWin
 */
package team.win;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseHelper {

	private static final String TAG = "TW_DatabaseHelper";
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
	
	public List<WhiteBoard> getWhiteBoards() {
		checkConnectionOpen();
		List<WhiteBoard> whiteBoards = new LinkedList<WhiteBoard>();
		
		// For each row in the white boards table we initalise a WhiteBoard object.
		Cursor cursor = database.query(WhiteBoardsTable.TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			WhiteBoard nextWhiteBoard = new WhiteBoard();
			nextWhiteBoard.id = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.ID));
			nextWhiteBoard.title = cursor.getString(cursor.getColumnIndex(WhiteBoardsTable.TITLE));
			nextWhiteBoard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.LAST_MODIFIED));
			whiteBoards.add(nextWhiteBoard);
		}
		
		return whiteBoards;
	}
	
	public WhiteBoard getWhiteBoard(long id) {
		checkConnectionOpen();
		
		Cursor cursor = database.query(WhiteBoardsTable.TABLE_NAME, null, WhiteBoardsTable.ID + "=?", new String[] {String.valueOf(id)}, null, null, null);
		if (cursor.moveToNext()) {
			WhiteBoard whiteBoard = new WhiteBoard();
			whiteBoard.id = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.ID));
			whiteBoard.title = cursor.getString(cursor.getColumnIndex(WhiteBoardsTable.TITLE));
			whiteBoard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteBoardsTable.LAST_MODIFIED));
			return whiteBoard;
		} else {
			return null;
		}
	}
	
	public void addWhiteBoard(WhiteBoard whiteBoard) {
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
	
	private static class OpenHelper extends SQLiteOpenHelper {

		OpenHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase database) {
			Log.i(TAG, "Creating database tables");
			
			// Contacts table
			String createContactsTableSQL = "CREATE TABLE " + WhiteBoardsTable.TABLE_NAME + " (" +
				WhiteBoardsTable.ID + " INTEGER PRIMARY KEY NOT NULL, " +
				WhiteBoardsTable.TITLE + " TEXT NOT NULL, " +
				WhiteBoardsTable.LAST_MODIFIED + " INTEGER NOT NULL" +
				");";
			database.execSQL(createContactsTableSQL);
			
			Log.i(TAG, "Database tables successfully created");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// No upgrades required... yet.
		}
		
	}
	
	private static class WhiteBoardsTable {
		public static final String TABLE_NAME = "Whiteboards";
		public static final String ID = "Id";
		public static final String TITLE = "Title";
		public static final String LAST_MODIFIED = "LastModified";
	}
	
	public interface Listener {
		public void dataChanged();
	}

}
