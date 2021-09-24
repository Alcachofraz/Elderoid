package com.alcachofra.elderoid;

import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.widget.ImageViewCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.provider.MediaStore;
import android.provider.Settings;
import android.provider.Telephony;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.alcachofra.elderoid.configuration.ChooseAppsConfigurationActivity;
import com.alcachofra.elderoid.configuration.FeaturesConfigurationActivity;
import com.alcachofra.elderoid.configuration.LangConfigurationActivity;
import com.alcachofra.elderoid.configuration.LauncherConfigurationActivity;
import com.alcachofra.elderoid.configuration.PermissionsConfigurationActivity;
import com.alcachofra.elderoid.utils.CameraUtils;
import com.alcachofra.elderoid.utils.ElderoidActivity;
import com.alcachofra.elderoid.utils.animatedViews.AnimatedImageButton;
import com.alcachofra.elderoid.utils.dialog.DialogOption;
import com.alcachofra.elderoid.utils.netie.Cue;
import com.alcachofra.elderoid.utils.netie.Netie;
import com.alcachofra.elderoid.utils.SimplePrefs;
import com.alcachofra.elderoid.utils.weather.Weather;
import com.alcachofra.elderoid.utils.weather.WeatherManager;

import java.io.File;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.alcachofra.elderoid.utils.CameraUtils.MEDIA_TYPE_IMAGE;

public class MenuActivity extends ElderoidActivity {

    final int WEATHER_REQUEST_DELAY = 5; // Seconds
    final int REQUEST_LAUNCHER_CHOOSER = 2;

    Netie netie;

    AppCompatTextView temperature;
    AppCompatImageView weather_icon;
    ConstraintLayout battery_fill_constraint;
    AppCompatImageView battery_fill;
    AppCompatImageView bolt;
    AppCompatImageView loading;
    AnimatedImageButton flashlight;
    String weather_description = Elderoid.string(R.string.internet_needed);
    String temperature_description = Elderoid.string(R.string.internet_needed);

    DialogOption dialogConfig = null;

    boolean isCharging = false;
    int battery = -1;

    String imageStoragePath;

    boolean comingFromCamera = false;

    //ConstraintLayout notification_badge;
    //AppCompatTextView notification_text;

    private Camera cam1;
    private Camera.Parameters parameters;
    boolean flashOn1 = false;

    CameraManager cameraManager;
    String flashlightCamId;
    boolean flashOn2 = false;
    CameraManager.TorchCallback torchCallback = new CameraManager.TorchCallback() {
        @Override
        public void onTorchModeUnavailable(String cameraId) {
            super.onTorchModeUnavailable(cameraId);
        }

        @Override
        public void onTorchModeChanged(String cameraId, boolean enabled) {
            super.onTorchModeChanged(cameraId, enabled);
            if (cameraId.equals(flashlightCamId)) flashOn2 = enabled;
        }
    };

    LocationManager locationManager;
    LocationListener locationListener = new LocationListener() {
        public void onLocationChanged(Location location) {}

        public void onProviderDisabled(String provider) {}

        public void onProviderEnabled(String provider) {}

        public void onStatusChanged(String provider, int status, Bundle extras) {}
    };

    boolean connected_to_internet_and_gps = false;

