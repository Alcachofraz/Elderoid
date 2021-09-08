package com.alcachofra.elderoid.utils;

import androidx.appcompat.widget.AppCompatCheckBox;

import java.util.function.Consumer;

public class FeatureInfo implements Comparable<FeatureInfo> {
    private String name;
    private Consumer<AppCompatCheckBox> onClickListener;
    private boolean enabled;

    /**
     * Constructor for FeatureInfo.
     * @param name Name of feature.
     * @param enabled Is feature enabled.
     * @param onClickListener On click listener.
     */
    public FeatureInfo(String name, boolean enabled, Consumer<AppCompatCheckBox> onClickListener) {
        this.name = name;
        this.onClickListener = onClickListener;
        this.enabled = enabled;
    }

    /**
     * Compares this FeatureInfo to another FeatureInfo (sorting environment). Compares name.
     * @param f Another FeatureInfo.
     * @return Same as String.compareTo().
     */
    @Override
    public int compareTo(FeatureInfo f) {
        return getName().compareTo(f.getName());
    }

    /**
     * Compares this FeatureInfo to another FeatureInfo (use to distinguish two FeatureInfo). Compares name.
     * @param o Another FeatureInfo.
     * @return True if the same.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof FeatureInfo) {
            FeatureInfo f = (FeatureInfo) o;
            return getName().equals(f.getName());
        }
        return false;
    }

    /**
     * Returns a hash code for this object.
     * @return int a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getName().hashCode() + getOnClickListener().hashCode();
    }

    /**
     * String value of this FeatureInfo.
     * @return String containing this FeatureInfo's information.
     */
    @Override
    public String toString() {
        return "FeatureInfo{" +
                "name='" + name + '\'' +
                '}';
    }

    /**
     * Get name of this feature.
     * @return String containing name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set name of this feature.
     * @param name String containing name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get this feature's onClick Listener.
     * @return Listener.
     */
    public Consumer<AppCompatCheckBox> getOnClickListener() {
        return onClickListener;
    }

    /**
     * Set this feature's onClick Listener.
     * @param onClickListener Listener.
     */
    public void setOnClickListener(Consumer<AppCompatCheckBox> onClickListener) {
        this.onClickListener = onClickListener;
    }

    /**
     * Is this feature enabled.
     * @return True if is enabled. False otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Set feature enabled.
     * @param enabled True to enable. False to disable.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
