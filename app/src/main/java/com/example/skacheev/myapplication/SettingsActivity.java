package com.example.skacheev.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String BOOST_X_KEY = "boost_x_key";
    public static final String BOOST_Y_KEY = "boost_y_key";
    public static final String BG_COLOR = "background_color_key";
    public static final String OH_COLOR = "octahedron_color_key";
    private static final String TAG = "SettingsActivity";
    static Context appContext;

    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        String value = preferences.getString(key, "");
        Preference pref = findPreference(key);

        Log.d(TAG, String.format("Got new value %s for preference %s", value, pref));
        if (key.equals(BG_COLOR) || key.equals(OH_COLOR)) {
            // Сохраняем выбранные цвета, key содержит id нужного цвета
            ListPreference listPreference = (ListPreference) pref;
            int index = listPreference.findIndexOfValue(value);
            pref.setSummary(index >= 0 ? listPreference.getEntries()[index]: null);
        } else {
            int intValue = -1;
            try {
                intValue = Integer.parseInt(value);

            } catch (NumberFormatException ex) {
                Log.d(TAG, String.format("Failed to parse %s as integer", value));
            }
            if (intValue < 1 || intValue > 100) {
                // Допустимые значения для ускорения в диапазоне от 1 до 100
                // Если пользователь ввел другое значение, то показываем подсказку
                // и игнорируем ввод.
                Log.d(TAG, String.format("Failed to set preference %s to %s", pref, value));
                Toast.makeText(
                        appContext,
                        "Choose something between 1 and 100",
                        Toast.LENGTH_LONG
                ).show();
                return;
            }
            pref.setSummary(value);
        }
        Log.d(TAG, String.format("Set preference %s to %s", pref, value));
    };

    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        appContext = getApplicationContext();
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
