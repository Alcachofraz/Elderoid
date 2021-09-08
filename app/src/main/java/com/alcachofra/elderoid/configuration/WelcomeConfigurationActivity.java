package com.alcachofra.elderoid.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;
import java.util.Collections;

public class WelcomeConfigurationActivity extends ElderoidActivity {

    Netie netie;

    boolean tmp = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_configuration);

        AppCompatButton proceed = findViewById(R.id.proceed);
        AppCompatImageView dashed_arrow = findViewById(R.id.dashed_arrow);

        dashed_arrow.setVisibility(View.GONE);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.config_welcome_1), R.drawable.netie)
                ),
                false,
                v -> startActivity(new Intent(getApplicationContext(), LangConfigurationActivity.class))
        );

        proceed.setOnClickListener(v -> {
            if (tmp) {
                Intent intent = new Intent(getApplicationContext(), UserConfigurationActivity.class);
                startActivity(intent);
            }
            else {
                netie.resetCuePool(Arrays.asList(
                        new Cue(
                                Elderoid.string(R.string.config_welcome_2),
                                R.drawable.netie_happy
                        ),
                        new Cue(
                                Elderoid.string(R.string.config_welcome_3),
                                R.drawable.netie
                        ),
                        new Cue(
                                Elderoid.string(R.string.config_welcome_4),
                                R.drawable.netie_confused
                        ),
                        new Cue(
                                Elderoid.string(R.string.config_welcome_5),
                                R.drawable.netie_concerned
                        ),
                        new Cue(
                                Elderoid.string(R.string.config_welcome_6),
                                R.drawable.netie_happy
                        )
                ), false);
                netie.firstCue();
                proceed.setText(R.string.dialog_got_it);
                dashed_arrow.setVisibility(View.VISIBLE);
                tmp = true;
            }
        });
    }
}
