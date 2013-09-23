package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by anpi0413 on 23.09.13.
 */
public class PhoneModeHelper {
    private Context context;
    public static final int MODE_NORMAL = AudioManager.RINGER_MODE_NORMAL;
    public static final int MODE_SILENT = AudioManager.RINGER_MODE_SILENT;
    public static final int MODE_VIBRATE = AudioManager.RINGER_MODE_VIBRATE;


    public PhoneModeHelper(Context context) {
        this.context = context;
    }

    public int getCurrentMode() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        return audioManager.getRingerMode();
    }

    public void setMode(int mode) {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(mode);
    }

    public void setModeNormal() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(MODE_NORMAL);
    }

    public void setModeSilent() {
        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        audioManager.setRingerMode(MODE_SILENT);
    }
}
