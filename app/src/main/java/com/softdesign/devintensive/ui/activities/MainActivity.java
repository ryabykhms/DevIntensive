package com.softdesign.devintensive.ui.activities;


import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.softdesign.devintensive.R;
import com.softdesign.devintensive.data.managers.DataManager;
import com.softdesign.devintensive.data.network.res.UserModelRes;
import com.softdesign.devintensive.ui.fragments.LoadPhotoDialogFragment;
import com.softdesign.devintensive.utils.AppConfig;
import com.softdesign.devintensive.utils.ConstantManager;
import com.softdesign.devintensive.utils.EditTextWatcher;
import com.softdesign.devintensive.utils.ImageSizeTransformation;
import com.softdesign.devintensive.utils.RoundedImageTransformation;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.ButterKnife;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Главное активити приложения
 * @author ryabykh_ms
 */
public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = ConstantManager.TAG_PREFIX + "Main Activity";

    private boolean mCurrentEditMode;

    private DataManager mDataManager;

    @BindView(R.id.call_img)
    ImageView mCallImg;
    @BindView(R.id.email_img)
    ImageView mEmailImg;
    @BindView(R.id.vk_img)
    ImageView mVkImg;
    @BindView(R.id.git_img)
    ImageView mGitImg;

    @BindView(R.id.coordinator_container)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.navigation_drawer)
    DrawerLayout mNavigationDrawer;
    @BindView(R.id.fab)
    FloatingActionButton mFab;
    @BindView(R.id.profile_placeholder)
    RelativeLayout mProfilePlaceholder;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbar;
    @BindView(R.id.appbar_layout)
    AppBarLayout mAppBarLayout;
    @BindView(R.id.user_photo_img)
    ImageView mProfileImage;

    @BindView(R.id.email_til)
    TextInputLayout mEmailInputLayout;

    @BindViews({R.id.phone_et, R.id.email_et, R.id.vk_et, R.id.git_et, R.id.about_et})
    List<EditText> mUserInfo;

    //private TextView mUserValueRaiting, mUserValueCodeLines, mUserValueProjects;

    @BindViews({R.id.user_info_rait_txt, R.id.user_info_code_lines_txt, R.id.user_info_project_txt})
    List<TextView> mUserValueViews;

    private String mUserFullName;

    private boolean isProfileImageChange;

    private LoadPhotoDialogFragment mDialogFragment;

    private AppBarLayout.LayoutParams mAppBarParams;
    private Uri mSelectedImage;
    private EditTextWatcher mTextWatcher;

    /**
     * Метод вызывается при создании активити (после изменения конфигурации/возврата к текущей
     * активити после его уничтожения)
     *
     * В данном методе инициализируются/производятся:
     * - UI пользовательский интерфейс (статика)
     * - инициализация статических данных активити
     * - связь данных со списками (инициализация адаптеров)
     *
     * Не запускать длительные операции по работе с данными в onCreate()!!!
     * @param savedInstanceState - объект со значениями, сохранёнными в Bundle - состояние UI
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, getString(R.string.log_callback_oncreate));
        ButterKnife.bind(this);
        //mUserValueRaiting = (TextView) findViewById()
        mDataManager = DataManager.getInstance();
        isProfileImageChange = false;

        mDialogFragment = new LoadPhotoDialogFragment();
        mAppBarParams = null;

        mCallImg.setOnClickListener(this);
        mEmailImg.setOnClickListener(this);
        mVkImg.setOnClickListener(this);
        mGitImg.setOnClickListener(this);
        mFab.setOnClickListener(this);
        mProfilePlaceholder.setOnClickListener(this);

        mSelectedImage = mDataManager.getPreferencesManager().loadUserPhoto();

        initUserFullName();
        setupToolbar();
        setupDrawer();
        initUserFullName();
        initUserInfoValue();
        initUserFields();

        Picasso.with(this)
                .load(mSelectedImage)
                .placeholder(R.drawable.user_bg)
                .transform(new ImageSizeTransformation())
                .fit()
                .centerCrop()
                .into(mProfileImage);

        if (savedInstanceState != null) {
            mCurrentEditMode = savedInstanceState.getBoolean(ConstantManager.EDIT_MODE_KEY, false);
            changeEditMode(mCurrentEditMode);
        }

        mTextWatcher = new EditTextWatcher();
        for (int i = 0; i < mUserInfo.size()-1; i++) {
            mUserInfo.get(i).addTextChangedListener(new EditTextWatcher(mUserInfo.get(i)));
        }

    }

    /**
     * Обрабатывет нажатие на пункт меню
     * @param item - пункт меню, на который нажал пользователь
     * @return - {@link android.app.Activity#onOptionsItemSelected}
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            mNavigationDrawer.openDrawer(GravityCompat.START);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Метод вызывается при старте активити перед моментом того, как UI станет доступен пользователю.
     * Как правило, в данном методе происходит регистрация подписки на события, остановка которых
     * была произведена в onStop()
     */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, getString(R.string.log_callback_onstart));
    }

    /**
     * Метод вызывается, когда активити становится доступна пользователю для взаимодействия.
     * В данном методе, как правило, происходит запуск анимаций/аудио/видео/запуск BroadcastReceiver,
     * необходимых для реализации UI логики/запуск выполнения потоков и т.д.
     * Метод должен быть максимально легковесным для максимальной отзывчивости UI
     */
    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, getString(R.string.log_callback_onresume));
    }

    /**
     * Метод вызывается, когда текущая активити теряет фокус, но остаётся видимой (всплытие
     * диалогового окна/частичное перекрытие другой активити и т.д.
     * В данном методе реализуют сохранение легковесных UI данных/анимаций/аудио/видео и т.д.
     */
    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, getString(R.string.log_callback_onpause));
    }

    /**
     * Метод вызывается, когда активити становится невидимой для пользователя.
     * В данном методе происходит отписка от событий, остановка сложных анимаций, сложные операции
     * по сохранению данных/прерывание запущенных потоков и т.д.
     */
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, getString(R.string.log_callback_onstop));
        if(mTextWatcher.isValidAllFields(mUserInfo)) {
            saveUserInfoValues();
        }

    }

    /**
     * Метод вызывается при окончании работы активити (когда это происходит системно или после
     * вызова метода finish()
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, getString(R.string.log_callback_ondestroy));
    }

    /**
     * Метод вызывается при рестарте активити/возобновлении работы после вызова метода onStop().
     * В данном методе реализуется специфическая бизнес-логика, которая должна быть реализована
     * именно при рестарте активити - например запросы к серверу, которые необходимо вызывать
     * при возвращении из другой активити (обновление данных, подписка на определённое событие,
     * проинициализированное на другом экране/специфическа бизнес-логика, завязанная именно
     * на перезапуске активити
     */
    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, getString(R.string.log_callback_onrestart));
    }

    /**
     * Сохранения состоянния пользователя, а именно режима редактирования, чтобы при повороте
     * экрана или свёртывания приложения, режим редактирования и введённые данные сохранялись
     * @param outState - объект со значениями, сохранёнными в Bundle - состояние UI
     *                 (режим редактирования
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(ConstantManager.EDIT_MODE_KEY, mCurrentEditMode);
    }

    /**
     * Обработка нажатия на системную кнопку "Назад". Если боковое меню было открыто, то при нажатии
     * кнопки "Назад", закрываем его
     */
    @Override
    public void onBackPressed() {
        if (mNavigationDrawer.isDrawerOpen(GravityCompat.START)) {
            mNavigationDrawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Обработчик нажатия на view-элементы. Происходит вызов соответствующих интентов для
     * каждого пункта информации о пользователе, обработка нажатия floatingActionButton, а также
     * загрузка фотографии в профиль пользователя с помощью {@link LoadPhotoDialogFragment}
     * @param view - элемент интерфейса, на который нажал пользователь
     */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.call_img:
                actionDialIntent();
                break;

            case R.id.email_img:
                actionSendMailIntent();
                break;

            case R.id.vk_img:
                actionViewVkIntent();
                break;

            case R.id.git_img:
                actionViewGitIntent();
                break;

            case R.id.fab:
                fabClick();
                break;

            case R.id.profile_placeholder:
                mDialogFragment.show(getFragmentManager(), "Load Image");
                break;
        }
    }

    /**
     * Вызов активити, позволяющей совершать звонки. В неё передаётся номер телефона, который
     * введён в соответствующем поле информации о пользователе
     */
    private void actionDialIntent() {
        if(mTextWatcher.isValidPhone(mUserInfo.get(0).getText().toString())) {
            Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" +
                    mUserInfo.get(0).getText().toString()));
            startActivity(dialIntent);
        } else {
            showSnackbar(getString(R.string.user_profile_invalidate_number));
        }
    }

    /**
     * Вызов активити, позволяющей совершать отправку email-сообщений. В неё передаётся адрес
     * электронной почты, который введён в соответствующем поле информации о пользователе
     */
    private void actionSendMailIntent() {
        if(mTextWatcher.isValidEmail(mUserInfo.get(1).getText().toString())) {
            Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" +
                    mUserInfo.get(1).getText().toString()));
            startActivity(Intent.createChooser(mailIntent, getString(R.string.mail_intent_chooser_message)));
        } else {
            showSnackbar(getString(R.string.user_profile_invalidate_email));
        }
    }

    /**
     * Вызов активити, позволяющей открывать профиль пользователя на vk.com (например браузеры).
     * В неё передаётся адрес профиля пользователя vk.com, который введён в соответствующем поле
     * информации о пользователе
     */
    private void actionViewVkIntent() {
        if(mTextWatcher.isValidVk(mUserInfo.get(2).getText().toString())) {
            Intent vkIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" +
                    mUserInfo.get(2).getText().toString()));
            startActivity(vkIntent);
        } else {
            showSnackbar(getString(R.string.user_profile_invalidate_email));
        }
    }

    /**
     * Вызов активити, позволяющей открывать профиль пользователя на git.com (например браузеры).
     * В неё передаётся адрес профиля пользователя git.com, который введён в соответствующем поле
     * информации о пользователе
     */
    private void actionViewGitIntent() {
        if(mTextWatcher.isValidGit(mUserInfo.get(3).getText().toString())) {
            Intent gitIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://" +
                    mUserInfo.get(3).getText().toString()));
            startActivity(gitIntent);
        } else {
            showSnackbar(getString(R.string.user_profile_invalidate_email));
        }
    }

    /**
     * Обработка нажатия на кнопку FloatingActionButton. Если режим редактирования был выключен
     * (false), то необходимо войти в режим редактирования, вызвав {@link #changeEditMode(boolean)}
     * с параметром true и установки текущего режима редактирования в true.
     * Если перед нажатием на кнопку режим редактирования был активен, то проверяем введённые
     * в поля данные, с помощью {@link EditTextWatcher#isValidAllFields(List)}
     * Если поля заполнены верно, то выходим из режима редактирования.
     * Если поля заполнены неверно, то сворачиваем клавиатуру и выводим Snackbar с сообщением
     * "Неверный ввод" и предлагаем выйти из режима редактирования без сохранения параметров.
     */
    private void fabClick() {
        if (!mCurrentEditMode) {
            changeEditMode(true);
            mCurrentEditMode = true;
        } else {
            hideErrorMessagesUserInfo();
            if (mTextWatcher.isValidAllFields(mUserInfo)) {
                changeEditMode(false);
                mCurrentEditMode = false;
            } else {
                //закрытие клавиатуры
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mFab.getWindowToken(),
                        InputMethodManager.HIDE_NOT_ALWAYS);

                Snackbar.make(mCoordinatorLayout, R.string.invalid_input, Snackbar.LENGTH_LONG)
                        .setAction(R.string.cancel_input, new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                initUserFields();
                                changeEditMode(false);
                                mCurrentEditMode = false;
                            }
                        }).show();
            }
        }
    }

    /**
     * Убираем сообщение об ошибке со всех полей после выхода из режима редактирования при обработке
     * FloatingActionButton {@link #fabClick()}
     */
    private void hideErrorMessagesUserInfo() {
        for(EditText field : mUserInfo) {
            field.setError(null);
        }
    }

    /**
     * Показывает Snackbar
     * @param message - сообщение, которое должен вывести Snackbar
     */
    private void showSnackbar(String message) {
        Snackbar.make(mCoordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Настраиваем toolbar. Заменяем начальный actionBar нашим toolbar'ом.
     */
    private void setupToolbar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();

        mAppBarParams = (AppBarLayout.LayoutParams) mCollapsingToolbar.getLayoutParams();
        if (actionBar != null) {
            actionBar.setTitle(mUserFullName);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_24dp);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Настраиваем DrawerLayout. Получаем шапку DrawerLayout'а, загружаем с помощью
     * {@link Picasso} в шапку фотографию, и делаем её круглой с помощью
     * {@link RoundedImageTransformation}, а также обрабатываем нажатия на пункты меню
     */
    private void setupDrawer() {
        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        View headerView = navigationView != null ? navigationView.getHeaderView(0) : null;
        if(headerView != null) {
            ((TextView)headerView.findViewById(R.id.user_name_txt)).setText(mUserFullName);
            ImageView drawerPhoto = (ImageView) headerView.findViewById(R.id.user_photo_drawer_img);
            Picasso.with(this)
                    .load(mSelectedImage)
                    .placeholder(R.drawable.user_bg)
                    .transform(new RoundedImageTransformation())
                    .fit()
                    .centerCrop()
                    .into(drawerPhoto);
            navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem item) {
                    showSnackbar(item.getTitle().toString());
                    item.setChecked(true);
                    mNavigationDrawer.closeDrawer(GravityCompat.START);
                    return false;
                }
            });
        } else {
            Snackbar.make(mCoordinatorLayout, R.string.error_menu_open, Snackbar.LENGTH_LONG)
                    .setAction(R.string.report_about_error, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent mailIntent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:" +
                                    getString(R.string.admin_email)));
                            startActivity(Intent.createChooser(mailIntent, getString(R.string.mail_intent_chooser_message)));
                        }
                    }).show();
        }
    }


    /**
     * Получение результата из другой Activity (фото из камеры или галлереи).
     * Результат приходит из {@link LoadPhotoDialogFragment}
     * @param requestCode -  тот же идентификатор, что и в startActivityForResult.
     *                    По нему определяем, с какого Activity пришел результат.
     * @param resultCode - код возврата. Определяет успешно прошел вызов или нет.
     * @param data - Intent, в котором возвращаются данные
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case ConstantManager.REQUEST_GALLERY_PICTURE:
                if (resultCode == RESULT_OK && data != null) {
                    mSelectedImage = data.getData();

                }
                break;
            case ConstantManager.REQUEST_CAMERA_PICTURE:
                File tmpImage = mDialogFragment.getImage();
                if (resultCode == RESULT_OK && tmpImage != null) {
                    mSelectedImage = Uri.fromFile(tmpImage);
                }
                break;
        }
    }

    /**
     * Устанавливаем значения полей с помощью бибиотеки {@link ButterKnife}
     */
    static final ButterKnife.Setter<View, Boolean> Enabled = new ButterKnife.Setter<View, Boolean>() {

        @Override
        public void set(@NonNull View view, Boolean value, int index) {
            view.setEnabled(value);
            view.setFocusable(value);
            view.setFocusableInTouchMode(value);
        }
    };

    /**
     * Переключает режимы редактирования. Если входим в режим редактирования, то:
     * - загружем значения пользовательских полей {@link #initUserFields()};
     * - меняем иконку FloatingActionButton;
     * - устанавливаем поля для редактирования в true с помощью {@link #Enabled};
     * - делаем placeholder видимым;
     * - блокируем toolbar {@link #lockToolbar()}, чтобы он не скроллился;
     * - передаём фокус на первое поле.
     * Если выходим из режима редактирования, то:
     * - сохраняем значения пользовательских полей {@link #saveUserInfoValues()};
     * - меняем иконку FloatingActionButton;
     * - устанавливаем поля для редактирования в false с помощью {@link #Enabled};
     * - делаем placeholder невидимым;
     * - разблокируем toolbar {@link #unlockToolbar()};
     * - устанавливаем режим редактирования в false.
     * @param mode если false - просмотр, если true - редактирование
     */
    private void changeEditMode(boolean mode) {
        if (mode) {
            initUserFields();
            mFab.setImageResource(R.drawable.ic_done_24dp);
            ButterKnife.apply(mUserInfo, Enabled, true);
            mProfilePlaceholder.setVisibility(View.VISIBLE);
            lockToolbar();
            mCollapsingToolbar.setExpandedTitleColor(Color.TRANSPARENT);
            mUserInfo.get(0).requestFocus();
        } else {
            saveUserInfoValues();
            mFab.setImageResource(R.drawable.ic_create_24dp);
            ButterKnife.apply(mUserInfo, Enabled, false);
            mProfilePlaceholder.setVisibility(View.GONE);
            unlockToolbar();
            mCollapsingToolbar.setExpandedTitleColor(getResources().getColor(R.color.white));
            mCurrentEditMode = false;
        }
    }


    /**
     * Загружаем данные в пользовательские поля с помощью
     * {@link com.softdesign.devintensive.data.managers.PreferencesManager}
     * и {@link DataManager}.
     */
    private void initUserFields() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileData();
        for (int i = 0; i < userData.size(); i++) {
            mUserInfo.get(i).setText(userData.get(i));
        }
    }

    private void initUserInfoValue() {
        List<String> userData = mDataManager.getPreferencesManager().loadUserProfileValues();
        for (int i = 0; i < userData.size(); i++) {
            mUserValueViews.get(i).setText(userData.get(i));
        }
    }

    private void initUserFullName() {
        String userFirstName = mDataManager.getPreferencesManager().loadUserFirstName();
        String userSecondName = mDataManager.getPreferencesManager().loadUserSecondName();
        mUserFullName = userSecondName + " " + userFirstName;
    }

    /**
     * Сохраняем данные из пользовательских полей с помощью
     * {@link com.softdesign.devintensive.data.managers.PreferencesManager}
     * и {@link DataManager}.
     */
    private void saveUserInfoValues() {
        List<String> userData = new ArrayList<>();
        for (EditText userFieldView : mUserInfo) {
            userData.add(userFieldView.getText().toString());
        }
        mDataManager.getPreferencesManager().saveUserProfileData(userData);
    }

    /**
     * Обработка результатов запросов разрешений
     * @param requestCode - значение, по которому определяется, на какой запрос разрешения
     *                    пришел ответ подобно тому как мы получаем результат от activity
     * @param permissions - содержит названия разрешений, которые необходимо было запросить
     * @param grantResults - информация о том, получены разрешения или нет
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ConstantManager.CAMERA_REQUEST_PERMISSION_CODE && grantResults.length == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                mDialogFragment.loadPhotoFromCamera();
            }
        }

        if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            mDialogFragment.loadPhotoFromGallery();
        }
    }

    /**
     * Запрещаем toolbar скроллиться
     */
    private void lockToolbar() {
        mAppBarLayout.setExpanded(true, true);
        mAppBarParams.setScrollFlags(0);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    /**
     * Разрешаем toolbar скроллиться
     */
    private void unlockToolbar() {
        mAppBarParams.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL | AppBarLayout.LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED);
        mCollapsingToolbar.setLayoutParams(mAppBarParams);
    }

    /**
     * Обновляет изображение профиля с помощью библиотеки {@link Picasso} и сохраняет его
     * с помощью {@link com.softdesign.devintensive.data.managers.PreferencesManager} и
     * {@link DataManager}
     * @param selectedImage - ссылка на изображение
     */
    private void insertProfileImage(Uri selectedImage) {
        Picasso.with(this)
                .load(mSelectedImage)
                .placeholder(R.drawable.user_bg)
                .transform(new ImageSizeTransformation())
                .fit()
                .centerCrop()
                .into(mProfileImage);
        mDataManager.getPreferencesManager().saveUserPhoto(selectedImage);
    }


}
