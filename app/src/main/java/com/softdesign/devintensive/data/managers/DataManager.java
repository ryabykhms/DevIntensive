package com.softdesign.devintensive.data.managers;

/**
 * @author ryabykh_ms
 */
public class DataManager {
    private static DataManager INSTANSE = null;
    private PreferencesManager mPreferencesManager;

    private DataManager(){
        this.mPreferencesManager = new PreferencesManager();
    }

    public static DataManager getInstance() {
        if(INSTANSE == null) {
            INSTANSE = new DataManager();
        }
        return INSTANSE;
    }

    public PreferencesManager getPreferencesManager() {
        return mPreferencesManager;
    }
}
