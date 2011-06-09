/**
 * Copyright 2011 Appleton 5 Software. All rights reserved.
 */
package com.appleton5.whiteboard;

import java.util.LinkedList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.appleton5.whiteboard.DatabaseHelper.WhiteboardsTable;

/**
 * 
 */
public class WhiteboardManager {
	
	private DatabaseHelper databaseHelper;
	private SQLiteDatabase database;
	private List<WhiteboardManager.Listener> listeners;
	private List<Whiteboard> whiteboards;
	
	public WhiteboardManager(Context context) {
		databaseHelper = new DatabaseHelper(context);
		database = databaseHelper.getDatabase();
		listeners = new LinkedList<WhiteboardManager.Listener>();
	}
	
	public void cleanup() {
		databaseHelper.close();
		databaseHelper = null;
		database = null;
		listeners.clear();
		whiteboards.clear();
	}
	
	public void addListener(WhiteboardManager.Listener listener) {
		if (!listeners.contains(listener)) {
			listeners.add(listener);
		}
	}
	
	public void removeListener(WhiteboardManager.Listener listener) {
		listeners.remove(listener);
	}
	
	public List<Whiteboard> getWhiteboards() {
		List<Whiteboard> whiteboards = new LinkedList<Whiteboard>();
		
		// For each row in the white boards table we initalise a Whiteboard object.
		Cursor cursor = database.query(WhiteboardsTable.TABLE_NAME, null, null, null, null, null, null);
		while (cursor.moveToNext()) {
			Whiteboard nextWhiteboard = new Whiteboard();
			nextWhiteboard.id = cursor.getInt(cursor.getColumnIndex(WhiteboardsTable.ID));
			nextWhiteboard.title = cursor.getString(cursor.getColumnIndex(WhiteboardsTable.TITLE));
			nextWhiteboard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteboardsTable.LAST_MODIFIED));
			whiteboards.add(nextWhiteboard);
		}
		cursor.close();
		
		return whiteboards;
	}
	
	public Whiteboard getWhiteboard(long id) {
		if (database == null) {
			return null;
		}
		
		Cursor cursor = database.query(WhiteboardsTable.TABLE_NAME, null, WhiteboardsTable.ID + "=?", new String[] {String.valueOf(id)}, null, null, null);
		try {
			if (cursor.moveToNext()) {
				Whiteboard whiteboard = new Whiteboard();
				whiteboard.id = cursor.getInt(cursor.getColumnIndex(WhiteboardsTable.ID));
				whiteboard.title = cursor.getString(cursor.getColumnIndex(WhiteboardsTable.TITLE));
				whiteboard.lastModified = cursor.getInt(cursor.getColumnIndex(WhiteboardsTable.LAST_MODIFIED));
				return whiteboard;
			} else {
				return null;
			}
		} finally {
			cursor.close();
		}
	}
	
	public void addWhiteboard(Whiteboard whiteboard) {
		ContentValues content = new ContentValues();
		content.put(WhiteboardsTable.TITLE, whiteboard.title);
		content.put(WhiteboardsTable.LAST_MODIFIED, whiteboard.lastModified);
		
		if (whiteboard.id < 0) {
			whiteboard.id = database.insert(WhiteboardsTable.TABLE_NAME, null, content);
		} else {
			database.update(WhiteboardsTable.TABLE_NAME, content, WhiteboardsTable.ID + "=?", new String[] {String.valueOf(whiteboard.id)});
		}
		
		for (WhiteboardManager.Listener listener : listeners) {
			listener.dataChanged();
		}
	}
	
	public void deleteWhiteboard(long id) {
		database.delete(WhiteboardsTable.TABLE_NAME, WhiteboardsTable.ID + "=?", new String[] {String.valueOf(id)});
		
		for (WhiteboardManager.Listener listener : listeners) {
			listener.dataChanged();
		}
	}
	
	public interface Listener {
		public void dataChanged();
	}
	
}
