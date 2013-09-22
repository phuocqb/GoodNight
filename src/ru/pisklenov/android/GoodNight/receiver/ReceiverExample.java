package ru.pisklenov.android.GoodNight.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import ru.pisklenov.android.GoodNight.GN;

/**
 * Created by anpi0413 on 09.09.13.
 */
public class ReceiverExample extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(GN.TAG, "ReceiverExample.onReceive");
        /*Log.i(GN.TAG, "getAction " + intent.getAction());
        Log.i(GN.TAG, "getDataString " + intent.getDataString());
        Log.i(GN.TAG, "getPackage " + intent.getPackage());
        Log.i(GN.TAG, "getScheme " + intent.getScheme());
        Log.i(GN.TAG, "getType " + intent.getType());*/
    }
}
