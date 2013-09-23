package ru.pisklenov.android.GoodNight;

import android.app.Application;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

/**
 * Created by anpi0413 on 09.09.13.
 */
@ReportsCrashes(formKey = "YOUR_FORM_KEY")
public class GN extends Application {
    public static final String TAG = "GoodNight";
    public static final boolean DEBUG = true;

    @Override
    public void onCreate() {
        ACRA.init(this);
        super.onCreate();
    }
}
