package com.alcachofra.elderoid.utils.weather;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.core.content.ContextCompat;

import java.time.LocalDate;

public class Weather {

    /**
     * Type of temperature scale for temperature values.
     */
    public enum TempScale {
        CELSIUS,
        FAHRENHEIT,
        KELVIN
    }

    private final LocalDate date;
    private final String weekday;
    private final int icon;
    private final String city;
    private final String description;
    private String id;

    /**
     * Constructor with no attributes for temperature. Use one of the Weather subclasses
     * (ForecastWeather or RealTimeWeather) to set temperature values, accordingly.
     * @param date Date of this Weather object, i.e. the day temperature values were measured.
     * @param weekday Day of week.
     * @param icon Integer value for icon representing this weather.
     * @param city Name of the city where weather is being measured.
     * @param description A short statement describing this weather.
     */
    public Weather(LocalDate date, String weekday, Integer icon, String city, String description) {
        this.date = date;
        this.weekday = weekday;
        this.icon = icon;
        this.city = city;
        this.description = description;
    }


    public String toString() {
        return "date = {" + date + "}; " +
                "weekday = {" + weekday + "}; " +
                "city = {" + city + "}; " +
                "description = {" + description + "}";
    }

    /**
     * Set optional identifier.
     * @param id Identifier
     */
    public Weather setIdentifier(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get optional identifier (must be set).
     * @return Identifier if it has been set. Null otherwise.
     */
    public String getIdentifier() {
        return id;
    }

    /**
     * Get icon.
     * @return Integer representing the respective icon drawable or null if it hasn't been set.
     */
    public Drawable getIcon(Context context) {
        return ContextCompat.getDrawable(context, icon);
    }

    /**
     * Get city name.
     * @return Name of the city where temperature is being measured or null if it hasn't been set.
     */
    public String getCity() {
        return city;
    }

    /**
     * Get description of weather.
     * @return Weather description or null if it hasn't been set.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Get date in which weather is being measured.
     * @return Date or null if it hasn't been set.
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Get day of the week in which weather is being measured.
     * @return Week day or null if it hasn't been set.
     */
    public String getWeekday() {
        return weekday;
    }
}
