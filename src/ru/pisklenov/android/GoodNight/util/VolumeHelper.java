package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import ru.pisklenov.android.GoodNight.GN;

/**
 * Created by dns on 05.10.13.
 */
public class VolumeHelper {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    public static final int MEDIUM_VOLUME_PERCENT = 50;
    public static final int LOW_VOLUME_PERCENT = 20;


    public static void setMediumVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round((float)maxVolume * ((float)MEDIUM_VOLUME_PERCENT / (float)100)), 0);

        //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //if (DEBUG) Log.w(TAG, "currentVolume " + currentVolume);
    }

    public static void setLowVolume(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (int) Math.round((float)maxVolume * ((float)LOW_VOLUME_PERCENT / (float)100)), 0);

        //int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //if (DEBUG) Log.w(TAG, "currentVolume " + currentVolume);
    }

    public static int getCurrentVolumeInPercent(Context context) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //if (DEBUG) Log.w(TAG, "maxVolume " + maxVolume);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        //if (DEBUG) Log.w(TAG, "currentVolume " + currentVolume);

        return Math.round(((float)currentVolume / (float)maxVolume) * (float)100);
    }
}
