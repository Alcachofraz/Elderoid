package com.alcachofra.elderoid.configuration;

import androidx.appcompat.widget.AppCompatButton;

import android.content.Intent;
import android.os.Bundle;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;

import java.util.Collections;

public class FinishConfigurationActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_finish_configuration);



        AppCompatButton proceed = findViewById(R.id.proceed);

        netie = new Netie(
                this,
                Netie.NetieWindowType.NONE,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.config_finish), R.drawable.netie)
                ),
                false
        );

        proceed.setOnClickListener(v -> {
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        });
    }
}