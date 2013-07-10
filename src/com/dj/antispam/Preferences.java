package com.dj.antispam;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * User: dzherebjatjew@thumbtack.net
 * Date: 7/9/13
 */
public class Preferences {
	public static final String ANTISPAM_IMPORT = "antispamImport";
	public static final String LAST_VIEWED = "lastViewed";
	private final SharedPreferences prefs;

	public Preferences(Context context) {
		prefs = context.getSharedPreferences(ANTISPAM_IMPORT, Context.MODE_PRIVATE);
	}

	public boolean showImportActivity() {
		try {
			return prefs.getBoolean(ANTISPAM_IMPORT, true);
		} catch (Exception e) {
			return true;
		}
	}

	public void setShowImportActivity(boolean show) {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(ANTISPAM_IMPORT, show);
		editor.commit();
	}

	public long getLastViewedTime() {
		return prefs.getLong(LAST_VIEWED, System.currentTimeMillis());
	}

	public void updateLastViewedTime() {
		SharedPreferences.Editor editor = prefs.edit();
		editor.putLong(LAST_VIEWED, System.currentTimeMillis());
		editor.commit();
	}
}
