package com.hanayue.ayuemobieview.splash;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.TypedValue;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;

import java.util.ArrayList;
import java.util.List;

public class SplashActivity extends FragmentActivity {


    private ViewPager viewPager;
    private LinearLayout llIndicator;
    private PagerAdapter adapter;
    private List<Fragment> fragments = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        askPermission();
        setContentView(R.layout.activity_splash);
        judgeSkipLogin();
        initView();
    }

    /**
     * 判断是否需要跳过登录
     */
    private void judgeSkipLogin() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfo = preferences.getString("userInfo", null);
        if (userInfo != null) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        llIndicator = (LinearLayout) findViewById(R.id.ll_indicator);
        this.generateFragment();
    }

    /**
     * 创建fragment
     */
    private void generateFragment() {
        for (int i = 0; i < 5; i++) {
            SplashFragment splashFragment = new SplashFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("index", i);
            splashFragment.setArguments(bundle);
            fragments.add(splashFragment);
        }
        adapter = new SplashPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                for (int index = 0; index < fragments.size(); index++) {
                    llIndicator.getChildAt(index).setBackgroundResource(i == index ? R.drawable.dot_focus : R.drawable.dot_normal);
                }
            }

            @Override
            public void onPageSelected(int i) {

            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });
        initIndicator();
    }

    /**
     * 初始化指示器  小圆点
     */
    public void initIndicator() {
        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10f, getResources().getDisplayMetrics());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(width, width);
        lp.rightMargin = 2 * width;
        for (int i = 0; i < fragments.size(); i++) {
            View view = new View(this);
            view.setId(i);
            view.setBackgroundResource(i == 0 ? R.drawable.dot_focus : R.drawable.dot_normal);
            view.setLayoutParams(lp);
            llIndicator.addView(view, i);
        }
    }


    /**
     * 动态获取权限
     */
    private void askPermission() {
        List<String> permissionList = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (ContextCompat.checkSelfPermission(SplashActivity.this,
                Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.INTERNET);
        }

        if (!permissionList.isEmpty()) {
            String[] permissions = permissionList.toArray(new String[permissionList.size()]);
            ActivityCompat.requestPermissions(SplashActivity.this, permissions, 1);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    for (int result : grantResults) {
                        if (result != PackageManager.PERMISSION_GRANTED) {
                            Toast.makeText(this, "您需要同意上述权限才可以使用本程序，请在设置中打开", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                    }
                }
                break;
            default:
        }
    }


}
