package com.alcachofra.elderoid.utils.netie;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.res.ResourcesCompat;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;
import com.alcachofra.elderoid.utils.animatedViews.DigitalClock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * API to deal with Netie window easily, in any class. Just create Netie
 * instance, and use methods to dynamically modify its components.
 * <p>
 * Note: Documentation in this class uses word "throw" a lot. In no circumstance
 * (unless specified) this has anything to do with throw java keyword.
 * </p>
 */
public class Netie {
    public enum NetieWindowType {
        /**
         * Netie window with a button that takes the user to home screen.
         */
        WITH_HOME_BUTTON,
        /**
         * Netie window with a button that takes the user to the previous activity.
         */
        WITH_BACK_BUTTON,
        /**
         * Netie window with a digital clock.
         */
        WITH_CLOCK,
        /**
         * Netie window alone.
         */
        NONE
    }

    /**
     * Format of Netie date.
     */
    public static final DateTimeFormatter NETIE_DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    // Invoker activity:
    private AppCompatActivity activity;

    // Netie window:
    private View netieWindow;

    // General views:
    private NetieView netie;
    private AppCompatButton option1;
    private AppCompatButton option2;
    private AppCompatTextView balloonText;

    // Specific views:
    private AppCompatImageButton back;
    private DigitalClock clock;

    // Cue Pool:
    private List<Cue> cuePool = new ArrayList<>();
    private int cueIndex = 0;

    /**
     * Netie constructor.
     * @param activity Activity in which Netie window is being initialised.
     * @param type Type of Netie window: NetieWindowType {WITH_BACK_BUTTON, WITH_HOME_BUTTON, WITH_CLOCK, NONE}.
     * @param cuePool Collection of Cues that should Netie say.
     * @param shuffle True if cuePool should be shuffled, false if order is important.
     */
    public Netie(AppCompatActivity activity, NetieWindowType type, Collection<Cue> cuePool, boolean shuffle) {
        initialise(activity, type, cuePool, shuffle, null);
    }

    /**
     * Netie constructor.
     * @param activity Activity in which Netie window is being initialised.
     * @param type Type of Netie window: NetieWindowType {WITH_BACK_BUTTON, WITH_HOME_BUTTON, WITH_CLOCK, NONE}.
     * @param cuePool Collection of Cues that should Netie say.
     * @param shuffle True if cuePool should be shuffled, false if order is important.
     * @param onClickBack In case you want to override back button listener (type must be WITH_BACK_BUTTON). Else, use the other constructor.
     */
    public Netie(AppCompatActivity activity, NetieWindowType type, Collection<Cue> cuePool, boolean shuffle, View.OnClickListener onClickBack) {
        initialise(activity, type, cuePool, shuffle, onClickBack);
    }

    /**
     * Netie initializer, used by both constructors. Initialises Cue Pool and layout views according to type. Also, Netie throws the first Cue.
     * @param activity Activity in which Netie window is being initialised.
     * @param type Type of Netie window: NetieWindowType {WITH_BACK_BUTTON, WITH_HOME_BUTTON, WITH_CLOCK, NONE}.
     * @param cuePool Collection of Cues that should Netie say.
     * @param shuffle True if cuePool should be shuffled, false if order is important.
     * @param onClickBack In case you want to override back button listener (type must be WITH_BACK_BUTTON).
     */
    private void initialise(AppCompatActivity activity, NetieWindowType type, Collection<Cue> cuePool, boolean shuffle, View.OnClickListener onClickBack) {
        this.activity = activity;
        netieWindow = activity.findViewById(R.id.netie_window);

        // General views:
        balloonText = netieWindow.findViewById(R.id.balloon_text);
        netie = (NetieView) netieWindow.findViewById(R.id.netie);
        option1 = netieWindow.findViewById(R.id.option1);
        option2 = netieWindow.findViewById(R.id.option2);
        back = netieWindow.findViewById(R.id.back);
        clock = netieWindow.findViewById(R.id.clock);

        // Specific views:
        switch (type) {
            case WITH_BACK_BUTTON:
                clock.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(onClickBack == null ? v -> activity.finish() : onClickBack);
                break;
            case WITH_HOME_BUTTON:
                clock.setVisibility(View.GONE);
                back.setVisibility(View.VISIBLE);
                back.setOnClickListener(v -> {
                    Intent startMain = new Intent(Intent.ACTION_MAIN);
                    startMain.addCategory(Intent.CATEGORY_HOME);
                    startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(startMain);
                });
                break;
            case WITH_CLOCK:
                clock.setVisibility(View.VISIBLE);
                back.setVisibility(View.GONE);
                clock.setOnClickRunnable(
                        () -> setBalloon(Elderoid.string(R.string.the_time_is) + " " + clock.getText() + ", " + LocalDate.now().format(NETIE_DATE_FORMAT) + ".")
                                .setExpression(R.drawable.netie)
                );
                break;
            default:
                clock.setVisibility(View.GONE);
                back.setVisibility(View.GONE);
                break;
        }

        if (cuePool != null && cuePool.size() > 0) {
            this.cuePool.addAll(cuePool);
            if (shuffle) Collections.shuffle(this.cuePool);
            netie.setOnClickRunnable(this::nextCue);
            firstCue();
        }
    }

