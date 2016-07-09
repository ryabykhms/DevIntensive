package com.softdesign.devintensive.ui.fragments;


import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.DialogFragment;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.utils.ConstantManager;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Диалог для выбора метода получения изображения (камера/галлерея) и загрузки изображения
 * в профиль пользователя. Результат вызова startActivityOnResult обрабатывается в
 * {@link com.softdesign.devintensive.ui.activities.MainActivity#onActivityResult(int, int, Intent)},
 * а вызова requestPermissions в
 * {@link com.softdesign.devintensive.ui.activities.MainActivity#onRequestPermissionsResult(int, String[], int[])}
 * @author ryabykh_ms
 */
public class LoadPhotoDialogFragment extends DialogFragment implements View.OnClickListener {

    private File mImage;

    public LoadPhotoDialogFragment() {
        // Required empty public constructor
    }

    /**
     * Вызывается при создании диалога. Убираем заголовок у диалога.
     * @param savedInstanceState - объект со значениями, сохранёнными в Bundle - состояние UI
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
    }

    /**
     * Аналогично {@link android.app.Activity#onCreateView(View, String, Context, AttributeSet)}
     * Дословно "раздувает" интерфейс диалога из файлов ресурсов.
     * @param inflater - объект для установки ("раздувания") интерфейса диалога из
     *                 layout-ресурса
     * @param container - родительский объект, находящийся в layout-ресурсе диалога
     * @param savedInstanceState - объект со значениями, сохранёнными в Bundle - состояние UI
     * @return - настроенный интерфейс диалогового окна в виде View
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_load_photo_dialog, container, false);
        v.findViewById(R.id.add_photo_from_camera_ll).setOnClickListener(this);
        v.findViewById(R.id.add_photo_from_gallery_ll).setOnClickListener(this);
        mImage = null;
        return v;
    }

    /**
     * Обработчик нажатия на пункты диалога. После нажатия на иконку с камерой вызываем метод
     * {@link #loadPhotoFromCamera()} для загрузки изображения с камеры и закрываем диалог.
     * После нажатия на иконку с коллекциями (галлереей), вызываем метод
     * {@link #loadPhotoFromGallery()} для загрузки изображения из галлереи и закрываем диалог.
     * @param v - view, на которое нажал пользователь
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_photo_from_camera_ll:
                loadPhotoFromCamera();
                dismiss();
                break;

            case R.id.add_photo_from_gallery_ll:
                loadPhotoFromGallery();
                dismiss();
                break;
        }
    }

    /**
     * Загрузка изображения из галлереи. Обработка результата происходит в
     * {@link com.softdesign.devintensive.ui.activities.MainActivity#onActivityResult(int, int, Intent)}
     */
    public void loadPhotoFromGallery() {
        Intent takeGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        takeGalleryIntent.setType("image/*");
        getActivity().startActivityForResult(takeGalleryIntent, ConstantManager.REQUEST_GALLERY_PICTURE);

    }

    /**
     * Загрузка изображения из камеры. бработка результата происходит в
     * {@link com.softdesign.devintensive.ui.activities.MainActivity#onActivityResult(int, int, Intent)}
     * Также проверяется, дал ли пользователь разрешения на использование камеры и записи во внешнюю
     * память (sd карту). Если есть, то создаём файл с помощью {@link #createImageFile()}
     * и посылаем в MainActivity на обработку.
     * Если разрешения не были получены, то снова запрашиваем разрешения.
     */
    public void loadPhotoFromCamera() {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent takeCaptureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException e) {
                e.printStackTrace();
                Snackbar.make(getActivity().findViewById(R.id.coordinator_container), R.string.can_not_create_file_err, Snackbar.LENGTH_LONG);
            }
            if (photoFile != null) {
                takeCaptureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                getActivity().startActivityForResult(takeCaptureIntent, ConstantManager.REQUEST_CAMERA_PICTURE);
            }
            mImage = photoFile;
        } else {
            ActivityCompat.requestPermissions(getActivity(), new String[]{
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, ConstantManager.CAMERA_REQUEST_PERMISSION_CODE);
            Snackbar.make(getActivity().findViewById(R.id.coordinator_container), R.string.please_get_permissions_msg, Snackbar.LENGTH_LONG)
                    .setAction(R.string.permit_msg, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            openApplicationSettings();
                        }
                    }).show();
        }
    }

    /**
     * Создаём файл изображения. Для этого указываем формат, приписываем к названию префикс
     * JPEG и текущее время. Находим директорию, где хронятся изображения и создаём файл.
     * @return - созданный файл для записи изображения
     * @throws IOException - исключение ввода вывода при создании файла
     */
    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        File image = File.createTempFile(imageFileName, ".jpg", storageDir);

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
        values.put(MediaStore.MediaColumns.DATA, image.getAbsolutePath());

        getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        return image;
    }

    /**
     * Открываем насройки для установки пользователем необходимых разрешений
     */
    public void openApplicationSettings() {
        Intent appSettingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getActivity().getPackageName()));
        getActivity().startActivityForResult(appSettingsIntent, ConstantManager.PERMISSION_REQUEST_SETTINGS_CODE);
    }

    /**
     * Возвращаем файл изображения
     * @return - файл изображения
     */
    public File getImage() {
        return mImage;
    }
}
