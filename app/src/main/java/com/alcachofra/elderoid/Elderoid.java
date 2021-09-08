package com.alcachofra.elderoid;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.provider.CallLog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;

import androidx.annotation.StringRes;

import com.alcachofra.elderoid.utils.CallInfo;
import com.alcachofra.elderoid.utils.SimplePrefs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static android.provider.CallLog.Calls.INCOMING_TYPE;
import static android.provider.CallLog.Calls.OUTGOING_TYPE;

public class Elderoid extends Application {

    public static final boolean DEBUGGING = false;
    public static final String LANGUAGE = "language"; // Used in Date (getMissedCalls()) and News API (NewsManager)

    // SimplePrefs:
    public static final String APP_NUM = "app_num"; // Number of apps currently stored in SimplePrefs
    public static final String APP_NUM_MAX = "app_num_max"; // Keeps record of the maximum number of apps that have been stored in SimplePrefs
    public static final String APP = "app";
    public static final String TOPIC_NUM = "topic_num"; // Number of topics currently stored in SimplePrefs
    public static final String TOPIC_NUM_MAX = "topic_num_max"; // Keeps record of the maximum number of topics that have been stored in SimplePrefs
    public static final String TOPIC_STATE = "topic_state"; // Topic state (selected or not)
    public static final String TOPIC = "topic"; // Topic name
    public static final String IMAGE_FILE_NAME = "image_file_name";
    public static final String USERNAME = "username";
    public static final String CONFIG_STAGE = "configuration_stage";
    public static final String FUNC_CALLS = "func_calls";
    public static final String FUNC_CONTACTS = "func_contacts";
    public static final String FUNC_APPS = "func_apps";
    public static final String FUNC_WEATHER = "func_messages";
    public static final String FUNC_MESSAGES = "func_messages";
    public static final String FUNC_PHOTOS = "func_photos";
    public static final String FUNC_FLASHLIGHT = "func_flashlight";
    public static final String IS_CELSIUS = "is_celsius";
    public static final String CURRENT_DAY = "current_day";
    public static final String CURRENT_DAY_FORMAT = "00-00-0000";
    public static final String DATE_FORMAT = "dd-MM-yyyy";
    public static final String MISSED_CALLS_COUNT = "missed_calls_count";
    public static final String COMING_FROM = "coming_from";
    public static final String ADDED_CONTACT = "added_contact";
    public static final String DISABLE_DIALOG_BATTERY = "disable_dialog_battery";
    public static final String DISABLE_DIALOG_INTERNET = "disable_dialog_internet";
    public static final String DISABLE_DIALOG_GPS = "disable_dialog_gps";

    public static final int PERMISSIONS = 1;

    private static Elderoid instance;

    public static Elderoid getInstance() {
        return instance;
    }

    public static Context getContext() {
        return instance.getApplicationContext();
    }

    @Override
    public void onCreate() {
        instance = this;

        // Initialise Shared Preferences:
        new SimplePrefs.Builder()
                .setPrefsName("elderoid")
                .setContext(this)
                .setMode(MODE_PRIVATE)
                .setDefaultUse(false)
                .build();

        updateLanguage(getLanguage());

        super.onCreate();
    }

    public static void updateLanguage(String lang) {
        SimplePrefs.putString(Elderoid.LANGUAGE, lang);
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        Elderoid.getContext().getResources().updateConfiguration(
                config,
                Elderoid.getContext().getResources().getDisplayMetrics()
        );
    }

    public static void setLanguage(Context context, String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(
                config,
                context.getResources().getDisplayMetrics()
        );
    }

    public static String getLanguage() {
        return SimplePrefs.getString(Elderoid.LANGUAGE, "en");
    }

    public static void addAppPackageToSimplePrefs(String app_package) {
        List<String> app_packages = loadAppPackagesFromSimplePrefs();
        if (app_packages.add(app_package)) saveAppPackagesToSimplePrefs(app_packages);
    }

    public static void removeAppPackageFromSimplePrefs(String app_package) {
        List<String> app_packages = loadAppPackagesFromSimplePrefs();
        if (app_packages.remove(app_package)) saveAppPackagesToSimplePrefs(app_packages);
    }

