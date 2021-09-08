package com.alcachofra.elderoid.utils.animatedViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.widget.AppCompatButton;

import com.alcachofra.elderoid.R;

public class AnimatedButton extends AppCompatButton {

    // Animations:
    Animation scaleDown;
    Animation scaleUp;

    // OnClick Runnable:
    Runnable onClick;

    public AnimatedButton(Context context) {
        super(context);
        scaleUp = AnimationUtils.loadAnimation(context, R.anim.medium_scale_up);
        scaleDown = AnimationUtils.loadAnimation(context, R.anim.medium_scale_down);
    }

    public AnimatedButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleUp = AnimationUtils.loadAnimation(context, R.anim.medium_scale_up);
        scaleDown = AnimationUtils.loadAnimation(context, R.anim.medium_scale_down);
    }

    /**
     * On touch listener. This animates the view.
     * @param event Object used to report movement.
     * @return True if the event was handled. False otherwise.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startAnimation(scaleUp);
                return super.onTouchEvent(event);

            case MotionEvent.ACTION_UP:
                startAnimation(scaleDown);
                if (onClick != null) onClick.run();
                return super.onTouchEvent(event);
        }
        return false;
    }

    /**
     * Set a Runnable listener for when view is clicked.
     * @param onClick Runnable listener.
     */
    public void setOnClickRunnable(Runnable onClick) {
        this.onClick = onClick;
    }
}