    /**
     * Clean options from Netie window and set new message.
     * @param balloonMsg String Resource containing message.
     * @return Netie instance.
     */
    public Netie setBalloon(@StringRes int balloonMsg) {
        cleanOptions();
        balloonText.setText(balloonMsg);
        return this;
    }

    /**
     * Clean options from Netie window and set new message.
     * @param balloonMsg CharSequence containing message.
     * @return Netie instance.
     */
    public Netie setBalloon(CharSequence balloonMsg) {
        cleanOptions();
        balloonText.setText(balloonMsg);
        return this;
    }

    /**
     * Clean options from Netie window and set new message.
     * @param balloonMsg Spannable containing message.
     * @return Netie instance.
     */
    public Netie setBalloon(Spannable balloonMsg) {
        cleanOptions();
        balloonText.setText(balloonMsg);
        return this;
    }

    /**
     * Set first option on Netie window.
     * @param balloonMsg String resource containing option text.
     * @param onClickListener Listener to option.
     * @return Netie instance.
     */
    public Netie setOption1(@StringRes int balloonMsg, View.OnClickListener onClickListener) {
        option1.setText(balloonMsg);
        option1.setOnClickListener(onClickListener);
        option1.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * Set first option on Netie window.
     * @param balloonMsg CharSequence containing option text.
     * @param onClickListener Listener to option.
     * @return Netie instance.
     */
    public Netie setOption1(CharSequence balloonMsg, View.OnClickListener onClickListener) {
        option1.setText(balloonMsg);
        option1.setOnClickListener(onClickListener);
        option1.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * Set second option on Netie window.
     * @param balloonMsg String resource containing option text.
     * @param onClickListener Listener to option.
     * @return Netie instance.
     */
    public Netie setOption2(@StringRes int balloonMsg, View.OnClickListener onClickListener) {
        option2.setText(balloonMsg);
        option2.setOnClickListener(onClickListener);
        option2.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * Set second option on Netie window.
     * @param balloonMsg CharSequence containing option text.
     * @param onClickListener Listener to option.
     * @return Netie instance.
     */
    public Netie setOption2(CharSequence balloonMsg, View.OnClickListener onClickListener) {
        option2.setText(balloonMsg);
        option2.setOnClickListener(onClickListener);
        option2.setVisibility(View.VISIBLE);
        return this;
    }

    /**
     * Set Netie facial expression.
     * @param drawable Drawable Resource of Netie facial expression to use.
     * @return Netie instance.
     */
    public Netie setExpression(@DrawableRes int drawable) {
        netie.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), drawable, activity.getTheme()));
        return this;
    }

    /**
     * Set Netie facial expression.
     * @param drawable Drawable of Netie facial expression to use.
     * @return Netie instance.
     */
    public Netie setExpression(Drawable drawable) {
        netie.setImageDrawable(drawable);
        return this;
    }

    /**
     * Reset Cue Pool. Erases existing Cue Pool. Also, Netie instantly throws the first Cue.
     * @param cuePool Collection of Cues, containing new Cue Pool.
     * @param shuffle True if cuePool should be shuffled, false if order is important.
     */
    public void resetCuePool(Collection<Cue> cuePool, boolean shuffle) {
        this.cuePool.clear();
        this.cuePool.addAll(cuePool);
        if (shuffle) Collections.shuffle(this.cuePool);
        firstCue();
    }

    /**
     * Add a Collection of Cues to existing Cue Pool.
     * @param cuePool Collection of Cues to add.
     */
    public void addToCuePool(Collection<Cue> cuePool) {
        this.cuePool.addAll(cuePool);
    }

    /**
     * Netie throws next Cue, and increments internal Cue counter.
     */
    public void nextCue() {
        setCue(cuePool.get((++cueIndex >= cuePool.size()) ? (cueIndex = 0) : cueIndex));
    }

    /**
     * Netie throws the first Cue. Sets internal Cue counter to 0.
     */
    public void firstCue() {
        setCue(cuePool.get(cueIndex = 0));
    }

    /**
     * Clean all options from Netie window.
     */
    private void cleanOptions() {
        option1.setVisibility(View.GONE);
        option2.setVisibility(View.GONE);
    }

    /**
     * Netie throws designated Cue.
     * @param cue Cue to throw.
     */
    private void setCue(Cue cue) {
        setBalloon(cue.getText());
        setExpression(cue.getExpression());
        if (cue.getOption1() != null) setOption1(cue.getOption1(), cue.getListenerOption1());
        if (cue.getOption2() != null) setOption2(cue.getOption2(), cue.getListenerOption2());
    }
}
