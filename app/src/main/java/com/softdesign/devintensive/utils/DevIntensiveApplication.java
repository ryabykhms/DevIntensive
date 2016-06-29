package com.softdesign.devintensive.utils;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * @author ryabykh_ms
 */
public class DevIntensiveApplication extends Application {

    public static SharedPreferences sSharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();
        sSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    public static SharedPreferences getSharedPreferences() {
        return sSharedPreferences;
    }
}
