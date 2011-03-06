package team.win;

import java.lang.reflect.Method;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Utils {

	private static final String LOG_TAG = "TeamWin";

	private Utils() {
		throw new AssertionError("Utility class");
	}
	
	public static Method quietGetMethod(Class<?> clazz, String name, Class<?>... parameterTypes) {
		try {
			return clazz.getMethod(name, parameterTypes);
		} catch (NoSuchMethodException e) {
			return null;
		}
	}
	
	public static Object quietInvokeMethod(Method method, Object receiver, Object... args) {
		try {
			return method.invoke(receiver, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	public static Object reallyQuietInvokeMethod(Method method, Object receiver, Object... args) {
		try {
			return quietInvokeMethod(method, receiver, args);
		} catch (RuntimeException e) {
			return null;
		}
	}
	
	public static Object quietlyGetStaticField(Class<?> clazz, String name) {
		try {
			return clazz.getField(name).get(null);
		} catch (Exception e) {
			return null;
		}
	}
	
	/**
	 * Requests that the specified menu items be showed in the Honeycomb Action Bar.
	 * 
	 * @see http://developer.android.com/guide/topics/ui/actionbar.html
	 */
	public static boolean showMenuItemsInActionBar(Menu menu, int[] ids) {
		Method showAsActionMethod = Utils.quietGetMethod(MenuItem.class, "setShowAsAction", int.class);
		if (showAsActionMethod == null) {
			return false;
		}
		
		for (int id : ids) {
			// SHOW_AS_ACTION_IF_ROOM & SHOW_AS_ACTION_WITH_TEXT
			MenuItem menuItem = menu.findItem(id);
			if (menuItem != null) {
				Utils.reallyQuietInvokeMethod(showAsActionMethod, menuItem, 1 | 4);
			} else {
				Log.e(LOG_TAG, "Could not find menu item " + id);
			}
		}
		return true;
	}
}
