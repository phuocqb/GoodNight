package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by anpi0413 on 17.09.13.
 */
public class PreferencesHelper {
    public static final String PREFS_NAME = PreferencesHelper.class.getName();
    static final boolean DEFAULT_BOOLEAN = false;
    static final String DEFAULT_STRING = "";

    Context context;

    public PreferencesHelper(Context context) {
        this.context = context;
    }

    public boolean getBoolean(String key) {
        return getBoolean(key, DEFAULT_BOOLEAN);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getBoolean(key, defaultValue);
    }

    public String getString(String key) {
        return getString(key, DEFAULT_STRING);
    }

    public String getString(String key, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(key, defaultValue);
    }

    public void setBoolean(String key, boolean value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(key, value);

        // Commit the edits!
        editor.commit();
    }

    public void setString(String key, String value) {
        SharedPreferences settings = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(key, value);

        // Commit the edits!
        editor.commit();
    }
}
