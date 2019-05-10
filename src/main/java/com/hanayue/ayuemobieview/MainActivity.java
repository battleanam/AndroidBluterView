package com.hanayue.ayuemobieview;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.amount.activities.AmountActivity;
import com.hanayue.ayuemobieview.amount.fragments.AmountFragment;
import com.hanayue.ayuemobieview.databinding.ActivityMainBinding;
import com.hanayue.ayuemobieview.note.activities.NoteActivity;
import com.hanayue.ayuemobieview.note.fragments.NoteListFragment;
import com.hanayue.ayuemobieview.notice.activities.NoticeActivity;
import com.hanayue.ayuemobieview.notice.fragments.NoticeFragment;
import com.hanayue.ayuemobieview.weather.activities.WeatherActivity;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final String NAV_TYPE = "nav_type";

    ActivityMainBinding binding;

    private JSONObject userInfo;

    private NoteListFragment noteListFragment;
    private NoticeFragment noticeFragment;
    private AmountFragment amountFragment;

    private int currentFragmentId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initView();
        activeFragment(R.id.nav_note);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
//        activeFragment(R.id.nav_note);
//        binding.navView.setCheckedItem(R.id.nav_note);
    }

    /**
     * 设置视图
     */
    private void initView() {

        setSupportActionBar(binding.toolbar);


        // 显示工具栏左侧图标
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.mipmap.round_touxiang);
        }

        // 点击添加按钮跳转
        binding.fab.setOnClickListener(this::toWhatAdd);

        // 侧滑菜单
        binding.navView.setCheckedItem(R.id.nav_note);
        binding.navView.setNavigationItemSelectedListener(item -> {
            activeFragment(item.getItemId());
            binding.drawerLayout.closeDrawers();
            return true;
        });

        View navHeader = binding.navView.getHeaderView(0);
        TextView navAccount = navHeader.findViewById(R.id.account);
        TextView navUsername = navHeader.findViewById(R.id.username);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String infoStr = preferences.getString("userInfo", null);
        if (infoStr != null) {
            userInfo = JSONObject.parseObject(infoStr);
            navAccount.setText(userInfo.getString("account"));
            navUsername.setText(userInfo.getString("username"));
        }

    }



    /**
     * 跳转到添加页面
     */
    private void toWhatAdd(View v) {
        Intent intent;
        int id = Objects.requireNonNull(binding.navView.getCheckedItem()).getItemId();
        switch (id) {
            case R.id.nav_note:
                intent = new Intent(MainActivity.this, NoteActivity.class);
                break;
            case R.id.nav_notice:
                intent = new Intent(MainActivity.this, NoticeActivity.class);
                break;
            case R.id.nav_money:
                intent = new Intent(MainActivity.this, AmountActivity.class);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                String[] types = {"收入", "支出"};
                builder.setItems(types, (dialog, witch) -> {
                    intent.putExtra(AmountActivity.AMOUNT_TYPE, witch == 0 ? "收入" : "支出");
                    intent.putExtra(NAV_TYPE, "add");
                    dialog.dismiss();
                    startActivityForResult(intent, 0);
                });
                builder.create().show();
                break;
            default:
                intent = new Intent();
        }
        intent.putExtra(NAV_TYPE, "add");
        if (id != R.id.nav_money) startActivityForResult(intent, 0);
    }

    /**
     * 将当前的fragment设置为选中的
     *
     * @param id 选中的菜单项的ID
     */
    private void activeFragment(int id) {
        this.currentFragmentId = id;
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        hideFragments(transaction);
        ActionBar actionBar = getSupportActionBar();
        switch (id) {
            case R.id.nav_note:
                if (null == noteListFragment) {
                    noteListFragment = new NoteListFragment();
                    transaction.add(R.id.main_note_fragment, noteListFragment);
                } else {
                    transaction.show(noteListFragment);
                }
                if (actionBar != null) {
                    actionBar.setTitle("便签管理");
                }
                break;
            case R.id.nav_notice:
                if (null == noticeFragment) {
                    noticeFragment = new NoticeFragment();
                    transaction.add(R.id.main_note_fragment, noticeFragment);
                } else {
                    transaction.show(noticeFragment);
                }
                if (actionBar != null) {
                    actionBar.setTitle("事件提醒");
                }
                break;
            case R.id.nav_money:
                if (null == amountFragment) {
                    amountFragment = new AmountFragment();
                    transaction.add(R.id.main_note_fragment, amountFragment);
                } else {
                    transaction.show(amountFragment);
                }
                if (actionBar != null) {
                    actionBar.setTitle("收支管理");
                }
                break;
            case R.id.nav_weather:
                Intent intent = new Intent(MainActivity.this, WeatherActivity.class);
                startActivityForResult(intent, 0);
                break;
        }
        transaction.commitAllowingStateLoss();
//        if (id == R.id.nav_money) {
//            binding.title.setVisibility(View.VISIBLE);
//            initTitle();
//        } else {
//            binding.title.setVisibility(View.GONE);
//        }
    }


    /**
     * 隐藏fragment
     */
    private void hideFragments(FragmentTransaction transaction) {
        if (null != noteListFragment) {
            transaction.hide(noteListFragment);
        }
        if (null != noticeFragment) {
            transaction.hide(noticeFragment);
        }
        if (null != amountFragment) {
            transaction.hide(amountFragment);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                binding.drawerLayout.openDrawer(GravityCompat.START);
                break;
            default:
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        switch (requestCode) {
            case 0:
                int id;
                if (data != null) {
                    id = data.getIntExtra("res", R.id.nav_note);
                } else {
                    id = R.id.nav_note;
                }
                activeFragment(id);
                binding.navView.setCheckedItem(id);
                break;
        }

    }
}