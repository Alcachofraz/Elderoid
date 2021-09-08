package com.alcachofra.elderoid.utils.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.alcachofra.elderoid.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Consumer;

/**
 * Dialog Box that shows App Filter Options.
 */
public class DialogAppFilter extends AppCompatDialogFragment {
    private final String title;
    private final String message;
    private final Spanned messageSpanned;
    private final String positive;
    private final String negative;
    private final Consumer<ArrayList<Boolean>> positiveAction;
    private final DialogInterface.OnClickListener negativeAction;
    private Consumer<DialogInterface> onCancel;
    private int drawable;
    private final boolean initSocial;
    private final boolean initGames;
    private final boolean initOther;

    /**
     * Constructor of App Filter Options Dialog Box.
     * @param title String containing title of Dialog Box.
     * @param message String containing message under title.
     * @param positive Text of positive button.
     * @param negative Text of negative button.
     * @param positiveAction Listener to positive button.
     * @param negativeAction Listener to negative button.
     * @param social Initial boolean value of social field.
     * @param games Initial boolean value of games field.
     * @param other Initial boolean value of other field.
     */
    public DialogAppFilter(String title, String message, String positive, String negative, Consumer<ArrayList<Boolean>> positiveAction, DialogInterface.OnClickListener negativeAction,
                            boolean social, boolean games, boolean other) {
        this.title = title;
        this.message = message;
        this.messageSpanned = null;
        this.positive = positive;
        this.negative = negative;
        this.positiveAction = positiveAction;
        this.negativeAction = negativeAction;
        this.initSocial = social;
        this.initGames = games;
        this.initOther = other;
    }

    /**
     * Constructor of App Filter Options Dialog Box.
     * @param title String containing title of Dialog Box.
     * @param message Spanned containing message under title.
     * @param positive Text of positive button.
     * @param negative Text of negative button.
     * @param positiveAction Listener to positive button.
     * @param negativeAction Listener to negative button.
     * @param social Initial boolean value of social field.
     * @param games Initial boolean value of games field.
     * @param other Initial boolean value of other field.
     */
    public DialogAppFilter(String title, Spanned message, String positive, String negative, Consumer<ArrayList<Boolean>> positiveAction, DialogInterface.OnClickListener negativeAction,
                           boolean social, boolean games, boolean other) {
        this.title = title;
        this.message = null;
        this.messageSpanned = message;
        this.positive = positive;
        this.negative = negative;
        this.positiveAction = positiveAction;
        this.negativeAction = negativeAction;
        this.initSocial = social;
        this.initGames = games;
        this.initOther = other;
    }

    /**
     * Set image to appear behind dialog title.
     * @param drawable Drawable Resource of image.
     */
    public void setImage(@DrawableRes int drawable) {
        this.drawable = drawable;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_app_filter, null);

        AppCompatCheckBox social = view.findViewById(R.id.social_checkbox);
        AppCompatCheckBox games = view.findViewById(R.id.games_checkbox);
        AppCompatCheckBox other = view.findViewById(R.id.other_checkbox);

        social.setChecked(initSocial);
        games.setChecked(initGames);
        other.setChecked(initOther);

        builder.setView(view)
                .setTitle(title)
                .setMessage(message == null ? messageSpanned : message);
        if (positive != null)
            builder.setPositiveButton(
                    positive,
                    (d, w) -> positiveAction.accept(new ArrayList<>(Arrays.asList(social.isChecked(), games.isChecked(), other.isChecked())))
            );
        if (negative != null)
            builder.setNegativeButton(negative, negativeAction);
        if (drawable != 0)
            builder.setIcon(drawable);
        return builder.create();
    }

    @Override
    public void onCancel(@NonNull DialogInterface dialog) {
        super.onCancel(dialog);
        negativeAction.onClick(getDialog(), getId());
    }
}
