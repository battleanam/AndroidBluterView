package com.hanayue.ayuemobieview.weather.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.bumptech.glide.Glide;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.tools.HttpUtil;
import com.hanayue.ayuemobieview.weather.services.WeatherAutoUpdateService;
import com.hanayue.ayuemobieview.weather.util.WeatherTransformUtility;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class WeatherActivity extends AppCompatActivity {

    private static final String TAG = "WeatherActivity";

    private ScrollView weatherLayout; // 外层的滚动布局

    public TextView titleCity; // 城市

    private TextView titleUpdateTime; // 更新时间

    private TextView degreeText; // 温度

    private TextView humidityText; // 湿度

    private TextView wind; // 风向

    private TextView skycon; // 雨雪情况（天气状况）

    private LinearLayout forecastLayout; // 天气预报的线性布局

    private TextView pm25Text; // 可入肺颗粒物

    private TextView pm10Text; // 可吸入颗粒物

    private TextView o3Text; // 臭氧

    private TextView coText; // 一氧化碳

    private TextView no2Text; // 二氧化氮

    private TextView so2Text; // 二氧化硫

    private TextView comfortText; // 出行建议

    private ImageView bingPicImg; // 必应图片 （天气的背景）

    public SwipeRefreshLayout swipeRefresh; // 下拉刷新的布局

    private String weatherValue = ""; // 天气坐标

    public DrawerLayout drawerLayout; // 侧滑菜单

    //声明AMapLocationClient类对象
    public AMapLocationClient mLocationClient;
    //声明定位回调监听器
    public AMapLocationListener mLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLocationOption();
        setContentView(R.layout.activity_weather);
        initView();
        Intent intent = new Intent();
        intent.putExtra("res", R.id.nav_note);
        setResult(1, intent);
    }

    /**
     * 初始化视图
     */
    private void initView() {
        weatherLayout = findViewById(R.id.weather_layout);
        titleCity = findViewById(R.id.title_city);
        titleUpdateTime = findViewById(R.id.title_update_time);
        degreeText = findViewById(R.id.degree_text);
        humidityText = findViewById(R.id.weather_info_humidity);
        wind = findViewById(R.id.weather_info_wind);
        skycon = findViewById(R.id.weather_info_skycon);
        forecastLayout = findViewById(R.id.forecast);
        pm25Text = findViewById(R.id.weather_pm25);
        pm10Text = findViewById(R.id.weather_pm10);
        o3Text = findViewById(R.id.weather_o3);
        coText = findViewById(R.id.weather_co);
        no2Text = findViewById(R.id.weather_no2);
        so2Text = findViewById(R.id.weather_so2);
        comfortText = findViewById(R.id.weather_comfort_text);

        bingPicImg = findViewById(R.id.weather_bing_pic);
        swipeRefresh = findViewById(R.id.swipe_refresh);


        titleCity.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));

        weatherLayout.setVisibility(View.INVISIBLE);
        startListenLocationAndLoadWeather(); // 获取当前位置的天气

        swipeRefresh.setOnRefreshListener(() -> requestWeather(weatherValue));

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String bingPic = sharedPreferences.getString("bing_pic", null);
        if (bingPic != null) {
            Glide.with(this).load(HttpUtil.BING_PIC_URL).into(bingPicImg);
        } else {
            loadBingPic();
        }

        drawerLayout = findViewById(R.id.weather_draw_layout);
        // 导航home按钮
        Button navBtn = findViewById(R.id.weather_nav_btn);
        navBtn.setOnClickListener(v -> finish());
    }

    /**
     * 加载必应每日一图
     */
    private void loadBingPic() {
        HttpUtil.get(HttpUtil.BING_PIC_URL, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                final String binPic = response.body().string();
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                editor.putString("bing_pic", binPic);
                editor.apply();
                if (!isDestroyed()) {
                    runOnUiThread(() -> Glide.with(WeatherActivity.this).load(binPic).into(bingPicImg));
                }
            }
        });
    }

    /**
     * 初始化定位参数配置
     */

    private void initLocationOption() {
        mLocationListener = aMapLocation -> {
            Log.d(TAG, "getLatitude: " + aMapLocation.getLatitude());
            Log.d(TAG, "getLongitude: " + aMapLocation.getLongitude());
            Log.d(TAG, "getLocationDetail: " + aMapLocation.getLocationDetail());
            Log.d(TAG, "getAddress: " + aMapLocation.getAddress());
            Log.d(TAG, "getDescription: " + aMapLocation.getErrorCode());
            Log.d(TAG, "getErrorInfo: " + aMapLocation.getErrorInfo());
            String cityName = aMapLocation.getCity() + aMapLocation.getDistrict();
            titleCity.setText(cityName);
            weatherValue = aMapLocation.getLatitude() + "," + aMapLocation.getLongitude();
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
            String weatherStr = sharedPreferences.getString(weatherValue, null);
            if (weatherStr != null) {
                loadBingPic();
                JSONObject weather = JSONObject.parseObject(weatherStr);
                showWeatherInfo(weather);
            } else {
                weatherLayout.setVisibility(View.INVISIBLE);
                requestWeather(weatherValue);
            }
            mLocationClient.stopLocation();
        };
        mLocationClient = new AMapLocationClient(getApplicationContext());
        AMapLocationClientOption option = new AMapLocationClientOption();
        //设置是否允许模拟位置,默认为true，允许模拟位置
        option.setMockEnable(true);
        mLocationClient.setLocationOption(option);
        mLocationClient.setLocationListener(mLocationListener);
    }

    /**
     * 开始监听位置信息改变并获取位置当前位置的天气状况
     */
    private void startListenLocationAndLoadWeather() {
        mLocationClient.startLocation();
    }


    /**
     * 处理拿到的天气信息 解析成页面上的数据 并展示到界面上
     *
     * @param weather 从后台取到的数据
     */
    private void showWeatherInfo(JSONObject weather) {
        DateFormat dateFormat = DateFormat.getTimeInstance(DateFormat.SHORT); // 时间转换格式 上午09:00
        String updateTime = dateFormat.format(new Date());
        JSONObject result = weather.getJSONObject("result");
        JSONObject realtime = result.getJSONObject("realtime"); // 获取实时预报信息
        String degree = realtime.getString("temperature");
        String humidity = realtime.getString("humidity");
        String wind = WeatherTransformUtility.transformWind(realtime.getJSONObject("wind"));
        String skycon = WeatherTransformUtility.transformSkycon(realtime.getString("skycon"));
        String pm25 = realtime.getDoubleValue("pm25") + "";
        String pm10 = realtime.getDoubleValue("pm10") + "";
        String o3 = realtime.getDoubleValue("o3") + "";
        String no2 = realtime.getDoubleValue("no2") + "";
        String so2 = realtime.getDoubleValue("so2") + "";
        String co = realtime.getDoubleValue("co") + "";
        JSONObject daily = result.getJSONObject("daily"); // 逐日播报
        JSONArray temperature = daily.getJSONArray("temperature"); // 温度
        JSONArray skycons = daily.getJSONArray("skycon"); // 天气状况
        forecastLayout.removeAllViews();
        for (int i = 0; i < temperature.size(); i++) {
            View view = LayoutInflater.from(this).inflate(R.layout.weather_forecast_item, forecastLayout, false);
            TextView dateText = view.findViewById(R.id.weather_date_text);
            TextView infoText = view.findViewById(R.id.weather_info_text);
            TextView minText = view.findViewById(R.id.weather_min_text);
            TextView maxText = view.findViewById(R.id.weather_max_text);
            dateText.setText(temperature.getJSONObject(i).getString("date"));
            infoText.setText(WeatherTransformUtility.transformSkycon(skycons.getJSONObject(i).getString("value")));
            minText.setText(temperature.getJSONObject(i).getString("min"));
            maxText.setText(temperature.getJSONObject(i).getString("max"));
            forecastLayout.addView(view);
        }

        titleUpdateTime.setText(updateTime);
        degreeText.setText(degree);
        humidityText.setText(humidity);
        this.wind.setText(wind);
        this.skycon.setText(skycon);

        if (realtime.getString("aqi") != null) {
            pm25Text.setText(pm25);
            pm10Text.setText(pm10);
            o3Text.setText(o3);
            no2Text.setText(no2);
            so2Text.setText(so2);
            coText.setText(co);
        }

        comfortText.setText(result.getString("forecast_keypoint"));
        weatherLayout.setVisibility(View.VISIBLE);

        Intent intent = new Intent(this, WeatherAutoUpdateService.class);
        intent.putExtra("weatherValue", weatherValue);
        startService(intent);
    }

    /**
     * 从服务器获取天气信息
     *
     * @param weatherValue 当前位置的经纬度
     */
    public void requestWeather(final String weatherValue) {
        loadBingPic();
        String[] temp = weatherValue.split(",");
        String url = HttpUtil.CAI_YUN_PATH_PRE + temp[1] + "," + temp[0] + HttpUtil.CAI_YUN_PATH_AFTER;
        HttpUtil.get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                if(isDestroyed()) return;
                runOnUiThread(() -> {
                    Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(isDestroyed()) return;
                assert response.body() != null;
                final String resText = response.body().string();
                JSONObject weather = JSONObject.parseObject(resText);
                runOnUiThread(() -> {
                    if (!weather.isEmpty() && weather.getString("status").equals("ok")) {
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WeatherActivity.this).edit();
                        editor.putString(weatherValue, resText);
                        editor.apply();
                        WeatherActivity.this.weatherValue = weatherValue;
                        showWeatherInfo(weather);
                    } else {
                        Toast.makeText(WeatherActivity.this, "获取天气信息失败", Toast.LENGTH_SHORT).show();
                    }
                    swipeRefresh.setRefreshing(false);
                });
            }
        });

    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.onDestroy();
    }
}
