package com.hanayue.ayuemobieview.weather.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.tools.HttpUtil;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherAutoUpdateService extends Service {

    public WeatherAutoUpdateService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        updateWeather(intent.getStringExtra("weatherValue"));
        updateBingPic();
        AlarmManager manager = (AlarmManager) getSystemService(ALARM_SERVICE); // 设置每隔8小时更新天气
        int anHour = 8 * 60 * 60 * 1000;
        long triggerAtTime = SystemClock.elapsedRealtime() + anHour;
        Intent i = new Intent(this, WeatherAutoUpdateService.class);
        PendingIntent pi = PendingIntent.getService(this, 0, i, 0);
        manager.cancel(pi);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAtTime, pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void updateWeather(String weatherValue) {
        HttpUtil.get(HttpUtil.CAI_YUN_PATH_PRE + weatherValue + HttpUtil.CAI_YUN_PATH_AFTER, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String resposeText = response.body().string();
                JSONObject weather = JSONObject.parseObject(resposeText);
                if(!weather.isEmpty()){
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherAutoUpdateService.this).edit();
                    editor.putString("weather", resposeText);
                    editor.apply();
                }
            }
        });
    }

    private void updateBingPic() {
        HttpUtil.get(HttpUtil.BING_PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String bingPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherAutoUpdateService.this).edit();
                editor.putString("bing_pic", bingPic);
                editor.apply();
            }
        });
    }
}
