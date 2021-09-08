package com.alcachofra.elderoid.configuration;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.SimplePrefs;
import com.alcachofra.elderoid.utils.dialog.DialogTempScale;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;

public class WeatherConfigurationActivity extends ElderoidActivity {
    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_configuration);



        ConstraintLayout reject_accept = findViewById(R.id.reject_accept);
        View accept = reject_accept.findViewById(R.id.accept_constraint);
        View reject = reject_accept.findViewById(R.id.reject_constraint);

        SimplePrefs.putBoolean(Elderoid.IS_CELSIUS, true);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_weather), R.drawable.netie),
                        new Cue(
                                Html.fromHtml(String.format(
                                        Elderoid.string(R.string.config_accept_reject),
                                        "<b>" + Elderoid.string(R.string.accept) + "</b>",
                                        "<b>" + Elderoid.string(R.string.reject) + "</b>"
                                )),
                                R.drawable.netie
                        )
                ),
                false,
                v-> startActivity(new Intent(getApplicationContext(), AppsConfigurationActivity.class))
        );

        accept.setOnClickListener(v -> {
            DialogTempScale dialog = new DialogTempScale(Elderoid.string(R.string.dialog_attention),
                    Elderoid.string(R.string.dialog_weather_message),
                    Elderoid.string(R.string.dialog_ready),
                    Elderoid.string(R.string.dialog_cancel),
                    (d, w) -> proceed(true),
                    (d, w) -> d.cancel());
            dialog.show(getSupportFragmentManager(), "Weather Dialog");
        });

        reject.setOnClickListener(v -> proceed(false));
    }

    void proceed(boolean accepted) {
        SimplePrefs.putBoolean(Elderoid.FUNC_WEATHER, accepted);
        // Next Configuration Activity:
        startActivity(new Intent(getApplicationContext(), PhotosConfigurationActivity.class));
    }
}