    //runs without a timer by reposting this handler at the end of the runnable
    Handler timerHandler = new Handler();
    Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            updateWeather();
            timerHandler.postDelayed(this, WEATHER_REQUEST_DELAY * 1000);
        }
    };

    private final BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver(){
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1); // Battery level
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1); // Maximum value of battery level
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1); // Battery status
            isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL;
            battery = Math.round(level * 100 / (float)scale); // Battery percentage

            ConstraintSet set = new ConstraintSet();
            set.clone(battery_fill_constraint);

            set.constrainPercentWidth(R.id.battery_fill, (float) battery/100); // Set battery indicator width

            if (battery < 20) {
                ImageViewCompat.setImageTintList(battery_fill, ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.red))); // Red battery indicator
                netie.setBalloon(Elderoid.string(R.string.battery_low)) // Throw netie warning
                    .setExpression(R.drawable.netie_concerned);
            }
            else if (battery < 40) ImageViewCompat.setImageTintList(battery_fill, ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.orange))); // Orange battery indicator
            else if (battery < 60) ImageViewCompat.setImageTintList(battery_fill, ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.yellow))); // Yellow battery indicator
            else ImageViewCompat.setImageTintList(battery_fill, ColorStateList.valueOf(ContextCompat.getColor(getApplicationContext(), R.color.green))); // Green battery indicator

            set.applyTo(battery_fill_constraint);

            bolt.setVisibility(isCharging ? View.VISIBLE : View.INVISIBLE); // Set charging bolt visible
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initial switch case for Configuration Phase:
        switch(getConfigurationStage()) {
            case 0: // Nothing is done:
                SimplePrefs.clear();
                startActivity(new Intent(getApplicationContext(), LangConfigurationActivity.class));
                finish();
                return;
            case 1: // User has entered a username:
                startActivity(new Intent(getApplicationContext(), FeaturesConfigurationActivity.class));
                finish();
                return;
            case 2: // User has configured features:
                if (SimplePrefs.getBoolean(Elderoid.FUNC_APPS)) startActivity(new Intent(getApplicationContext(), ChooseAppsConfigurationActivity.class));
                else startActivity(new Intent(getApplicationContext(), PermissionsConfigurationActivity.class));
                finish();
                return;
            case 3: // User has allowed permissions:
                startActivity(new Intent(getApplicationContext(), LauncherConfigurationActivity.class));
                finish();
                return;
            default: // Launcher set. Configuration complete:
                break; // Stay on MenuActivity
        }

        setContentView(R.layout.activity_menu);




        // Ring Buttons:
        AppCompatImageButton call = findViewById(R.id.call);
        AppCompatImageButton camera = findViewById(R.id.camera);
        AppCompatImageButton gallery = findViewById(R.id.gallery);
        AppCompatImageButton apps = findViewById(R.id.apps);
        AppCompatImageButton messages = findViewById(R.id.messages);
        AppCompatImageButton contacts = findViewById(R.id.contacts);

        ArrayList<AppCompatImageButton> buttons = new ArrayList<>(); // List of buttons to be displayed
        ArrayList<Cue> cuePool = new ArrayList<>(); // List of cues to be displayed

        if (SimplePrefs.getBoolean(Elderoid.FUNC_CALLS)) { // Check for Calls
            buttons.add(call); // Check for Calls
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_call_log),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.see),
                    v -> openCallLog()
            ));
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_call),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> startActivity(new Intent(getApplicationContext(), DialerActivity.class))
            ));
        }
        else call.setVisibility(View.GONE);
        if (SimplePrefs.getBoolean(Elderoid.FUNC_CONTACTS)) { // Check for Contacts
            buttons.add(contacts); // Check for Contacts
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_contacts),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> enterContacts()
            ));
        }
        else contacts.setVisibility(View.GONE);
        if (SimplePrefs.getBoolean(Elderoid.FUNC_APPS)) { // Check for Apps
            buttons.add(apps); // Check for Apps
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_apps),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> enterApps()
            ));
        }
        else apps.setVisibility(View.GONE);
        if (SimplePrefs.getBoolean(Elderoid.FUNC_MESSAGES)) { // Check for Weather
            buttons.add(messages); // Check for Weather
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_messages),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> enterMessages()
            ));
        }
        else messages.setVisibility(View.GONE);
        if (SimplePrefs.getBoolean(Elderoid.FUNC_PHOTOS)) { // Check for Photos
            buttons.add(camera);
            buttons.add(gallery);
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_camera),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> enterCamera()
            ));
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_gallery),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.yes),
                    v -> startActivity(new Intent(getApplicationContext(), GalleryActivity.class))
            ));
        }
        else {
            camera.setVisibility(View.GONE);
            gallery.setVisibility(View.GONE);
        }
        if (SimplePrefs.getBoolean(Elderoid.FUNC_WEATHER)) { // Check for Weather
            cuePool.add(new Cue(
                    Elderoid.string(R.string.cue_weather),
                    R.drawable.netie
            ).setOption1(
                    Elderoid.string(R.string.see),
                    v -> enterWeatherForecast()
            ));
        }

        float angle = (float) 360 / buttons.size(); // Angle between buttons in the central button ring
        ConstraintLayout.LayoutParams layoutParams;

        for (int i = 0; i < buttons.size(); i++) {
            layoutParams = (ConstraintLayout.LayoutParams) buttons.get(i).getLayoutParams(); // Layout parameters for button
            layoutParams.circleAngle = ((buttons.size() == 2) ? 180 - angle/2 : 180) + i*angle; // Efficient button angle position
            // Affecting height, will affect width too, because every button's ratio is set to "1:1":
            layoutParams.matchConstraintPercentHeight = (float) ((0.34f / Math.pow((10.0f / 17.0f), (1.0f / 5.0f))) * Math.pow(Math.pow((10.0f / 17.0f), (1.0f / 5.0f)), buttons.size()));
            layoutParams.circleRadius = (int) (layoutParams.circleRadius * (2/Math.PI) * Math.atan(3 * (buttons.size() - 1)));
            buttons.get(i).setLayoutParams(layoutParams); // Update layout parameters
        }




        // Activity views:
        AnimatedImageButton disable = findViewById(R.id.disable);
        AnimatedImageButton gears = findViewById(R.id.gears);
        AnimatedImageButton call_log = findViewById(R.id.call_log);
        flashlight = findViewById(R.id.flashlight);
        View battery_view = findViewById(R.id.battery_view);
        battery_fill_constraint = findViewById(R.id.battery_fill_constraint);
        battery_fill = findViewById(R.id.battery_fill);
        bolt = findViewById(R.id.bolt);
        temperature = findViewById(R.id.temperature);
        weather_icon = findViewById(R.id.weather_icon);
        loading = findViewById(R.id.loading);
        //notification_badge = findViewById(R.id.notification_badge); // Messages notification badge
        //notification_text = findViewById(R.id.notification_text); // Messages notification text




        Animation rotate = AnimationUtils.loadAnimation(this, R.anim.loading);
        loading.startAnimation(rotate);




        //updateMessageNotificationBadge();




        // Netie:
        netie = new Netie(
                this,
                Netie.NetieWindowType.WITH_CLOCK,
                cuePool,
                false
        );




        // Prompt alert dialogs, if necessary:
        promptDialogs();




        // Greet user:
        greet(SimplePrefs.getString(Elderoid.USERNAME));




        // Corner Buttons:
        if (!SimplePrefs.getBoolean(Elderoid.FUNC_WEATHER)) {
            weather_icon.setVisibility(View.GONE);
            temperature.setVisibility(View.GONE);
        }
        if (!SimplePrefs.getBoolean(Elderoid.FUNC_FLASHLIGHT)) flashlight.setVisibility(View.GONE);
        if (!SimplePrefs.getBoolean(Elderoid.FUNC_CALLS)) call_log.setVisibility(View.GONE);



        // Initialise flash:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            cameraManager.registerTorchCallback(torchCallback, null);

            // Find available camera with flash:
            try {
                String[] cameras = cameraManager.getCameraIdList();
                for (String s : cameras) {
                    if (cameraManager.getCameraCharacteristics(s).get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                        flashlightCamId = s;
                    }
                }
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
        else {
            cam1 = Camera.open();
            parameters = cam1.getParameters();
        }




        // Register battery broadcast receiver (mBatInfoReceiver being the instance of BroadcastReceiver)
        this.registerReceiver(this.mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        // Start requesting weather:
        timerHandler.postDelayed(timerRunnable, 0);




        // Set view listeners:
        gears.setOnClickRunnable(() -> netie.setBalloon(Elderoid.string(R.string.entering_settings))
            .setExpression(R.drawable.netie_confused)
            .setOption1(Elderoid.string(R.string.yes), v1 -> enterSettings())
            .setOption2(Elderoid.string(R.string.no), v2 -> netie.setBalloon(Elderoid.string(R.string.refused_settings))
                .setExpression(R.drawable.netie)
            )
        );

        call_log.setOnClickRunnable(this::openCallLog);

        flashlight.setOnClickRunnable(() -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                try {
                    cameraManager.setTorchMode(flashlightCamId, !flashOn2);
                } catch (CameraAccessException e) {
                    e.printStackTrace();
                }
                flashlight.setActivated(!flashOn2);
            }
            else {
                if (flashOn1) {
                    flashOn1 = false;
                    switchFlashOff1();
                }
                else {
                    flashOn1 = true;
                    switchFlashOn1();
                }
            }
        });




        // Ring Buttons Listeners:
        call.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), DialerActivity.class)));
        camera.setOnClickListener(v -> enterCamera());
        gallery.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), GalleryActivity.class)));
        apps.setOnClickListener(v -> enterApps());
        messages.setOnClickListener(v -> enterMessages());
        contacts.setOnClickListener(v -> enterContacts());




        // Other listeners:
        weather_icon.setOnClickListener(v -> {
            netie.setBalloon(weather_description)
                    .setExpression(connected_to_internet_and_gps ? R.drawable.netie : R.drawable.netie_concerned);
            if (connected_to_internet_and_gps) netie.setOption1(Elderoid.string(R.string.see), view -> enterWeatherForecast());
        });

        temperature.setOnClickListener(v -> {
            netie.setBalloon(temperature_description)
                    .setExpression(connected_to_internet_and_gps ? R.drawable.netie : R.drawable.netie_concerned);
            if (connected_to_internet_and_gps) netie.setOption1(Elderoid.string(R.string.see), view -> enterWeatherForecast());
        });

        battery_view.setOnClickListener(v -> {
            if (battery >= 0) {
                netie.setBalloon(
                        String.format((
                                (battery < 10) ?
                                Elderoid.string(R.string.battery_status) + " " + Elderoid.string(R.string.battery_must_charge) :
                                Elderoid.string(R.string.battery_status)
                        ), battery)
                );
                if (battery < 10) netie.setExpression(R.drawable.netie_confused);
                if (battery < 90) netie.setExpression(R.drawable.netie);
                else netie.setExpression(R.drawable.netie_happy);
            }
        });

        disable.setVisibility(Elderoid.DEBUGGING ? View.VISIBLE : View.GONE);
        disable.setOnClickListener(v -> chooseLauncher());
    }

    private int getConfigurationStage() {
        return SimplePrefs.getInt(Elderoid.CONFIG_STAGE, 0);
    }

    private void greet(String name) {
        Random rnd = new Random();
        if (rnd.nextInt(4) == 1) {
            netie.setBalloon(Elderoid.string(R.string.i_can_help_you))
                    .setExpression(R.drawable.netie_happy);
            return;
        }

        int hour = LocalTime.now().getHour();
        if (hour < 5)
            netie.setBalloon(String.format(Elderoid.string(R.string.greeting_evening), name))
                    .setExpression(R.drawable.netie);
        else if (hour < 13)
            netie.setBalloon(String.format(Elderoid.string(R.string.greeting_morning), name))
                    .setExpression(R.drawable.netie);
        else if (hour < 20)
            netie.setBalloon(String.format(Elderoid.string(R.string.greeting_afternoon), name))
                    .setExpression(R.drawable.netie);
        else eveningGreet(name);
    }

    private void eveningGreet(String name) {
        LocalDate today = LocalDate.now();
        String current_day = today.format(DateTimeFormatter.ofPattern(Elderoid.DATE_FORMAT));
        if (!SimplePrefs.getString(Elderoid.CURRENT_DAY).equalsIgnoreCase(current_day)) {
            netie.setBalloon(String.format(Elderoid.string(R.string.greeting_evening) + " " + Elderoid.string(R.string.how_was_today), name))
                    .setOption1(Elderoid.string(R.string.good), view -> {
                        netie.setBalloon(Elderoid.string(R.string.day_went_well))
                                .setExpression(R.drawable.netie_happy);
                        SimplePrefs.putString(Elderoid.CURRENT_DAY, current_day);
                    })
                    .setOption2(Elderoid.string(R.string.bad), view -> {
                        netie.setBalloon(Elderoid.string(R.string.day_went_bad))
                                .setExpression(R.drawable.netie_sad);
                        SimplePrefs.putString(Elderoid.CURRENT_DAY, current_day);
                    })
                    .setExpression(R.drawable.netie);
        }
        else netie.setBalloon(String.format(Elderoid.string(R.string.greeting_evening), name))
                .setExpression(R.drawable.netie);
    }

    private void promptDialogs() {
        if (!isDefaultLauncher() && dialogConfig == null) {
            dialogConfig = new DialogOption(Elderoid.string(R.string.dialog_not_configured),
                    Elderoid.string(R.string.dialog_not_configured_message),
                    Elderoid.string(R.string.dialog_configure),
                    Elderoid.string(R.string.dialog_cancel),
                    (d, w) -> {
                        chooseLauncher();
                        d.cancel();
                        dialogConfig = null;
                    },
                    (d, w) -> {
                        d.cancel();
                        dialogConfig = null;
                    });
            dialogConfig.setImage(R.drawable.ic_warning);
            dialogConfig.show(getSupportFragmentManager(), "Not Configured Dialog");
        }

        if (getBattery(this) < 15) {
            if (!SimplePrefs.getBoolean(Elderoid.DISABLE_DIALOG_BATTERY, false)) {
                DialogOption dialog = new DialogOption(Elderoid.string(R.string.dialog_low_battery),
                        Elderoid.string(R.string.dialog_charge_battery),
                        Elderoid.string(R.string.dialog_got_it),
                        Elderoid.string(R.string.dialog_do_not_tell_me_again),
                        (d, w) -> {},
                        (d, w) -> {
                            SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_BATTERY, true);
                            d.cancel();
                        });
                dialog.setImage(R.drawable.ic_warning);
                dialog.show(getSupportFragmentManager(), "Weak Battery Dialog");
            }
        }
        else SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_BATTERY, false);

        if (SimplePrefs.getBoolean(Elderoid.FUNC_WEATHER)) {
            if (!isNetworkAvailable()) {
                if (!SimplePrefs.getBoolean(Elderoid.DISABLE_DIALOG_INTERNET, false)) {
                    DialogOption dialog = new DialogOption(Elderoid.string(R.string.dialog_no_connection),
                            Elderoid.string(R.string.dialog_no_connection_message),
                            Elderoid.string(R.string.dialog_got_it),
                            Elderoid.string(R.string.dialog_do_not_tell_me_again),
                            (d, w) -> {},
                            (d, w) -> {
                                SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_INTERNET, true);
                                d.cancel();
                            });
                    dialog.setImage(R.drawable.ic_warning);
                    dialog.show(getSupportFragmentManager(), "Network Dialog");
                }
            }
            else SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_INTERNET, false);

            if (!isGPSEnabled()) {
                System.out.println("Olá");
                if (!SimplePrefs.getBoolean(Elderoid.DISABLE_DIALOG_GPS, false)) {
                    DialogOption dialog = new DialogOption(Elderoid.string(R.string.dialog_no_gps),
                            Elderoid.string(R.string.dialog_no_gps_message),
                            Elderoid.string(R.string.dialog_got_it),
                            Elderoid.string(R.string.dialog_do_not_tell_me_again),
                            (d, w) -> {},
                            (d, w) -> {
                                SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_GPS, true);
                                d.cancel();
                            });
                    dialog.setImage(R.drawable.ic_warning);
                    dialog.show(getSupportFragmentManager(), "GPS Dialog");
                }
            }
            else SimplePrefs.putBoolean(Elderoid.DISABLE_DIALOG_GPS, false);
        }
    }

    private Location getLastKnownLocation() {
        locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location location = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, Elderoid.PERMISSIONS);
        }
        for (String provider : providers) {
            locationManager.requestLocationUpdates(provider, 1000, 0, locationListener);
            location = locationManager.getLastKnownLocation(provider);
            if (location != null) break;
        }
        locationManager.removeUpdates(locationListener);
        return location;
    }

    private void switchFlashOn1() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cam1.setParameters(parameters);
        cam1.startPreview();
        flashOn1 = true;
    }

    private void switchFlashOff1() {
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        cam1.setParameters(parameters);
        cam1.stopPreview();
        flashOn1 = false;
    }

    public static int getBattery(Context context) {
        if (Build.VERSION.SDK_INT >= 21) {
            BatteryManager bm = (BatteryManager) context.getSystemService(BATTERY_SERVICE);
            return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        } else {
            IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
            Intent batteryStatus = context.registerReceiver(null, iFilter);

            int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
            int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

            double batteryPct = level / (double) scale;
            return (int) (batteryPct * 100);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivity = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            return false;
        } else {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            for (NetworkInfo networkInfo : info) {
                if (networkInfo.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isGPSEnabled() {
        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        System.out.println(manager.isProviderEnabled(LocationManager.GPS_PROVIDER));
        return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    private void updateWeather() {
        Location location = getLastKnownLocation();

        if (location == null) { // GPS failed
            loading.clearAnimation();
            loading.setVisibility(View.GONE);
            weather_description = Elderoid.string(R.string.temp_not_working);
            temperature_description = Elderoid.string(R.string.temp_not_working);
            temperature.setText(null);
            weather_icon.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_weather_error));
            connected_to_internet_and_gps = false;
            return;
        }

        WeatherManager.requestRealTime(this, location.getLatitude(), location.getLongitude(), weather -> {
            loading.clearAnimation();
            loading.setVisibility(View.GONE);
            connected_to_internet_and_gps = true;
            String degrees = weather.getCurrentTemperature(SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? Weather.TempScale.CELSIUS : Weather.TempScale.FAHRENHEIT)
                    + (SimplePrefs.getBoolean(Elderoid.IS_CELSIUS) ? " \u2103" : " \u2109");
            weather_description = weather.getDescription() + ". " + Elderoid.string(R.string.want_to_see_tomorrow_weather);
            temperature_description = String.format(Elderoid.string(R.string.weather_temperature), degrees) + ". " + Elderoid.string(R.string.want_to_see_tomorrow_weather);
            temperature.setText(degrees);
            weather_icon.setBackgroundDrawable(weather.getIcon(this));
        }, error -> {
            loading.clearAnimation();
            loading.setVisibility(View.GONE);
            connected_to_internet_and_gps = false;
            weather_description = Elderoid.string(R.string.temp_not_working);
            temperature_description = Elderoid.string(R.string.temp_not_working);
            temperature.setText(null);
            weather_icon.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.ic_weather_error));
        });
    }

    /*void updateMessageNotificationBadge() {
        final Uri SMS_INBOX = Uri.parse("content://sms/inbox");
        Cursor c = getContentResolver().query(SMS_INBOX, null, "read = 0", null, null);
        int unreadMessagesCount = c.getCount();
        c.close();
        if (unreadMessagesCount > 0) {
            notification_badge.setVisibility(View.VISIBLE);
            notification_text.setText(String.valueOf(unreadMessagesCount));
        } else notification_badge.setVisibility(View.GONE);
    }*/

    private void enterWeatherForecast() {
        if (connected_to_internet_and_gps) startActivity(new Intent(getApplicationContext(), WeatherForecastActivity.class));
        else {
            netie.setBalloon(Elderoid.string(R.string.temp_not_working))
                    .setExpression(R.drawable.netie_concerned);
        }
    }

    public void enterCamera() {
        if (CameraUtils.isCameraAvailable(this)) captureImage();
        else netie.setBalloon(Elderoid.string(R.string.camera_not_working))
                .setExpression(R.drawable.netie_concerned);
    }

    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File file = CameraUtils.getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (file != null) {
            imageStoragePath = file.getAbsolutePath();
        }

        Uri fileUri = CameraUtils.getOutputMediaFileUri(getApplicationContext(), file);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        comingFromCamera = true;

        // start the image capture Intent
        startActivityForResult(intent, CameraUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }

    private void enterContacts() {
        startActivity(new Intent(getApplicationContext(), ContactListActivity.class));
    }

    private void enterApps() {
        startActivity(new Intent(getApplicationContext(), AppListActivity.class));
    }

    private void enterMessages() {
        String defaultSmsPackage = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
                ? Telephony.Sms.getDefaultSmsPackage(this)
                : Settings.Secure.getString(getContentResolver(), "sms_default_application");

        Intent smsIntent = getPackageManager().getLaunchIntentForPackage(defaultSmsPackage);

        if (smsIntent == null) {
            smsIntent = new Intent(Intent.ACTION_MAIN);
            smsIntent.addCategory(Intent.CATEGORY_DEFAULT);
            smsIntent.setType("vnd.android-dir/mms-sms");
        }

        try {
            startActivity(smsIntent);
        } catch (Exception e) {
            netie.setBalloon(R.string.messages_error)
                    .setExpression(R.drawable.netie_confused);
        }
    }

    private void enterSettings() {
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        finish();
    }

    private void openCallLog() {
        Intent showCallLog = new Intent();
        showCallLog.setAction(Intent.ACTION_VIEW);
        showCallLog.setType(CallLog.Calls.CONTENT_TYPE);
        startActivity(showCallLog);
    }

    private boolean isPackageInstalled(String packageName, Context context) {
        try {
            context.getPackageManager().getPackageInfo(packageName, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    private boolean isDefaultLauncher() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        ResolveInfo resolveInfo = getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
        String currentHomePackage = resolveInfo.activityInfo.packageName;

        return getPackageName().equals(currentHomePackage);
    }

    private void chooseLauncher() {
        PackageManager p = getPackageManager();
        Intent preferredApps = new Intent("com.android.settings.PREFERRED_SETTINGS");
        preferredApps.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        if (p.resolveActivity(preferredApps, 0) != null) {
            startActivity(preferredApps);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                startActivity(new Intent(Settings.ACTION_HOME_SETTINGS));
            }
            else {
                ComponentName cN = new ComponentName(getApplicationContext(), FakeLauncherActivity.class);
                p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

                Intent selector = new Intent(Intent.ACTION_MAIN);
                selector.addCategory(Intent.CATEGORY_HOME);
                selector.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(selector);

                p.setComponentEnabledSetting(cN, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case CameraUtils.CAMERA_CAPTURE_IMAGE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    CameraUtils.refreshGallery(getApplicationContext(), imageStoragePath);
                    netie.setBalloon(Elderoid.string(R.string.photo_saved_menu))
                            .setOption1(Elderoid.string(R.string.see), view -> startActivity(new Intent(getApplicationContext(), GalleryActivity.class)))
                            .setExpression(R.drawable.netie);
                }
                else if (resultCode == RESULT_CANCELED)
                    netie.setBalloon(Elderoid.string(R.string.photo_canceled))
                            .setExpression(R.drawable.netie_confused);
                else
                    netie.setBalloon(Elderoid.string(R.string.photo_save_error))
                            .setExpression(R.drawable.netie_concerned);
                break;
            case REQUEST_LAUNCHER_CHOOSER:
                if (resultCode != RESULT_OK) {
                    netie.setBalloon(Elderoid.string(R.string.config_error))
                            .setExpression(R.drawable.netie_concerned);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onPause() {
        super.onPause();
        timerHandler.removeCallbacks(timerRunnable);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.unregisterTorchCallback(torchCallback);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            if (cam1 != null) {
                cam1.release();
                cam1 = null;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Resume requesting weather:
        timerHandler.postDelayed(timerRunnable, 0);
        // Resume torch callbacks:
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cameraManager.registerTorchCallback(torchCallback, null);
        }

        if (!comingFromCamera) {
            greet(SimplePrefs.getString(Elderoid.USERNAME));
        }
        comingFromCamera = false;

        //updateMessageNotificationBadge();

        promptDialogs();
    }
}