package ru.pisklenov.android.GoodNight.util;

import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ru.pisklenov.android.GoodNight.GN;
import ru.pisklenov.android.GoodNight.activity.MainActivity;

/**
 * Created by anpi0413 on 27.09.13.
 */
public class MD5Helper {
    private static final boolean DEBUG = GN.DEBUG;
    private static final String TAG = GN.TAG;

    public static boolean checkMD5(String md5, File updateFile) {
        if (md5 == null || md5.equals("") || updateFile == null) {
            if (DEBUG) Log.e(TAG, "MD5 String NULL or UpdateFile NULL");
            return false;
        }

        String calculatedDigest = calculateMD5(updateFile);
        if (calculatedDigest == null) {
            if (DEBUG) Log.e(TAG, "calculatedDigest NULL");
            return false;
        }

        if (DEBUG) Log.i(TAG, "Calculated digest: " + calculatedDigest);
        if (DEBUG) Log.i(TAG, "Provided digest: " + md5);

        return calculatedDigest.equalsIgnoreCase(md5);
    }

    public static String calculateMD5(File updateFile) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            if (DEBUG) Log.e(TAG, "Exception while getting Digest", e);
            return null;
        }

        InputStream is;
        try {
            is = new FileInputStream(updateFile);
        } catch (FileNotFoundException e) {
            if (DEBUG) Log.e(TAG, "Exception while getting FileInputStream", e);
            return null;
        }

        byte[] buffer = new byte[8192];
        int read;
        try {
            while ((read = is.read(buffer)) > 0) {
                digest.update(buffer, 0, read);
            }
            byte[] md5sum = digest.digest();
            BigInteger bigInt = new BigInteger(1, md5sum);
            String output = bigInt.toString(16);
            // Fill to 32 chars
            output = String.format("%32s", output).replace(' ', '0');
            return output;
        } catch (IOException e) {
            throw new RuntimeException("Unable to process file for MD5", e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                if (DEBUG) Log.e(TAG, "Exception on closing MD5 input stream", e);
            }
        }
    }
}
