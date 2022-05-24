package org.horaapps.soundrecorder.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.FrameLayout;

import org.horaapps.soundrecorder.R;
import org.horaapps.soundrecorder.fragments.SettingFragment;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        getSupportActionBar().setTitle("Settings");

        if (findViewById(R.id.setting_container) != null) {
            if (savedInstanceState != null) {
                return;
            }
            // below line is to inflate our fragment.
            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.setting_container,new SettingFragment())
                    .commit();
        }
    }
}