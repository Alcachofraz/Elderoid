package com.alcachofra.elderoid.utils;

import android.graphics.drawable.Drawable;

public class AppInfo implements Comparable<AppInfo> {
    private String name;
    private String package_name;
    private int category;
    private Drawable icon;
    private boolean selected;

    /**
     * No-Parameter Constructor. Starts Selected = false.
     */
    public AppInfo() {
        selected = false;
    }

    /**
     * Compares this AppInfo to another AppInfo (sorting environment). Compares name.
     * @param a Another AppInfo.
     * @return Same as String.compareTo().
     */
    @Override
    public int compareTo(AppInfo a) {
        return getName().compareTo(a.getName());
    }

    /**
     * Compares this AppInfo to another AppInfo (use to distinguish two AppInfo). Compares package name.
     * @param o Another AppInfo.
     * @return True if the same.
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof AppInfo) {
            AppInfo a = (AppInfo) o;
            return getPackageName().equals(a.getPackageName());
        }
        return false;
    }

    /**
     * Returns a hash code for this object.
     * @return int a hash code value for this object.
     */
    @Override
    public int hashCode() {
        return getPackageName().hashCode();
    }

    /**
     * String value of this AppInfo.
     * @return String containing this AppInfo's information.
     */
    @Override
    public String toString() {
        return "AppInfo{" + name + "}";
    }

    /**
     * Get package name.
     * @return String containing package name.
     */
    public String getPackageName() {
        return package_name;
    }

    /**
     * Set package name.
     * @param package_name String containing package name.
     */
    public void setPackageName(String package_name) {
        this.package_name = package_name;
    }

    /**
     * Get Application name.
     * @return String containing application name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set Application name.
     * @param name String containing application name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get Drawable object of Application Icon.
     * @return Drawable.
     */
    public Drawable getIcon() {
        return icon;
    }

    /**
     * Set Drawable object of Application Icon.
     * @param icon Drawable.
     */
    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    /**
     * Get category of this Application.
     * @return Category.
     */
    public int getCategory() {
        return category;
    }

    /**
     * Set category of this Application.
     * @param category Category.
     */
    public void setCategory(int category) {
        this.category = category;
    }

    /**
     * Check if this Application is selected.
     * @return True if is selected.
     */
    public boolean isSelected() {
        return selected;
    }

    /**
     * Set this Application selected.
     * @param selected boolean dictating if is selected.
     */
    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
