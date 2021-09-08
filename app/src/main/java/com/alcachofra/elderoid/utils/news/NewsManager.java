package com.alcachofra.elderoid.utils.news;

import android.app.Activity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.alcachofra.elderoid.Elderoid;


import org.json.JSONArray;
import org.json.JSONException;

import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class NewsManager {

    /**
     * Format of news date.
     */
    public static final DateTimeFormatter NEWS_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Format of news time.
     */
    public static final DateTimeFormatter NEWS_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * Format of news datetime.
     */
    public static final DateTimeFormatter NEWS_DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * How old the news can be (in days).
     */
    private static final int TIME_GAP = 30;

    /**
     * Fields related to News API.
     */
    private static final String NEWS_API_KEY_HEADER = "x-rapidapi-key";
    private static final String NEWS_API_HOST_HEADER = "x-rapidapi-host";
    private static final String NEWS_API_KEY = "b6ea4fd607msh401ea444b5f4e9cp104c7cjsn8a28a1d9bb36";
    private static final String NEWS_API_HOST = "free-news.p.rapidapi.com";
    private static final String NEWS_REQUEST = "https://free-news.p.rapidapi.com/v1/search?";

    /**
     * Request a certain amount of views, an execute a function when news are received.
     * @param activity Activity in which the news are being requested.
     * @param maxNews Maximum number of news to request (less can be returned).
     * @param onSuccess Function to execute on success (receives list of New instances).
     * @param onError Function to execute in case of request error.
     */
    public static void request(Activity activity, int maxNews, Consumer<List<New>> onSuccess, Consumer<VolleyError> onError) {
        // Theme + language + date (date is set to yesterday at 00:00:00):
        String topics = Elderoid.parseTopics();
        if (topics.isEmpty()) {
            onSuccess.accept(Collections.emptyList());
            return;
        }

        // Request:
        String params = "q=" + topics +
                "&lang=" + Elderoid.getLanguage() +
                //"&country=" + Elderoid.getLanguage().toUpperCase() +
                //"&sources=" + "ionline.sapo.pt" +
                "&page_size=" + maxNews +
                "&from=" + LocalDateTime.now().minus(Period.ofDays(TIME_GAP)).format(NEWS_DATE_FORMAT);
        String url = NEWS_REQUEST + params; // Request

        Elderoid.log(url);

        JsonObjectRequest jor = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            Set<New> news = new HashSet<>();
            // On Success:
            try {
                JSONArray articles = response.getJSONArray("articles"); // Get articles
                for (int i = 0; i < Math.min(articles.length(), maxNews); i++) {
                    news.add(new New(
                            articles.getJSONObject(i).getString("title"),
                            articles.getJSONObject(i).getString("summary"),
                            articles.getJSONObject(i).getString("published_date"),
                            articles.getJSONObject(i).getString("link"),
                            articles.getJSONObject(i).getString("media"),
                            articles.getJSONObject(i).getString("_id")
                    ));
                }
                onSuccess.accept(new ArrayList<>(news));
            } catch (JSONException e) {
                e.printStackTrace();
                news.clear();
                onSuccess.accept(new ArrayList<>(news));
            }
        }, onError::accept) {
            // Headers:
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put(NEWS_API_KEY_HEADER, NEWS_API_KEY);
                headers.put(NEWS_API_HOST_HEADER, NEWS_API_HOST);
                return headers;
            }
        };
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(jor);
    }
}
