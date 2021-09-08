package com.alcachofra.elderoid.utils.weather;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.alcachofra.elderoid.Elderoid;
import com.alcachofra.elderoid.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

public class WeatherManager {

    /**
     * Boolean flag to forecast present day.
     * Example:
     *  If set to true:
     *      If today is friday, the 4 forecast days will be: Friday, Saturday, Sunday and Monday.
     *  If set to false:
     *      If today is friday, the 4 forecast days will be: Saturday, Sunday, Monday and Thursday.
     */
    private static final boolean FORECAST_PRESENT_DAY = false;

    /**
     * Weather stereotypes.
     */
    public enum WeatherType {
        GOOD_WEATHER, // Sunny, clear...
        NORMAL_WEATHER, // Cloudy, dark clouds...
        BAD_WEATHER, // Rainy, stormy...
        DUBIOUS_WEATHER // Foggy, snowy...
    }

    /**
     * Fields related to Weather API.
     */
    private static final String WEATHER_API_KEY = "da7df506216273d82f53ca32caff1bda";
    private static final String FORECAST_REQUEST = "https://api.openweathermap.org/data/2.5/forecast?";
    private static final String CURRENT_REQUEST = "https://api.openweathermap.org/data/2.5/weather?";

    /**
     * Request present weather (right now). When successful, executes function onSuccess receiving a NowWeather instance.
     * @param activity Activity from which the request is being made.
     * @param latitude Latitude value.
     * @param longitude Longitude value.
     * @param onSuccess Function to execute on Success. Receives NowWeather object as parameter.
     * @param onError Function to execute if an error occurs.
     */
    public static void requestRealTime(Activity activity, double latitude, double longitude, Consumer<NowWeather> onSuccess, Consumer<VolleyError> onError) {
        String url = CURRENT_REQUEST + "lat=" + latitude + "&lon=" + longitude + "&appid=" + WEATHER_API_KEY; // Request
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            // On Success:
            try {
                double current_temperature = response.getJSONObject("main").getDouble("temp");
                String city = response.getString("name");
                String icon = response.getJSONArray("weather").getJSONObject(0).getString("icon").substring(0, 2);
                LocalDate now = LocalDate.now();
                onSuccess.accept((NowWeather) new NowWeather(
                        now,
                        getWeekday(now),
                        getIcon(icon),
                        city,
                        getDescription(icon),
                        current_temperature,
                        Weather.TempScale.KELVIN
                ).setIdentifier(icon));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, onError::accept);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(jor);
    }

    /**
     * Request forecast for the next days. When successful, executes function onSuccess receiving a list of DayWeather instances.
     * @param activity Activity from which the request is being made.
     * @param latitude Latitude value.
     * @param longitude Longitude value.
     * @param onSuccess Function to execute on Success. Receives list of DayWeather objects as parameter.
     * @param onError Function to execute if an error occurs.
     */
    public static void requestForecast(Activity activity, double latitude, double longitude, Consumer<List<DayWeather>> onSuccess, Consumer<VolleyError> onError) {
        String url = FORECAST_REQUEST + "lat=" + latitude + "&lon=" + longitude + "&appid=" + WEATHER_API_KEY;
        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            try {
                String city = response.getJSONObject("city").getString("name"); // City
                JSONArray list = response.getJSONArray("list"); // Get list containing forecasts every 3 hours

                // Containers:
                List<DayWeather> weathers = new ArrayList<>(); // Where Weather instances for each day will be stored
                List<Double> max = new ArrayList<>(); // Maximum temperatures forecasted for every 3 hours
                List<Double> min = new ArrayList<>(); // Minimum temperatures forecasted for every 3 hours
                List<String> icons = new ArrayList<>(); // Icon received every 3 hours (in order to conclude the most frequent for each day)

                LocalDate now = LocalDate.now(); // Present date
                LocalDate iteration_date = now; // Current date in iteration out of all the days forecasted
                int previous_iteration_day = now.getDayOfMonth(); // Day of previous loop iteration

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                for (int i = 0; i < list.length(); i++) { // Go through forecasts every 3 hours
                    LocalDate date = LocalDate.parse(list.getJSONObject(i).getString("dt_txt"), formatter); // Date of forecast

                    if (!FORECAST_PRESENT_DAY && date.getDayOfMonth() == now.getDayOfMonth() && date.getMonth() == now.getMonth() && date.getYear() == now.getYear()) { // Ignore the current day. We want to see the forecast
                        continue; // Skip iteration, if present day && flag FORECAST_PRESENT_DAY is false
                    }

                    if (date.getDayOfMonth() != previous_iteration_day) { // If day is different from last iteration (i.e. loop has reached forecasts for a new day)
                        if (FORECAST_PRESENT_DAY || previous_iteration_day != 0) { // flag FORECAST_PRESENT_DAY is set, or last iteration was not the present day
                            if (!max.isEmpty() && !min.isEmpty() && !icons.isEmpty()) { // Containers are not empty:
                                // Add weather to weathers container:
                                double max_temperature = Collections.max(max); // Get maximum temperature for the day
                                double min_temperature = Collections.min(min); // Get minimum temperature for the day
                                String icon = mostCommon(icons); // Get most frequent icon for the day
                                if (icon == null) icon = "50"; // Default value, if mostCommon() returns null, for some reason
                                weathers.add((DayWeather) new DayWeather(iteration_date,
                                        getWeekday(iteration_date),
                                        getIcon(icon),
                                        city,
                                        getForecastDescription(icon),
                                        max_temperature,
                                        min_temperature,
                                        Weather.TempScale.KELVIN
                                ).setIdentifier(icon));

                                // New day, clear containers:
                                max.clear();
                                min.clear();
                                icons.clear();
                            }
                        }
                        iteration_date = date; // Update iteration date (it's out of the if() because even when "previous_iteration_day" is 0 and flag FORECAST_PRESENT_DAY is not set, we need to update the date)
                    }
                    max.add(list.getJSONObject(i).getJSONObject("main").getDouble("temp_max")); // Add maximum temperature for these forecasted 3 hours
                    min.add(list.getJSONObject(i).getJSONObject("main").getDouble("temp_min")); // Add minimum temperature for these forecasted 3 hours
                    icons.add(list.getJSONObject(i).getJSONArray("weather").getJSONObject(0).getString("icon").substring(0, 2)); // Add icon without "d" or "n"

                    previous_iteration_day = date.getDayOfMonth(); // Update previous_iteration_day
                }
                onSuccess.accept(weathers);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, onError::accept);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(jor);
    }

