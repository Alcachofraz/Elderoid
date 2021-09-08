package com.alcachofra.elderoid.utils.weather;

import java.time.LocalDate;

public class NowWeather extends Weather {

    private final Double current_temperature;

    /**
     * Constructor for weather that has been forecast.
     * @param date Date of this Weather object, i.e. the day temperature values were measured.
     * @param weekday Day of week.
     * @param icon Integer value for icon representing this weather.
     * @param city Name of the city where weather is being measured.
     * @param description A short statement describing this weather.
     * @param current_temperature Current temperature value.
     * @param scale Type of temperature scale max_temperature is in.
     */
    public NowWeather(LocalDate date, String weekday, Integer icon, String city, String description, Double current_temperature, TempScale scale) {
        super(date, weekday, icon, city, description);
        switch (scale) {
            case CELSIUS:
                this.current_temperature = current_temperature + 273.15;
                break;
            case FAHRENHEIT:
                this.current_temperature = (current_temperature - 32) / 1.8 + 273.15;
                break;
            case KELVIN:
                this.current_temperature = current_temperature;
                break;
            default:
                this.current_temperature = null;
                break;
        }
    }

    public String toString() {
        return "Real-Time Temperature = {" + current_temperature + " K}; " +
                super.toString();
    }

    /**
     * Get current temperature, choosing the temperature scale it's in.
     * @param scale Type of temperature scale in which the value will be returned.
     * @return Temperature value or null if it hasn't been set.
     */
    public Integer getCurrentTemperature(TempScale scale) {
        if (current_temperature == null) return null;
        switch (scale) {
            case CELSIUS:
                return (int) Math.round(current_temperature - 273.15);
            case FAHRENHEIT:
                return (int) Math.round((current_temperature - 273.15) * 1.8 + 32);
            case KELVIN:
                return (int) Math.round(current_temperature);
            default:
                return null;
        }
    }
}
