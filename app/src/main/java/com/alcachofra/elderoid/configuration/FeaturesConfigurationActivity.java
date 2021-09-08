package com.alcachofra.elderoid.configuration;

import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.SettingsActivity;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class FeaturesConfigurationActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_features_configuration);



        AppCompatButton proceed = findViewById(R.id.proceed);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(String.format(Elderoid.string(R.string.config_features_1), SimplePrefs.getString(Elderoid.USERNAME)), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_features_2), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_features_3), R.drawable.netie_happy)
                ),
                false,
                v -> {
                    if (SimplePrefs.getInt(Elderoid.CONFIG_STAGE, 0) >= 4) startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    else {
                        SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 0);
                        startActivity(new Intent(getApplicationContext(), UserConfigurationActivity.class));
                    }
                }
        );

        proceed.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), CallsConfigurationActivity.class)));
    }
}