    /**
     * Get String containing weekday, from a given LocalDate object.
     * @param date LocalDate object.
     * @return String containing weekday.
     */
    private static String getWeekday(LocalDate date) {
        if (date == null) return null;
        switch(date.getDayOfWeek()) {
            case MONDAY:
                return Elderoid.string(R.string.monday);
            case TUESDAY:
                return Elderoid.string(R.string.tuesday);
            case WEDNESDAY:
                return Elderoid.string(R.string.wednesday);
            case THURSDAY:
                return Elderoid.string(R.string.thursday);
            case FRIDAY:
                return Elderoid.string(R.string.friday);
            case SATURDAY:
                return Elderoid.string(R.string.saturday);
            default:
                return Elderoid.string(R.string.sunday);
        }
    }

    /**
     * General method to find a mostCommon element in a List.
     * @param list Given list.
     * @param <T> Type of list elements.
     * @return Most common element.
     */
    private static <T> T mostCommon(List<T> list) {
        Map<T, Integer> map = new HashMap<>();

        for (T t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }

        Entry<T, Integer> max = null;

        for (Entry<T, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() > max.getValue())
                max = e;
        }

        return max.getKey();
    }

    /**
     * Get Drawable Resource integer (icon to represent weather), given the weather id.
     * @param id Weather ID
     * @return Integer for Drawable Resource icon.
     */
    private static int getIcon(String id) {
        switch(id) {
            case "01":
                return R.drawable.ic_weather_sunny;
            case "02":
                return R.drawable.ic_weather_sunny_cloudy;
            case "03":
                return R.drawable.ic_weather_cloudy;
            case "04":
                return R.drawable.ic_weather_heavy_cloudy;
            case "09":
                return R.drawable.ic_weather_rainy;
            case "10":
                return R.drawable.ic_weather_sunny_rainy;
            case "11":
                return R.drawable.ic_weather_stormy;
            case "13":
                return R.drawable.ic_weather_snowy;
            default:
                return R.drawable.ic_weather_misty;
        }
    }

    /**
     * Get a brief description of the Weather, given the Weather ID.
     * @param id Weather ID.
     * @return String containing description.
     */
    private static String getDescription (String id) {
        switch(id) {
            case "01":
                return Elderoid.string(R.string.weather_sunny);
            case "02":
                return Elderoid.string(R.string.weather_sunny_cloudy);
            case "03":
                return Elderoid.string(R.string.weather_cloudy);
            case "04":
                return Elderoid.string(R.string.weather_heavy_cloudy);
            case "09":
                return Elderoid.string(R.string.weather_rainy);
            case "10":
                return Elderoid.string(R.string.weather_sunny_rainy);
            case "11":
                return Elderoid.string(R.string.weather_stormy);
            case "13":
                return Elderoid.string(R.string.weather_snowy);
            default:
                return Elderoid.string(R.string.weather_misty);
        }
    }

    /**
     * Get a brief description of the Weather forecast, given the Weather ID.
     * @param id Weather ID.
     * @return String containing description.
     */
    private static String getForecastDescription (String id) {
        switch(id) {
            case "01":
                return Elderoid.string(R.string.forecast_sunny);
            case "02":
                return Elderoid.string(R.string.forecast_sunny_cloudy);
            case "03":
                return Elderoid.string(R.string.forecast_cloudy);
            case "04":
                return Elderoid.string(R.string.forecast_heavy_cloudy);
            case "09":
                return Elderoid.string(R.string.forecast_rainy);
            case "10":
                return Elderoid.string(R.string.forecast_sunny_rainy);
            case "11":
                return Elderoid.string(R.string.forecast_stormy);
            case "13":
                return Elderoid.string(R.string.forecast_snowy);
            default:
                return Elderoid.string(R.string.forecast_misty);
        }
    }

    /**
     * Get type of weather, according to Weather Stereotypes enum WeatherType.
     * @param weather Weather instance.
     * @return Weather Type constant.
     */
    public static WeatherType typeOfWeather(Weather weather) {
        switch(weather.getIdentifier()) {
            case "01":
            case "02":
                return WeatherType.GOOD_WEATHER;
            case "03":
            case "04":
                return WeatherType.NORMAL_WEATHER;
            case "10":
            case "09":
            case "11":
                return WeatherType.BAD_WEATHER;
            default:
                return WeatherType.DUBIOUS_WEATHER;
        }
    }
}
