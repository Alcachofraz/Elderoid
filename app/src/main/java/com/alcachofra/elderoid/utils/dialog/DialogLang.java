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
import androidx.appcompat.widget.SwitchCompat;

import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;

import java.util.function.Consumer;

/**
 * Dialog Box that shows a binary switch to choose language.
 */
public class DialogLang extends AppCompatDialogFragment {
    private final String title;
    private final String message;
    private final Spanned messageSpanned;
    private final String positive;
    private final String negative;
    private final DialogInterface.OnClickListener positiveAction;
    private final DialogInterface.OnClickListener negativeAction;
    private Consumer<DialogInterface> onCancel;
    private int drawable;

    /**
     * Constructor of Language Switch Dialog Box.
     * @param title String containing title of Dialog Box.
     * @param message String containing message under title.
     * @param positive Text of positive button.
     * @param negative Text of negative button.
     * @param positiveAction Listener to positive button.
     * @param negativeAction Listener to negative button.
     */
    public DialogLang(String title, String message, String positive, String negative, DialogInterface.OnClickListener positiveAction, DialogInterface.OnClickListener negativeAction) {
        this.title = title;
        this.message = message;
        this.messageSpanned = null;
        this.positive = positive;
        this.negative = negative;
        this.positiveAction = positiveAction;
        this.negativeAction = negativeAction;
    }

    /**
     * Constructor of Language Switch Dialog Box.
     * @param title String containing title of Dialog Box.
     * @param message Spanned containing message under title.
     * @param positive Text of positive button.
     * @param negative Text of negative button.
     * @param positiveAction Listener to positive button.
     * @param negativeAction Listener to negative button.
     */
    public DialogLang(String title, Spanned message, String positive, String negative, DialogInterface.OnClickListener positiveAction, DialogInterface.OnClickListener negativeAction) {
        this.title = title;
        this.message = null;
        this.messageSpanned = message;
        this.positive = positive;
        this.negative = negative;
        this.positiveAction = positiveAction;
        this.negativeAction = negativeAction;
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
        View view = inflater.inflate(R.layout.dialog_lang, null);

        SwitchCompat lang_switch = view.findViewById(R.id.lang_switch);
        boolean WAS_PT = Elderoid.getLanguage().equals("pt");
        lang_switch.setChecked(WAS_PT);

        builder.setView(view)
                .setTitle(title)
                .setMessage(message == null ? messageSpanned : message);
        if (positive != null)
            builder.setPositiveButton(positive, (d, w) -> {
                Elderoid.updateLanguage(lang_switch.isChecked() ? "pt" : "en");
                if (lang_switch.isChecked() != WAS_PT) Elderoid.cleanTopics();
                positiveAction.onClick(d, w);
            });
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
