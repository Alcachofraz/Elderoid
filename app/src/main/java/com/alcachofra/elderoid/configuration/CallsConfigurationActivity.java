package com.alcachofra.elderoid.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.view.View;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.SimplePrefs;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Arrays;

public class CallsConfigurationActivity extends ElderoidActivity {
    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calls_configuration);



        ConstraintLayout reject_accept = findViewById(R.id.reject_accept);
        View accept = reject_accept.findViewById(R.id.accept_constraint);
        View reject = reject_accept.findViewById(R.id.reject_constraint);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_calls), R.drawable.netie_happy),
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
                v-> startActivity(new Intent(getApplicationContext(), FeaturesConfigurationActivity.class))
        );

        accept.setOnClickListener(v -> proceed(true));
        reject.setOnClickListener(v -> proceed(false));
    }

    void proceed(boolean accepted) {
        SimplePrefs.putBoolean(Elderoid.FUNC_CALLS, accepted);
        // Next Configuration Activity:
        startActivity(new Intent(getApplicationContext(), ContactsConfigurationActivity.class));
    }
}
