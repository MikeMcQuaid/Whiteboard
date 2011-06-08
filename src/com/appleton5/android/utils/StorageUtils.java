/**
 * Copyright 2011 Appleton 5 Software. All rights reserved.
 */
package com.appleton5.android.utils;

import java.io.File;
import java.lang.reflect.Method;

import team.win.Utils;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

/**
 * Provides utility methods for interacting with the device storage.
 */
public class StorageUtils {
	
	private static final String TAG = Utils.buildLogTag(StorageUtils.class);
	
	// References to methods introduced in API 8 for retrieving application directories on external
	// storage. If they are non-null then we are running on a 2.2+ device and should use these methods
	// over constructing the path manually.
	private static Method getExternalFilesDirMethod;
	private static Method getExternalCacheDirMethod;
	
	static {
		detectAPICompatibility();
	}
	
	private static void detectAPICompatibility() {
		try {
			// API 8 introduced a method to retrieve a File object representing the absolute path to the directory
			// on the external filesystem where the application can place persistent files it owns.
			//
			// We use reflection to get a reference to the method if it is available.
			getExternalFilesDirMethod = Context.class.getMethod("getExternalFilesDir", new Class[] {String.class});
		} catch (SecurityException e) {
			Log.d(TAG, e.getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			Log.d(TAG, e.getLocalizedMessage());
		}
		
		try {
			// API 8 introduced a method to retrieve a File object representing the absolute path to the directory
			// on the external filesystem where the application can place cached files it owns.
			//
			// We use reflection to get a reference to the method if it is available.
			getExternalCacheDirMethod = Context.class.getMethod("getExternalCacheDir", (Class[]) null);
		} catch (SecurityException e) {
			Log.d(TAG, e.getLocalizedMessage());
		} catch (NoSuchMethodException e) {
			Log.d(TAG, e.getLocalizedMessage());
		}
	}
	
	private static StringBuffer getPreAPI8ApplicationExternalStoragePath(Context context) {
		StringBuffer outputBuffer = new StringBuffer(Environment.getExternalStorageDirectory().getAbsolutePath());
		outputBuffer.append(File.separatorChar).append("Android")
					.append(File.separatorChar).append("data")
					.append(File.separatorChar).append(context.getPackageName());
		return outputBuffer;
	}
	
	/**
	 * API 1+ compatible method for getting the path of the directory holding application files on external storage.
	 * 
	 * @param context
	 * 
	 * @return Returns the path of the directory holding application files on external storage. Returns null if external storage is not 
	 *         currently mounted so it could not ensure the path exists; you will need to call this method again when it is available.
	 */
	public static File getApplicationExternalFilesDir(Context context) {
		// Is the API 8+ method available?
		if (getExternalFilesDirMethod != null) {
			try {
				return (File) getExternalFilesDirMethod.invoke(context, new Object[] {null});
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage());
				return null;
			}
		} else {
			String applicationExternalCacheDirPath = getPreAPI8ApplicationExternalStoragePath(context).toString();
			return new File(applicationExternalCacheDirPath);
		}
	}
	
	/**
	 * API 1+ compatible method for getting the path to the directory holding application cache files on external storage.
	 * 
	 * @param context

	 * @return Returns the path of the directory holding application cache files on external storage. Returns null if external storage is not 
	 *         currently mounted so it could not ensure the path exists; you will need to call this method again when it is available.
	 */
	public static File getApplicationExternalCacheDir(Context context) {
		// Is the API 8+ method available?
		if (getExternalCacheDirMethod != null) {
			try {
				return (File) getExternalCacheDirMethod.invoke(context, (Object[]) null);
			} catch (Exception e) {
				Log.e(TAG, e.getLocalizedMessage());
				return null;
			}
		} else {
			if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED) ||
					Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED_READ_ONLY)) {
				String applicationExternalCacheDirPath = getPreAPI8ApplicationExternalStoragePath(context).append(File.separatorChar).append("cache").toString();
				return new File(applicationExternalCacheDirPath);
			} else {
				return null;
			}
		}
	}
	
}
