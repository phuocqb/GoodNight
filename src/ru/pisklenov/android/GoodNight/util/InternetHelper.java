package ru.pisklenov.android.GoodNight.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

/**
 * Created by anpi0413 on 27.09.13.
 */
public class InternetHelper {
    public static boolean isWIFIAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            Toast.makeText(context, "None Available", Toast.LENGTH_SHORT).show();
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo inf : info) {
                if (inf.getTypeName().contains("WIFI"))
                    if (inf.isConnected())
                        return true;
            }
        }
        return false;
    }
}
