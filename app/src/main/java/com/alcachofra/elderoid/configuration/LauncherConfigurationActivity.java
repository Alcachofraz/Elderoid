package com.alcachofra.elderoid.configuration;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;

import android.app.Activity;
import android.app.role.RoleManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.FakeLauncherActivity;
import com.alcachofra.elderoid.MenuActivity;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class LauncherConfigurationActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_launcher_configuration);



        AppCompatButton settings = findViewById(R.id.settings);
        AppCompatButton proceed = findViewById(R.id.proceed);
        AppCompatImageButton gears = findViewById(R.id.gears);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_launcher_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_launcher_2), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.config_launcher_3), R.drawable.netie)
                ),
                false,
                v -> {
                    SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 2);
                    startActivity(new Intent(getApplicationContext(), PermissionsConfigurationActivity.class));
                }
        );

        settings.setOnClickListener(v -> {
            if (!isDefaultLauncher()) chooseLauncher();
            else netie.setBalloon(Elderoid.string(R.string.config_launcher_already));
        });

        proceed.setOnClickListener(v -> {
            if (isDefaultLauncher()) {
                SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 4);
                SimplePrefs.putString(Elderoid.CURRENT_DAY, Elderoid.CURRENT_DAY_FORMAT);
                startActivity(new Intent(getApplicationContext(), FinishConfigurationActivity.class));
            }
            else netie.setBalloon(Elderoid.string(R.string.config_launcher_not_yet))
                        .setExpression(R.drawable.netie_confused);
        });

        gears.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_SETTINGS)));
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
        new ActivityResultContracts.StartActivityForResult(),
        new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() != Activity.RESULT_OK) {
                    netie.setBalloon(Elderoid.string(R.string.config_error))
                            .setExpression(R.drawable.netie_concerned);
                }
                else startActivity(new Intent(getApplicationContext(), MenuActivity.class));
            }
        }
    );

    private void chooseLauncher() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            RoleManager roleManager = (RoleManager) getSystemService(Context.ROLE_SERVICE);
            if (roleManager.isRoleAvailable(RoleManager.ROLE_HOME)) {
                Intent intent = roleManager.createRequestRoleIntent(RoleManager.ROLE_HOME);
                activityResultLauncher.launch(intent);
            }
        }
        else {
            PackageManager p = getPackageManager();
            Intent preferredApps = new Intent("com.android.settings.PREFERRED_SETTINGS");
            preferredApps.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            if (p.resolveActivity(preferredApps, 0) != null) {
                startActivity(preferredApps);
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
                }
                else {
                    ComponentName cN = new ComponentName(getApplicationContext(), FakeLauncherActivity.class);
                    p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                    Intent selector = new Intent(Intent.ACTION_MAIN);
                    selector.addCategory(Intent.CATEGORY_HOME);
                    selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(selector);

                    p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
                }
            }
        }
    }

    private boolean isDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;

        return getPackageName().equals(currentHomePackage);
    }
}