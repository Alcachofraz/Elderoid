package com.alcachofra.elderoid.utils.netie;

import android.view.View;

import androidx.annotation.DrawableRes;

/**
 * Class representing a Cue (Something Netie says).
 * Consists of either:
 *      A message and an expression;
 *      A message, an expression and an option;
 *      A message, an expression and two options.
 */
public class Cue implements Comparable<Cue> {
    private final CharSequence text;
    private CharSequence option1;
    private CharSequence option2;
    private View.OnClickListener listenerOption1;
    private View.OnClickListener listenerOption2;
    private final int expression;

    /**
     * Constructor of Cue for Netie.
     * @param text String containing message.
     * @param expression Drawable Resource for expression.
     */
    public Cue(CharSequence text, @DrawableRes int expression) {
        this.text = text;
        this.expression = expression;
    }

    /**
     * Compare this Cue to another Cue, in a sorting environment. Compares Message (text).
     * @param c Another Cue.
     * @return Same as String.compareTo().
     */
    @Override
    public int compareTo(Cue c) {
        return getText().toString().compareTo(c.getText().toString());
    }

    /**
     * Compares this Cue to another Cue (use to distinguish two Cues). Compares all fields: text, expression and options.
     * @param o Another Cue.
     * @return true if Cues are equal.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Cue) {
            Cue c = (Cue) o;
            return getText().equals(c.getText()) && getOption1().equals(c.getOption1()) && getOption2().equals(c.getOption2()) && getExpression() == c.getExpression();
        }
        return false;
    }

    /**
     * Returns a hash code value for the object.
     * @return int a hash code value for this object
     */
    @Override
    public int hashCode() {
        return getText().hashCode() + getOption1().hashCode() + getOption2().hashCode() + getExpression();
    }

    /**
     * String value of this Cue [<Text> (<Option1>) (<Option2>).].
     * @return String representing Cue.
     */
    @Override
    public String toString() {
        return getText() + " (" + getOption1() + ") (" + getOption1() + ")";
    }

    /**
     * Set Option 1 on this Cue.
     * @param option1 Text for option button.
     * @param listener Option button click listener.
     * @return This Cue.
     */
    public Cue setOption1(CharSequence option1, View.OnClickListener listener) {
        this.option1 = option1;
        this.listenerOption1 = listener;
        return this;
    }

    /**
     * Set Option 2 on this Cue.
     * @param option2 Text for option button.
     * @param listener Option button click listener.
     * @return This Cue.
     */
    public Cue setOption2(CharSequence option2, View.OnClickListener listener) {
        this.option2 = option2;
        this.listenerOption2 = listener;
        return this;
    }

    /**
     * Get Text on this Cue.
     * @return String containing this Cue's text.
     */
    public CharSequence getText() {
        return text;
    }

    /**
     * Get Option 1 button text.
     * @return String containing option 1 text.
     */
    public CharSequence getOption1() {
        return option1;
    }

    /**
     * Get Option 2 button text.
     * @return String containing option 2 text.
     */
    public CharSequence getOption2() {
        return option2;
    }

    /**
     * Get Listener of Option 1 button.
     * @return Listener.
     */
    public View.OnClickListener getListenerOption1() {
        return listenerOption1;
    }

    /**
     * Get Listener of Option 2 button.
     * @return Listener.
     */
    public View.OnClickListener getListenerOption2() {
        return listenerOption2;
    }

    /**
     * Get this Cue's Netie expression
     * @return Drawable Resource integer value.
     */
    public @DrawableRes int getExpression() {
        return expression;
    }
}
