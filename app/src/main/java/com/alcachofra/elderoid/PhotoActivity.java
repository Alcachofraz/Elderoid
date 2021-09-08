package com.alcachofra.elderoid;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.bitmap.CenterInside;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.github.chrisbanes.photoview.PhotoView;

import java.util.Arrays;
import java.util.Collections;

public class PhotoActivity extends ElderoidActivity {

    Netie netie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);



        String path = getIntent().getStringExtra(Elderoid.IMAGE_FILE_NAME);

        RequestOptions requestOptions = new RequestOptions().transform(new CenterInside(), new RoundedCorners(20));

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Collections.singletonList(
                        new Cue(Elderoid.string(R.string.wait), R.drawable.netie_confused)
                ),
                false
        );

        PhotoView image = findViewById(R.id.image);
        AppCompatImageButton back = findViewById(R.id.back);
        ConstraintLayout background = findViewById(R.id.background);
        AppCompatImageView loading = findViewById(R.id.loading);

        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.loading);
        loading.startAnimation(rotate);

        Glide.with(Elderoid.getContext())
                .load(path)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        loading.clearAnimation();
                        loading.setVisibility(View.GONE);
                        netie.resetCuePool(Collections.singletonList(
                                new Cue(Elderoid.string(R.string.photo_error), R.drawable.netie_confused)
                        ), false);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        loading.clearAnimation();
                        loading.setVisibility(View.GONE);
                        background.setVisibility(View.VISIBLE);
                        netie.resetCuePool(Arrays.asList(
                                new Cue(Elderoid.string(R.string.photo_zoom), R.drawable.netie),
                                new Cue(Elderoid.string(R.string.photo_good), R.drawable.netie_happy)
                        ), true);
                        return false;
                    }
                })
                .apply(requestOptions)
                .into(image);

        back.setOnClickListener(v -> finish());
    }
}