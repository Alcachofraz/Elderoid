package com.alcachofra.elderoid.configuration;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.Build;
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

public class FlashlightConfigurationActivity extends ElderoidActivity {

    Netie netie;
    private boolean flashlightAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_flashlight_configuration);



        ConstraintLayout reject_accept = findViewById(R.id.reject_accept);
        View accept = reject_accept.findViewById(R.id.accept_constraint);
        View reject = reject_accept.findViewById(R.id.reject_constraint);

        try {
            flashlightAvailable = isFlashlightAvailable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_flashlight), R.drawable.netie),
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
                v-> startActivity(new Intent(getApplicationContext(), PhotosConfigurationActivity.class))
        );

        accept.setOnClickListener(v -> {
            if (!flashlightAvailable) {
                netie.setBalloon(Elderoid.string(R.string.no_flashlight))
                        .setExpression(R.drawable.netie_concerned);
            }
            else proceed(true);
        });
        reject.setOnClickListener(v -> proceed(false));
    }

    void proceed(boolean accepted) {
        if (SimplePrefs.getInt(Elderoid.CONFIG_STAGE, 0) >= 4) {
            SimplePrefs.putBoolean(Elderoid.FUNC_FLASHLIGHT, accepted);
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }
        else {
            SimplePrefs.putBoolean(Elderoid.FUNC_FLASHLIGHT, accepted);
            SimplePrefs.putInt(Elderoid.CONFIG_STAGE, 2);
            // Next Configuration Activity:
            startActivity(new Intent(getApplicationContext(), PermissionsConfigurationActivity.class));
        }
    }

    private boolean isFlashlightAvailable() throws CameraAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            for (String camera : cameraManager.getCameraIdList()) {
                if (cameraManager.getCameraCharacteristics(camera).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                    return true;
                }
            }
        }
        else {
            return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        }

        return false;
    }
}