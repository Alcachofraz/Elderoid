package com.alcachofra.elderoid;

import android.os.Bundle;

import com.alcachofra.elderoid.utils.ElderoidActivity;

public class FakeLauncherActivity extends ElderoidActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fake_launcher);


    }
}