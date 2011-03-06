package team.win;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class Utils {

	private static final String LOG_TAG = buildLogTag(Utils.class);

	private Utils() {
		throw new AssertionError("Utility class");
	}
	
	public static String buildLogTag(Class<?> clazz) {
		return "TW_" + clazz.getSimpleName();
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
	
	/**
	 * Returns the nicely formatted IP address.
	 * 
	 * @return for example: "Access at: http://1.2.3.4:8080/"
	 */
	public static String getFormattedUrl(Resources resources) {
		Enumeration<NetworkInterface> networkInterfaces;
		try {
			networkInterfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			Log.e(LOG_TAG, "Could not enumerate network interfaces", e);
			return resources.getString(R.string.error_remoteurl);
		}
		
		String remoteUrlFormat = resources.getString(R.string.label_remoteurl);
		while (networkInterfaces.hasMoreElements()) {
			NetworkInterface networkInterface = networkInterfaces.nextElement();
			for (Enumeration<InetAddress> inetAddresses = networkInterface.getInetAddresses(); inetAddresses.hasMoreElements();) {
				InetAddress inetAddress = inetAddresses.nextElement();
				if (!inetAddress.isLoopbackAddress()) {
					return String.format(remoteUrlFormat, inetAddress.toString(), HttpService.PORT_NUMBER);
				}
			}
		}
		return resources.getString(R.string.error_remoteurl);
	}
}
