package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.R;

/**
 * Created by dns on 28.09.13.
 */
public class FileHelper {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    public static void copyFromResToInternal(Context context, int resID, String fileName) {
        try {
            InputStream in = context.getResources().openRawResource(resID);

            //Log.e(TAG, context.getFilesDir() + "/" + fileName);

            FileOutputStream out = new FileOutputStream(fileName);
            byte[] buff = new byte[1024];
            int read = 0;

            try {
                while ((read = in.read(buff)) > 0) {
                    out.write(buff, 0, read);
                }
            } finally {
                in.close();
                out.close();
            }
        } catch (Exception e) {
        }
    }

    public static boolean isFileExists(String fileName) {
        try {
            File file = new File(fileName);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }
}
