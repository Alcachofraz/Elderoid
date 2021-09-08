package com.alcachofra.elderoid.configuration;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class UserConfigurationActivity extends ElderoidActivity {

    Netie netie;
    private AppCompatEditText name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_configuration);



        name = findViewById(R.id.name);
        AppCompatButton proceed = findViewById(R.id.proceed);

        name.setText(SimplePrefs.getString(Elderoid.USERNAME, null));


        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_user_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_user_2), R.drawable.netie)
                ),
                false,
                v -> startActivity(new Intent(getApplicationContext(), WelcomeConfigurationActivity.class))
        );

        proceed.setOnClickListener(v -> {
            Editable editable = name.getText();
            String username = (editable == null) ? "" : name.getText().toString().trim();
            if (username.length() > 2) {
                SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 1);
                SimplePrefs.putString(Elderoid.USERNAME, username);
                Intent intent = new Intent(getApplicationContext(), FeaturesConfigurationActivity.class);
                startActivity(intent);
            }
            else netie.setBalloon(Elderoid.string(R.string.config_username_error))
                        .setExpression(R.drawable.netie_confused);
        });
    }
}