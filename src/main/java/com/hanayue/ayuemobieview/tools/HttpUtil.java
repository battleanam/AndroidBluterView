package com.hanayue.ayuemobieview.tools;

import android.util.Log;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

public class HttpUtil {

    private static final MediaType JSON
            = MediaType.parse("application/json; charset=utf-8");
    private static final String HOST_ADDRESS = "http://www.hanayue.xn--6qq986b3xl/service-hy/";
//    private static final String HOST_ADDRESS = "http://192.168.0.2:8081/";
    public static final String LOCATION_HOST_URL = HOST_ADDRESS + "location";
    public static final String USER_HOST_URL = HOST_ADDRESS + "account";
    public static final String NOTE_HOST_URL = HOST_ADDRESS + "note";
    public static final String CAI_YUN_PATH_PRE = "https://api.caiyunapp.com/v2/eLSGlmRu2N1uY0VK/"; //对接彩云天气的URL前缀
    public static final String CAI_YUN_PATH_AFTER = "/weather.json"; //对接彩云天气的后续
    public static final String BING_PIC_URL = "http://guolin.tech/api/bing_pic";

    /**
     * post请求
     */
    public static void post(String url, String json, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(JSON, json);
        Log.d("请求参数", "post: " + body);
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        client.newCall(request).enqueue(callback);
    }

    public static void get(String url, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(url)
                .build();
        client.newCall(request).enqueue(callback);
    }
}
