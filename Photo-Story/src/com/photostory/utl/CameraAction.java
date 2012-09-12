/**
 * 
 */
package com.photostory.utl;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

/**
 * @author Greeta
 * 
 * @date Feb 15, 2012
 * 
 * @company AIM Bangkok.
 * 
 */
public class CameraAction {

	public void setCameraCallbackAction(Context context, boolean callback) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean("camcallback", callback);
		editor.putBoolean("camtaken", false);
		editor.commit();
	}

	public void setCameraTakenAction(Context context, boolean taken) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		Editor editor = prefs.edit();
		editor.putBoolean("camtaken", taken);
		editor.commit();
	}

	public boolean readCallbackState(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("camcallback", false);
	}

	public boolean readTakenState(Context context) {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		return prefs.getBoolean("camtaken", false);
	}
}
