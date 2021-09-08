package com.alcachofra.elderoid;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;
import com.alcachofra.elderoid.utils.weather.Weather;
import com.alcachofra.elderoid.utils.weather.WeatherManager;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class WeatherForecastActivity extends ElderoidActivity {

    Netie netie;
    Weather[] weekday_weathers = new Weather[4];
    LocationManager locationManager;

    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {}

        public void onProviderDisabled(String provider) {}

        public void onProviderEnabled(String provider) {}

        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather_forecast);



        ConstraintLayout day1 = findViewById(R.id.day1);
        ConstraintLayout day2 = findViewById(R.id.day2);
        ConstraintLayout day3 = findViewById(R.id.day3);
        ConstraintLayout day4 = findViewById(R.id.day4);
        AppCompatTextView[] weekdays = new AppCompatTextView[] {findViewById(R.id.text_weekday1), findViewById(R.id.text_weekday2), findViewById(R.id.text_weekday3),  findViewById(R.id.text_weekday4)};
        AppCompatImageView[] icons = new AppCompatImageView[] {findViewById(R.id.icon_day1), findViewById(R.id.icon_day2), findViewById(R.id.icon_day3),  findViewById(R.id.icon_day4)};
        AppCompatTextView[] texts = new AppCompatTextView[] {findViewById(R.id.text_day1), findViewById(R.id.text_day2), findViewById(R.id.text_day3),  findViewById(R.id.text_day4)};

        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_HOME_BUTTON,
                Arrays.asList(
                        new Cue(Elderoid.string(R.string.forecast_1), R.drawable.netie),
                        new Cue(Elderoid.string(R.string.forecast_2), R.drawable.netie)
                ),
                false
        );

        day1.setOnClickListener(v -> setNetieWindow(0));

        day2.setOnClickListener(v -> setNetieWindow(1));

        day3.setOnClickListener(v -> setNetieWindow(2));

        day4.setOnClickListener(v -> setNetieWindow(3));

        Location location = getLastKnownLocation();
        if (location == null) { // GPS failed
            return;
        }

        WeatherManager.requestForecast(this, location.getLatitude(), location.getLongitude(), weathers -> {
            for (int i = 0; i < Math.min(weathers.size(), 4); i++) { // 4 is the maximum number of days we can predict
                weekdays[i].setText(weathers.get(i).getDate().equals(LocalDate.now()) ? Elderoid.string(R.string.today) : weathers.get(i).getWeekday());
                icons[i].setBackgroundDrawable(weathers.get(i).getIcon(this));
                String temp = weathers.get(i).getMinTemperature(SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? Weather.TempScale.CELSIUS : Weather.TempScale.FAHRENHEIT)
                        + (SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? " \u2103" : " \u2109")
                        + " - "
                        + weathers.get(i).getMaxTemperature(SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? Weather.TempScale.CELSIUS : Weather.TempScale.FAHRENHEIT)
                        + (SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? " \u2103" : " \u2109");
                texts[i].setText(temp);
                weekday_weathers[i] = weathers.get(i);
            }
        }, volleyError -> netie.setBalloon(Elderoid.string(R.string.temp_not_working))
                                    .setExpression(R.drawable.netie_concerned)
        );
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, Elderoid.PERMISSIONS);
        }
        else {
            for (String provider : providers) {
                locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
                location = locationManager.getLastKnownLocation(provider);
                if (location != null) break;
            }
            locationManager.removeUpdates(locationListener);
        }
        return location;
    }

    private void setNetieWindow(int index) {
        netie.setBalloon(
                String.format(
                        weekday_weathers[index].getDescription() + ".",
                        String.format(
                                "%1.20s",
                                weekday_weathers[index].getWeekday() + (weekday_weathers[index].getDate().equals(LocalDate.now()) ? " (Hoje)" : "")
                        )
                )
        );
        switch (WeatherManager.typeOfWeather(weekday_weathers[index]))  {
            case GOOD_WEATHER:
                netie.setExpression(R.drawable.netie_happy);
                break;
            case NORMAL_WEATHER:
                netie.setExpression(R.drawable.netie);
                break;
            case BAD_WEATHER:
                netie.setExpression(R.drawable.netie_concerned);
                break;
            case DUBIOUS_WEATHER:
                netie.setExpression(R.drawable.netie_confused);
                break;
        }
    }
}