    public static void saveAppPackagesToSimplePrefs(Collection<String> app_packages) {
        int i = 0;
        for (String package_name : app_packages) {
            SimplePrefs.putString(APP + i++, package_name);
        }
        SimplePrefs.putInt(APP_NUM, i);
        SimplePrefs.putInt(APP_NUM_MAX, Math.max(SimplePrefs.getInt(APP_NUM_MAX), i));
    }

    public static List<String> loadAppPackagesFromSimplePrefs() {
        List<String> app_packages = new ArrayList<>();
        int app_num = SimplePrefs.getInt(APP_NUM);
        for (int i = 0; i < app_num; i++) {
            app_packages.add(SimplePrefs.getString(APP + i));
        }
        return app_packages;
    }

    public static void cleanAppPackagesFromSimplePrefs() {
        if (SimplePrefs.getInt(APP_NUM_MAX, 0) == 0) return;
        int app_num = SimplePrefs.getInt(APP_NUM_MAX);
        for (int i = 0; i < app_num; i++) {
            SimplePrefs.remove(APP + i);
        }
        SimplePrefs.putInt(APP_NUM, 0);
        SimplePrefs.putInt(APP_NUM_MAX, 0);
    }

    public static void addTopic(String topic, boolean state) {
        Map<String, Boolean> topics = loadTopics();
        topics.put(topic, state);
        saveTopics(topics);
    }

    public static void updateTopic(String topic, boolean state) {
        addTopic(topic, state);
    }

    public static void removeTopic(String topic) {
        Map<String, Boolean> topics = loadTopics();
        topics.remove(topic);
        saveTopics(topics);
    }

    public static void saveTopics(Map<String, Boolean> topics) {
        int i = 0;
        for (Map.Entry<String, Boolean> entry : topics.entrySet()) {
            SimplePrefs.putString(TOPIC + i, entry.getKey());
            SimplePrefs.putBoolean(TOPIC_STATE + i++, entry.getValue());
        }
        SimplePrefs.putInt(TOPIC_NUM, i);
        SimplePrefs.putInt(TOPIC_NUM_MAX, Math.max(SimplePrefs.getInt(TOPIC_NUM_MAX), i));
    }

    public static Map<String, Boolean> loadTopics() {
        int topicNum = SimplePrefs.getInt(TOPIC_NUM);
        if (topicNum == 0) {
            Map<String, Boolean> defaultTopics = getDefaultTopics();
            saveTopics(defaultTopics);
            return defaultTopics;
        }
        Map<String, Boolean> topics = new HashMap<>();
        for (int i = 0; i < topicNum; i++) {
            topics.put(SimplePrefs.getString(TOPIC + i), SimplePrefs.getBoolean(TOPIC_STATE + i));
        }
        return topics;
    }

    public static void cleanTopics() {
        if (SimplePrefs.getInt(TOPIC_NUM_MAX, 0) == 0) return;
        int topicNum = SimplePrefs.getInt(TOPIC_NUM_MAX);
        for (int i = 0; i < topicNum; i++) {
            SimplePrefs.remove(TOPIC + i);
            SimplePrefs.remove(TOPIC_STATE + i);
        }
        SimplePrefs.putInt(TOPIC_NUM, 0);
        SimplePrefs.putInt(TOPIC_NUM_MAX, 0);
    }

