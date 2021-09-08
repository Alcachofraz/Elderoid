package com.alcachofra.elderoid.configuration;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.dialog.DialogLang;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Collections;

public class LangConfigurationActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lang_configuration);

        AppCompatButton lang = findViewById(R.id.lang);
        AppCompatButton proceed = findViewById(R.id.proceed);

        netie = new Netie(
                this,
                Netie.NetieWindowType.NONE,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.config_lang), R.drawable.netie_happy)
                ),
                false
        );

        proceed.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), WelcomeConfigurationActivity.class)));

        lang.setOnClickListener(v -> chooseLanguage());
    }

    private void chooseLanguage() {
        DialogLang dialog = new DialogLang(Elderoid.string(R.string.language),
                Elderoid.string(R.string.dialog_lang),
                Elderoid.string(R.string.dialog_ready),
                Elderoid.string(R.string.dialog_cancel),
                (d, w) -> {
                    finish();
                    startActivity(getIntent());
                },
                (d, w) -> d.cancel());
        dialog.show(getSupportFragmentManager(), "Lang Dialog");
    }
}