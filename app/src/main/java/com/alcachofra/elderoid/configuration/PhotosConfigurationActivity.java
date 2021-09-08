package com.alcachofra.elderoid.configuration;

import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
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

public class PhotosConfigurationActivity extends ElderoidActivity {

    Netie netie;
    private boolean cameraAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photos_configuration);



        ConstraintLayout reject_accept = findViewById(R.id.reject_accept);
        View accept = reject_accept.findViewById(R.id.accept_constraint);
        View reject = reject_accept.findViewById(R.id.reject_constraint);

        try {
            cameraAvailable = isCameraAvailable();
        } catch (Exception e) {
            e.printStackTrace();
        }

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_BACK_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.config_photos), R.drawable.netie_happy),
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
                v-> startActivity(new Intent(getApplicationContext(), WeatherConfigurationActivity.class))
        );

        accept.setOnClickListener(v -> {
            if (!cameraAvailable) {
                netie.setBalloon(Elderoid.string(R.string.no_camera))
                        .setExpression(R.drawable.netie_concerned);
            }
            else proceed(true);
        });
        reject.setOnClickListener(v -> proceed(false));
    }

    void proceed(boolean accepted) {
        SimplePrefs.putBoolean(Elderoid.FUNC_PHOTOS, accepted);
        // Next Configuration Activity:
        startActivity(new Intent(getApplicationContext(), FlashlightConfigurationActivity.class));
    }

    private boolean isCameraAvailable() throws CameraAccessException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return ((CameraManager) getSystemService(Context.CAMERA_SERVICE)).getCameraIdList().length > 0;
        }
        else return Camera.getNumberOfCameras() > 0; // No problem in being deprecated, since this will be run for (<21) only
    }
}