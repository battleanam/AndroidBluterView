package com.hanayue.ayuemobieview.notice.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.ActivityNoticeHistoryBinding;
import com.hanayue.ayuemobieview.notice.adapter.NoticeAdapter;
import com.hanayue.ayuemobieview.notice.model.Notice;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

public class NoticeHistoryActivity extends AppCompatActivity {

    ActivityNoticeHistoryBinding binding;
    JSONObject userInfo;
    NoticeAdapter adapter;
    List<Notice> notices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notice_history);

        getUserInfo();
        initNotices();

        binding.toolbar.setTitle("事件提醒历史记录");
        setSupportActionBar(binding.toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }



        //下拉刷新
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        binding.swipeRefresh.setOnRefreshListener(this::initNotices);
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null)
            userInfo = com.alibaba.fastjson.JSONObject.parseObject(userInfoStr);
        else {
            runOnUiThread(() -> {
                Toast.makeText(this, "未登录", Toast.LENGTH_SHORT).show();
                finish();
            });
        }
    }

    /**
     * 加载提醒列表
     */
    private void initNotices() {
        List<Notice> notices = LitePal
                .where("userId = ? and isNoticed = ?", userInfo.getString("id"), "1")
                .order("noticeTime desc")
                .find(Notice.class);
        if (adapter == null) {
            this.notices.addAll(notices);
            adapter = new NoticeAdapter(this.notices, false);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
            binding.recyclerView.setLayoutManager(layoutManager);
            binding.recyclerView.setAdapter(adapter);
        } else {
            this.notices.clear();
            this.notices.addAll(notices);
            this.adapter.notifyDataSetChanged();
        }
        binding.swipeRefresh.setRefreshing(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
                break;
            default:
        }
        return true;
    }
}
