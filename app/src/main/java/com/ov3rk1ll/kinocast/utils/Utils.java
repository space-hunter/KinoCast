package com.ov3rk1ll.kinocast.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseIntArray;

import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.libraries.cast.companionlibrary.cast.CastConfiguration;
import com.google.android.libraries.cast.companionlibrary.cast.VideoCastManager;
import com.ov3rk1ll.kinocast.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class Utils {
    public static final String USER_AGENT = "KinoCast v" + BuildConfig.VERSION_NAME;

    public static boolean isStringEmpty(String val) {
        if (val == null) return true;
        if (val.isEmpty()) return true;
        if (val.equalsIgnoreCase("null")) return true;
        if (val.equalsIgnoreCase("http:null")) return true;
        if (val.equalsIgnoreCase("https:null")) return true;
        if (val.equalsIgnoreCase("https://null")) return true;
        if (val.equalsIgnoreCase("http://null")) return true;
        return false;
    }

    public static String getUrl(String url) {
        if (url != null && url.startsWith("//")) return "https:" + url;
        return url;
    }

    public static String getRedirectTarget(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .followRedirects(false)
                .addNetworkInterceptor(new UserAgentInterceptor(USER_AGENT))
                .build();
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = client.newCall(request).execute();
            return response.header("Location");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject readJson(String url) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(USER_AGENT))
                .build();
        Request request = new Request.Builder().url(url).build();

        Log.i("Utils", "read json from " + url);
        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            return new JSONObject(body);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static JSONObject readJson(String url, Set<Map.Entry<String,String>> postData) {
        OkHttpClient client = new OkHttpClient.Builder()
                .addNetworkInterceptor(new UserAgentInterceptor(USER_AGENT))
                .build();

        MultipartBody.Builder bodyBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String, String> pair: postData) {
            bodyBuilder.addFormDataPart(pair.getKey(), pair.getValue());
        }
        RequestBody requestBody = bodyBuilder.build();

        Request request = new Request.Builder().url(url).post(requestBody).build();

        Log.i("Utils", "read json from " + url);
        try {
            Response response = client.newCall(request).execute();
            String body = response.body().string();
            return new JSONObject(body);
        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ((netInfo != null) && netInfo.isConnected());
    }

    public static SparseIntArray getWeightedHostList(Context context) {
        SparseIntArray sparseArray = new SparseIntArray();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int count = preferences.getInt("order_hostlist_count", -1);
        if (count == -1) return null;
        for (int i = 0; i < count; i++) {
            int key = preferences.getInt("order_hostlist_" + i, i);
            sparseArray.put(key, i);
        }
        return sparseArray;
    }

    public static VideoCastManager initializeCastManager(Context context) {
        CastConfiguration.Builder builder = new CastConfiguration.Builder(CastMediaControlIntent.DEFAULT_MEDIA_RECEIVER_APPLICATION_ID)
                .enableAutoReconnect()
                .enableCaptionManagement()
                .enableWifiReconnection();

        if (BuildConfig.DEBUG) {
            builder.enableDebug();
        }

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if (preferences.getBoolean("chromecast_lock_screen", true)) {
            builder.enableLockScreen();
        }

        if (preferences.getBoolean("chromecast_notification", true)) {
            builder.enableNotification()
                    .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_PLAY_PAUSE, true)
                    .addNotificationAction(CastConfiguration.NOTIFICATION_ACTION_DISCONNECT, true);
        }

        return VideoCastManager.initialize(context, builder.build());
    }
}
