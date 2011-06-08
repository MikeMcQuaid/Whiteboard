/**
 * Copyright 2011 Appleton 5 Software. All rights reserved.
 */
package com.appleton5.android.utils;

/**
 * Logging utility methods.
 */
public class LogUtils {

	/**
	 * Prefix applied to all generated tags.
	 */
	public static final String TAG_PREFIX = "A5_";
	
	/**
	 * Wrapper method for {@link LogUtils#generateTag(String, Class)} using a null identifier.
	 * 
	 * @param callingClass
	 * 
	 * @return Prefixed tag for the given class.
	 */
	public static String generateTag(Class<?> callingClass) {
		return generateTag(null, callingClass);
	}
	
	/**
	 * Generates a logging tag using the optional given identifier and simple name of the given class, prefixed
	 * with {@link LogUtils#TAG_PREFIX}. E.g., using the identifier 'Library' with LogUtils.class gives
	 * the tag: 'A5_Library_LogUtils'. No identifier would give the tag: 'A5_LogUtils'.
	 * 
	 * @param identifier Optional identifier added to the tag for easier filtering of logs.
	 * @param callingClass
	 * 
	 * @return Prefixed tag for the given class.
	 */
	public static String generateTag(String identifier, Class<?> callingClass) {
		if (identifier == null || identifier.length() < 1) {
			return TAG_PREFIX + callingClass.getSimpleName();			
		} else {
			return TAG_PREFIX + identifier + "_" + callingClass.getSimpleName();
		}
	}
	
}
