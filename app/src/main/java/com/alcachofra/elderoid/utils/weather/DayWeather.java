package com.alcachofra.elderoid.utils.weather;

import java.time.LocalDate;

public class DayWeather extends Weather {

    private final Double max_temperature;
    private final Double min_temperature;

    /**
     * Constructor for weather that has been forecast.
     * @param date Date of this Weather object, i.e. the day temperature values were measured.
     * @param weekday Day of week.
     * @param icon Integer value for icon representing this weather.
     * @param city Name of the city where weather is being measured.
     * @param description A short statement describing this weather.
     * @param max_temperature Maximum temperature value.
     * @param min_temperature Minimum temperature value.
     * @param scale Type of temperature scale max_temperature and min_temperature is in.
     */
    public DayWeather(LocalDate date, String weekday, Integer icon, String city, String description, Double max_temperature, Double min_temperature, TempScale scale) {
        super(date, weekday, icon, city, description);
        switch (scale) {
            case CELSIUS:
                this.max_temperature = max_temperature + 273.15;
                this.min_temperature = min_temperature + 273.15;
                break;
            case FAHRENHEIT:
                this.max_temperature = (max_temperature - 32) / 1.8 + 273.15;
                this.min_temperature = (min_temperature - 32) / 1.8 + 273.15;
                break;
            case KELVIN:
                this.max_temperature = max_temperature;
                this.min_temperature = min_temperature;
                break;
            default:
                this.max_temperature = null;
                this.min_temperature = null;
                break;
        }
    }

    public String toString() {
        return "Max Temperature = {" + max_temperature + " K}; " +
                "Min Temperature = {" + min_temperature + " K}; " +
                super.toString();
    }

    /**
     * Get maximum temperature, i.e. highest temperature value forecasted for the day, choosing the
     * temperature scale it's in.
     * @param scale Type of temperature scale in which the value will be returned.
     * @return Temperature value or null if it hasn't been set.
     */
    public Integer getMaxTemperature(TempScale scale) {
        if (max_temperature == null) return null;
        switch (scale) {
            case CELSIUS:
                return (int) Math.round(max_temperature - 273.15);
            case FAHRENHEIT:
                return (int) Math.round((max_temperature - 273.15) * 1.8 + 32);
            case KELVIN:
                return (int) Math.round(max_temperature);
            default:
                return null;
        }
    }

    /**
     * Get minimum temperature, i.e. lowest temperature value forecasted for the day, choosing the
     * temperature scale it's in.
     * @param scale Type of temperature scale in which the value will be returned.
     * @return Temperature value or null if it hasn't been set.
     */
    public Integer getMinTemperature(TempScale scale) {
        if (min_temperature == null) return null;
        switch (scale) {
            case CELSIUS:
                return (int) Math.round(min_temperature - 273.15);
            case FAHRENHEIT:
                return (int) Math.round((min_temperature - 273.15) * 1.8 + 32);
            case KELVIN:
                return (int) Math.round(min_temperature);
            default:
                return null;
        }
    }
}
