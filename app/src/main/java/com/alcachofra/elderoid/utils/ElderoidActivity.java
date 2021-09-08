package com.alcachofra.elderoid.utils;

import android.os.Bundle;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.alcachofra.elderoid.Elderoid;

/**
 * Template activity for Elderoid
 */
public class ElderoidActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Elderoid.setLanguage(this, Elderoid.getLanguage());

        // Initialise activity in fullscreen:
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }
}