    public static String parseTopics() {
        int topicNum = SimplePrefs.getInt(TOPIC_NUM);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < topicNum; i++) {
            if (SimplePrefs.getBoolean(TOPIC_STATE + i))
                sb.append(SimplePrefs.getString(TOPIC + i)).append(" || ");
        }
        if (sb.length() >= 4) sb.delete(sb.length() - 4, sb.length());
        return sb.toString();
    }

    public static Map<String, Boolean> getDefaultTopics() {
        Map<String, Boolean> defaultTopics = new HashMap<>();
        defaultTopics.put(Elderoid.string(R.string.topic_gastronomy), false);
        defaultTopics.put(Elderoid.string(R.string.topic_sports), false);
        defaultTopics.put(Elderoid.string(R.string.topic_technology), false);
        defaultTopics.put(Elderoid.string(R.string.topic_music), false);
        defaultTopics.put(Elderoid.string(R.string.topic_economy), false);
        defaultTopics.put(Elderoid.string(R.string.topic_politics), false);
        defaultTopics.put(Elderoid.string(R.string.topic_leisure), false);
        defaultTopics.put(Elderoid.string(R.string.topic_famous), false);
        defaultTopics.put(Elderoid.string(R.string.topic_beauty), false);
        Elderoid.saveTopics(defaultTopics);
        return defaultTopics;
    }

    public static int getMissedCallsCount() {
        String PATH = "content://call_log/calls";

        String[] projection = new String[] { CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE };

        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor cursor = getContext().getContentResolver().query(
                Uri.parse(PATH),
                projection,
                CallLog.Calls.TYPE + "=?" + " and " + CallLog.Calls.IS_READ + "=?",
                new String[] { String.valueOf(CallLog.Calls.MISSED_TYPE), "0" },
                sortOrder
        );

        int count = cursor.getCount();

        cursor.close();

        return count;
    }

    public static Set<CallInfo> getMissedCalls() {
        String PATH = "content://call_log/calls";

        String[] projection = new String[] { CallLog.Calls.CACHED_NAME,
                CallLog.Calls.NUMBER, CallLog.Calls.DATE, CallLog.Calls.TYPE };

        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor cursor = getContext().getContentResolver().query(
                Uri.parse(PATH),
                projection,
                CallLog.Calls.TYPE + "=?" + " and " + CallLog.Calls.IS_READ + "=?",
                new String[] { String.valueOf(CallLog.Calls.MISSED_TYPE), "0" },
                sortOrder
        );

        Set<CallInfo> missedCalls = new HashSet<>();

        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            String dateString = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            long seconds=Long.parseLong(dateString);

            missedCalls.add(new CallInfo(new Date(seconds), number, name, "0", CallInfo.CallType.MISSED));

            if (missedCalls.size() > 9) break; // We only want 10 calls maximum
        }

        cursor.close();

        System.out.println(missedCalls);

        return missedCalls;
    }

    public static Set<CallInfo> getCalls() {
        //Uri allCalls = Uri.parse("content://call_log/calls");
        Cursor cursor = getContext().getContentResolver().query(CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC");

        Set<CallInfo> missedCalls = new HashSet<>();

        while (cursor.moveToNext()) {
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
            String dateString = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DATE));
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            String duration = cursor.getString(cursor.getColumnIndex(CallLog.Calls.DURATION));
            CallInfo.CallType callType;
            switch (Integer.parseInt(cursor.getString(cursor.getColumnIndex(CallLog.Calls.TYPE)))) {
                case INCOMING_TYPE:
                    callType = CallInfo.CallType.INCOMING;
                    break;
                case OUTGOING_TYPE:
                    callType = CallInfo.CallType.OUTGOING;
                    break;
                default:
                    callType = CallInfo.CallType.MISSED;
                    break;
            }
            long seconds=Long.parseLong(dateString);

            missedCalls.add(new CallInfo(new Date(seconds), number, name, duration, callType));

            if (missedCalls.size() > 9) break; // We only want 10 calls maximum
        }

        cursor.close();

        System.out.println(missedCalls);

        return missedCalls;
    }

    /**
     * Bold certain part of String
     * @param bounds Bounds, even index is an starting bound, odd index is a ending bound.
     * @param text String containing source text.
     * @return SpannableString
     */
    public static SpannableString bold(String text, int...bounds) {
        SpannableString ss = new SpannableString(text);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        for (int i = 0; i < bounds.length; i += 2) {
            ss.setSpan(boldSpan, bounds[i], bounds[i+1], Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        return ss;
    }

    public static String string(@StringRes int res) {
        return Elderoid.getInstance().getResources().getString(res);
    }

    public static void log (Object o) {
        Log.d("ELDEROID", " -------------------------> " + o);
    }
}
