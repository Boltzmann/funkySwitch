package com.grunzphi.funkySwitch;

import com.grunzphi.R;

import android.app.Activity;
import android.os.Bundle;
import android.preference.PreferenceManager;

public class UserSettingActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PreferenceManager.setDefaultValues(this, R.xml.settings, false);
        
        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }
}

