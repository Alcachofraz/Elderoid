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
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;

public class AppsConfigurationActivity extends ElderoidActivity {
    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apps_configuration);



        ConstraintLayout reject_accept = findViewById(R.id.reject_accept);
        View accept = reject_accept.findViewById(R.id.accept_constraint);
        View reject = reject_accept.findViewById(R.id.reject_constraint);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_choose_apps_1), R.drawable.netie_happy),
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
                v-> startActivity(new Intent(getApplicationContext(), SimplePrefs.getBoolean(Elderoid.FUNC_CONTACTS) ? MessagesConfigurationActivity.class : ContactsConfigurationActivity.class))
        );

        accept.setOnClickListener(v -> proceed(true));
        reject.setOnClickListener(v -> proceed(false));
    }

    void proceed(boolean accepted) {
        SimplePrefs.putBoolean(Elderoid.FUNC_APPS, accepted);
        // Next Configuration Activity:
        startActivity(new Intent(getApplicationContext(), accepted ? ChooseAppsConfigurationActivity.class : WeatherConfigurationActivity.class));
    }
}