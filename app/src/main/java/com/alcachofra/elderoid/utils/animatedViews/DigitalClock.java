package com.alcachofra.elderoid.utils.animatedViews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.widget.AppCompatTextView;

import com.alcachofra.elderoid.R;

import java.util.Calendar;

public class DigitalClock extends AppCompatTextView {

    Calendar mCalendar;
    private final static String m24 = "k:mm";

    private Runnable mTicker;
    private Handler mHandler;

    private boolean mTickerStopped = false;

    String mFormat;

    // Animations:
    Animation scaleDown;
    Animation scaleUp;

    // OnClick Runnable:
    Runnable onClick;

    public DigitalClock(Context context) {
        super(context);
        scaleUp = AnimationUtils.loadAnimation(context, R.anim.medium_scale_up);
        scaleDown = AnimationUtils.loadAnimation(context, R.anim.medium_scale_down);
        initClock();
    }

    public DigitalClock(Context context, AttributeSet attrs) {
        super(context, attrs);
        scaleUp = AnimationUtils.loadAnimation(context, R.anim.medium_scale_up);
        scaleDown = AnimationUtils.loadAnimation(context, R.anim.medium_scale_down);
        initClock();
    }

    /**
     * Initialise clock.
     */
    private void initClock() {
        if (mCalendar == null) {
            mCalendar = Calendar.getInstance();
        }

        FormatChangeObserver mFormatChangeObserver = new FormatChangeObserver();
        getContext().getContentResolver().registerContentObserver(
                Settings.System.CONTENT_URI, true, mFormatChangeObserver);

        setFormat();
    }

    /**
     * Callback to when clock is put on screen.
     */
    @Override
    protected void onAttachedToWindow() {
        mTickerStopped = false;
        super.onAttachedToWindow();
        mHandler = new Handler();

        mTicker = () -> {
            if (mTickerStopped) return;
            mCalendar.setTimeInMillis(System.currentTimeMillis());
            setText(DateFormat.format(mFormat, mCalendar));
            invalidate();
            long now = SystemClock.uptimeMillis();
            long next = now + (1000 - now % 1000);
            mHandler.postAtTime(mTicker, next);
        };
        mTicker.run();
    }

    /**
     * Callback to when clock is removed from screen.
     */
    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mTickerStopped = true;
    }

    /**
     * Set format.
     */
    private void setFormat() {
        mFormat = m24;

    }

    /**
     * Observer for Format changes.
     */
    private class FormatChangeObserver extends ContentObserver {
        public FormatChangeObserver() {
            super(new Handler());
        }

        @Override
        public void onChange(boolean selfChange) {
            setFormat();
        }
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
