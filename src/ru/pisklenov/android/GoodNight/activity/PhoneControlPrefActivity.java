package ru.pisklenov.android.GoodNight.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import ru.pisklenov.android.GoodNight.R;

/**
 * Created by dns on 15.09.13.
 */
public class PhoneControlPrefActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.phone_control_pref);

        // Get the custom preference
        /*Preference customPref = (Preference) findPreference("customPref");
        customPref
                .setOnPreferenceClickListener(new OnPreferenceClickListener() {

                    public boolean onPreferenceClick(Preference preference) {
                        Toast.makeText(getBaseContext(),
                                "The custom preference has been clicked",
                                Toast.LENGTH_LONG).show();
                        SharedPreferences customSharedPreference = getSharedPreferences(
                                "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = customSharedPreference
                                .edit();
                        editor.putString("myCustomPref",
                                "The preference has been clicked");
                        editor.commit();
                        return true;
                    }

                });*/
    }
}
