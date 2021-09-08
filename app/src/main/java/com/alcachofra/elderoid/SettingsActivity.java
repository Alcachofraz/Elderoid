package com.alcachofra.elderoid;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SwitchCompat;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;

import com.alcachofra.elderoid.configuration.FeaturesConfigurationActivity;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.dialog.DialogLang;
import com.alcachofra.elderoid.utils.dialog.DialogTextInput;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.Arrays;

public class SettingsActivity extends ElderoidActivity {

    Netie netie;
    DialogTextInput nameDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);



        AppCompatButton launcher = findViewById(R.id.launcher);
        AppCompatButton functionalities = findViewById(R.id.functionalities);
        AppCompatButton name = findViewById(R.id.name);
        SwitchCompat temp_switch = findViewById(R.id.temp_switch);
        View temp_choose_view = findViewById(R.id.temp_choose_view);
        AppCompatButton lang = findViewById(R.id.lang);

        temp_choose_view.setVisibility(SimplePrefs.getBoolean(Elderoid.FUNC_WEATHER) ? View.VISIBLE : View.GONE);

        launcher.setText(isDefaultLauncher() ? R.string.disable_elderoid : R.string.enable_elderoid);

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.settings_1), R.drawable.netie_confused),
                        new Cue(Elderoid.string(R.string.settings_2), R.drawable.netie)
                ),
                true
        );

        temp_switch.setChecked(!SimplePrefs.getBoolean(Elderoid.IS_CELSIUS));
        temp_switch.setOnCheckedChangeListener((v, checked) -> SimplePrefs.putBoolean(Elderoid.IS_CELSIUS, !checked));

        lang.setOnClickListener(v -> chooseLanguage());

        launcher.setOnClickListener(v -> chooseLauncher());

        functionalities.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), FeaturesConfigurationActivity.class)));

        name.setOnClickListener(v -> {
            nameDialog = new DialogTextInput(
                    " " + Elderoid.string(R.string.change_name),
                    (String) null,
                    Elderoid.string(R.string.name),
                    Elderoid.string(R.string.dialog_ready),
                    Elderoid.string(R.string.dialog_cancel),
                    (d, w) -> {
                        String text = nameDialog.getInput();
                        if (text.length() > 2) {
                            SimplePrefs.putString(Elderoid.USERNAME, text);
                            netie.setBalloon(String.format(Elderoid.string(R.string.your_name_is_now), text))
                                    .setExpression(R.drawable.netie_happy);
                        }
                        else netie.setBalloon(Elderoid.string(R.string.config_username_error))
                                .setExpression(R.drawable.netie_confused);
                    },
                    (d, w) -> d.cancel()
            );
            nameDialog.setImage(R.drawable.ic_pencil);
            nameDialog.show(getSupportFragmentManager(), "Add Topic Dialog");
        });
    }

    private void chooseLauncher() {
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

    private boolean isDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;

        return getPackageName().equals(currentHomePackage);
    }

    private void chooseLanguage() {
        DialogLang dialog = new DialogLang(Elderoid.string(R.string.language),
                (String) null,
                Elderoid.string(R.string.dialog_ready),
                Elderoid.string(R.string.dialog_cancel),
                (d, w) -> {
                    finish();
                    startActivity(getIntent());
                },
                (d, w) -> d.cancel());
        dialog.show(getSupportFragmentManager(), "Lang Dialog");
    }

    @Override
    public void onBackPressed() {
        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}