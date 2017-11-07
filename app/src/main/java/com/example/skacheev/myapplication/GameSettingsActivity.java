package com.example.skacheev.myapplication;

import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

public class GameSettingsActivity extends GameActivity {
    private static final String TAG = "GameSettingsActivity";

    int boomID = -1;
    SoundPool sp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences shPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        // Получаем сохраненные настройки цвета
        String oColor = shPrefs.getString(SettingsActivity.OH_COLOR, "#64dd17");
        String bColor = shPrefs.getString(SettingsActivity.BG_COLOR, "#757575");

        super.setColors(Color.parseColor(bColor), Color.parseColor(oColor));

        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes =
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build();
            sp = new SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            sp = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
        }

        try{
            // Create objects of the 2 required classes
            AssetManager assetManager = this.getAssets();
            AssetFileDescriptor descriptor;
            // Load our fx in memory ready for use
            descriptor = assetManager.openFd("boom.ogg");
            boomID = sp.load(descriptor, 0);
            Log.e(TAG, "Successfully load sound files");
        }catch(IOException e){
            // Print an error message to the console
            Log.e(TAG, "failed to load sound files");
        }
        super.setSound(sp, boomID);
        String xBoostString = shPrefs.getString(SettingsActivity.BOOST_X_KEY, "25");
        String yBoostString = shPrefs.getString(SettingsActivity.BOOST_Y_KEY, "25");
        float xBoost = (float)25;
        float yBoost = (float)25;
        try {
            xBoost = Float.parseFloat(xBoostString);
            yBoost = Float.parseFloat(yBoostString);
        } catch (NumberFormatException ex) {
            Log.d(TAG, String.format("Failed to parse as float %s", ex));
        }
        super.setXYBoost(xBoost/(float)100, yBoost/(float)100);
    }
}
