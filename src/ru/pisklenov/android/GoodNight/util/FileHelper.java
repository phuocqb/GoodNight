package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

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

    public static ArrayList<String> getLinesFromFile(File file) {
        ArrayList<String> list = new ArrayList<String>();
        try {
            Scanner s = new Scanner(file);
            while (s.hasNext()){
                list.add(s.next());
            }
            s.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

         return list;
    }

    public static boolean isFileExists(String fileName) {
        try {
            File file = new File(fileName);
            return file.exists();
        } catch (Exception e) {
            return false;
        }
    }

    public static File getAvailablePath(Context context) {
        String result;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            result = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + context.getPackageName();
            new File(result).mkdirs();
        } else {
            result = context.getFilesDir().getAbsolutePath();
        }

        return new File(result);
    }
}
