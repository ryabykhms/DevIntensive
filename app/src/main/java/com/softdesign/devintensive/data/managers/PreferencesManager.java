package com.softdesign.devintensive.data.managers;

import android.content.SharedPreferences;
import android.net.Uri;

import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.DevIntensiveApplication;

import java.util.ArrayList;
import java.util.List;

/**
 * Отвечает за работу с пользовательскими данными. Инкапсулирует логику, которая связана с
 * работой SharedPreferences.
 * @author ryabykh_ms
 */
public class PreferencesManager {

    private SharedPreferences mSharedPreferences;

    private static final String[] USER_FIELDS = {
            ConstantManager.USER_PHONE_KEY,
            ConstantManager.USER_MAIL_KEY,
            ConstantManager.USER_VK_KEY,
            ConstantManager.USER_GIT_KEY,
            ConstantManager.USER_BIO_KEY
    };

    public PreferencesManager() {
        mSharedPreferences = DevIntensiveApplication.getSharedPreferences();
    }

    /**
     * Сохранение пользовательских данных
     * @param userFields - список значений пользовательских полей
     */
    public void saveUserProfileData(List<String> userFields) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        for (int i = 0; i < USER_FIELDS.length; i++) {
            editor.putString(USER_FIELDS[i], userFields.get(i));
        }
        editor.apply();
    }

    /**
     * Загрузка пользовательских данных из SharedPreferences
     * @return - список заполненных из SharedPreferences полей
     */
    public List<String> loadUserProfileData() {
        List<String> userFields = new ArrayList<>();
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_PHONE_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_MAIL_KEY, ""));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_VK_KEY, "vk.com/"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_GIT_KEY, "git.com/"));
        userFields.add(mSharedPreferences.getString(ConstantManager.USER_BIO_KEY, ""));
        return userFields;
    }

    /**
     * Сохранения изображения профиля пользователя
     * @param uri - URI-адрес изображения
     */
    public void saveUserPhoto(Uri uri) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(ConstantManager.USER_PHOTO_KEY, uri.toString());
        editor.apply();
    }

    /**
     * Загрузка изображения из SharedPreferences
     * @return - URI-адрес изображения
     */
    public Uri loadUserPhoto() {
        return Uri.parse(mSharedPreferences.getString(ConstantManager.USER_PHOTO_KEY, "android:resource//com.softdesign.devintensive/drawable/user_bg"));
    }
}
