package com.softdesign.devintensive.utils;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.EditText;

import com.softdesign.devintensive.R;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Валидатор полей EditText при редактировании профиля пользователя
 * @author ryabykh_ms
 */
public class EditTextWatcher implements TextWatcher {

    private static final String PHONE_MASK = "+X XXX XXX-XX-XX";
    private static final String EMAIL_MASK = "XXX@XX.XX";
    private static final String VK_MASK = "vk.com/XXX";
    private static final String GIT_MASK = "github.com/XXX";
    private static final String PHONE_PATTERN = "\\+[0-9](\\s[0-9]{3}){2}(\\-[0-9]{2}){2}[0-9]{0,9}";
    private static final String EMAIL_PATTERN = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{3,256}" +
            "\\@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{2,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{2,25}" +
            ")+";
    private static final String VK_PATTERN = "vk\\.com\\/[a-zA-Z0-9]{3,50}";
    private static final String GIT_PATTERN = "github\\.com\\/[a-zA-Z0-9]{3,50}";
    public static final String BLANK_STRING = "";

    private EditText mInputField;

    public EditTextWatcher() {
    }

    public EditTextWatcher(EditText inputField) {
        mInputField = inputField;
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    }

    @Override
    public void afterTextChanged(Editable editable) {
        switch (mInputField.getId()) {
            case R.id.phone_et:
                validate(R.id.phone_et);
                break;

            case R.id.email_et:
                validate(R.id.email_et);
                break;

            case R.id.vk_et:
                validate(R.id.vk_et);
                break;

            case R.id.git_et:
                validate(R.id.git_et);
                break;
        }
    }

    /**
     * Проверяем поля.
     * @param resId - идентификатор ресурса поля, которое необходимо проверить
     * @return - true - валидация прошла успешно, false - найдены ошибки
     */
    private boolean validate(int resId) {
        boolean valid = true;

        String textError = BLANK_STRING;
        String inputString = mInputField.getText().toString().trim();

        if(resId != R.id.about_et && inputString.isEmpty()) {
            textError = "Пусто!";
        } else {
            switch (resId) {
                case R.id.phone_et:
                    textError = !isValidPhone(inputString) ? "Формат номера: " + PHONE_MASK : BLANK_STRING;
                    break;

                case R.id.email_et:
                    textError = !isValidEmail(inputString) ? "Формат email-адреса: " + EMAIL_MASK : BLANK_STRING;
                    break;

                case R.id.vk_et:
                    textError = !isValidVk(inputString) ? "Формат vk-адреса: " + VK_MASK : BLANK_STRING;
                    break;

                case R.id.git_et:
                    textError = !isValidGit(inputString) ? "Формат git-адреса: " + GIT_MASK : BLANK_STRING;
                    break;
            }
        }

        if(!textError.equals(BLANK_STRING)) {
            mInputField.setError(textError);
            mInputField.requestFocus();
            valid = false;
        } else {
            mInputField.setError(null);
        }

        return valid;
    }

    /**
     * Проверяем поле телефона на пустоту и соответствие формату
     * @param phone - номер телефона
     * @return - true - поле не пустое и соответствует формату, иначе false
     */
    public boolean isValidPhone(String phone) {

        return !TextUtils.isEmpty(phone) && Pattern.compile(PHONE_PATTERN).matcher(phone).matches();
    }

    /**
     * Проверяем поле email на пустоту и соответствие формату
     * @param email - email-адрес
     * @return - true - поле не пустое и соответствует формату, иначе false
     */
    public boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && Pattern.compile(EMAIL_PATTERN).matcher(email).matches();
    }

    /**
     * Проверяем поле vk на пустоту и соответствие формату
     * @param vk - vk-адрес
     * @return - true - поле не пустое и соответствует формату, иначе false
     */
    public boolean isValidVk(String vk) {
        return !TextUtils.isEmpty(vk) && Pattern.compile(VK_PATTERN).matcher(vk).matches();
    }

    /**
     * Проверяем поле git на пустоту и соответствие формату
     * @param git - git-адрес
     * @return - true - поле не пустое и соответствует формату, иначе false
     */
    public boolean isValidGit(String git) {
        return !TextUtils.isEmpty(git) && Pattern.compile(GIT_PATTERN).matcher(git).matches();
    }

    /**
     * Проверяет все необходимые поля сразу на пустоту и соответствие формату
     * @param fields - необходимые поля для проверки
     * @return - true - поля не пустые и соответствуют формату, иначе false
     */
    public boolean isValidAllFields(List<EditText> fields) {
        for (EditText field: fields) {
            mInputField = field;
            if(!validate(field.getId()))
                return false;
        }
        return true;
    }
}
