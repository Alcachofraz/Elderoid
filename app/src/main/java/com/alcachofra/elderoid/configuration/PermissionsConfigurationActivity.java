package com.alcachofra.elderoid.configuration;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class PermissionsConfigurationActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permissions_configuration);



        AppCompatButton allow = findViewById(R.id.allow);
        AppCompatButton proceed = findViewById(R.id.proceed);
        AppCompatTextView permissions = findViewById(R.id.permissions);

        String tmp = "O Elderoid irá:" + "\n" +
                "  1. " + "Aceder aos contatos;" + "\n" +
                "  2. " + "Realizar e gerir chamadas;" + "\n" +
                "  3. " + "Aceder ao registo de chamadas;" + "\n" +
                "  4. " + "Aceder à câmera;" + "\n" +
                "  5. " + "Aceder à localização;" + "\n" +
                "  6. " + "Aceder aos ficheiros." + "\n" +
                "  7. " + "Ver e enviar mensagens";
        permissions.setText(Elderoid.string(R.string.permission_list));

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_permissions_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_permissions_2), R.drawable.netie)
                ),
                false,
                v -> {
                    SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 1);
                    startActivity(new Intent(getApplicationContext(), FlashlightConfigurationActivity.class));
                }
        );

        allow.setOnClickListener(v -> {
            if (manifestPermissionsAllowed()) {
                netie.setBalloon(Elderoid.string(R.string.config_proceed))
                        .setExpression(R.drawable.netie);
            }
            else askForManifestPermissions();
        });

        proceed.setOnClickListener(v -> {
            if (manifestPermissionsAllowed()) {
                SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 3);
                Intent intent = new Intent(getApplicationContext(), LauncherConfigurationActivity.class);
                startActivity(intent);
            }
            else netie.setBalloon(Elderoid.string(R.string.config_permissions_not_yet))
                        .setExpression(R.drawable.netie_concerned);
        });
    }

    private void askForManifestPermissions() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            ActivityCompat.requestPermissions(this, info.requestedPermissions, Elderoid.PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private boolean manifestPermissionsAllowed() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_PERMISSIONS);
            for (String permission : info.requestedPermissions) {
                if (!permission.equals(Manifest.permission.QUERY_ALL_PACKAGES) && ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }
}