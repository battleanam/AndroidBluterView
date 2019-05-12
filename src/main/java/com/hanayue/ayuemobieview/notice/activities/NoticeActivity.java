package com.hanayue.ayuemobieview.notice.activities;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.NumberPicker;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.bean.DateType;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.ActivityNoticeBinding;
import com.hanayue.ayuemobieview.notice.model.Notice;
import com.hanayue.ayuemobieview.notice.services.NoticeService;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class NoticeActivity extends AppCompatActivity {

    public static final String NOTICE_ID = "notice_id";


    ActivityNoticeBinding binding;
    JSONObject userInfo; // 用户信息
    Notice notice; // 当前正在编辑的提醒
    String type; // add or edit


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_notice);

        getUserInfo();
        initNotice();
        initView();
        Intent intent = new Intent();
        intent.putExtra("res", R.id.nav_notice);
        setResult(2, intent);
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null) userInfo = JSONObject.parseObject(userInfoStr);
        else {
            Toast.makeText(this, "未登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 初始化notice
     */
    private void initNotice() {
        type = getIntent().getStringExtra(MainActivity.NAV_TYPE);
        if ("add".equals(type)) {
            notice = new Notice();
            notice.setTitle("");
            notice.setNoticeTime(System.currentTimeMillis());
            notice.setShiftTime(0);
            notice.setUserId(userInfo.getString("id"));
            notice.setContent("");
            binding.toolbar.setTitle("添加提醒");
        } else {
            int noticeId = getIntent().getIntExtra(NOTICE_ID, 0);
            notice = LitePal.find(Notice.class, noticeId);
            binding.toolbar.setTitle("修改提醒");
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {

        setSupportActionBar(binding.toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        binding.title.setText(notice.getTitle());
        binding.noticeTime.setText(dateFormat.format(notice.getNoticeTime()));
        binding.noticeTime.setOnClickListener(v -> showTimePicker());

        String shiftTime = notice.getShiftTime() + "";
        binding.shiftTime.setText(shiftTime);
        binding.shiftTime.setOnClickListener(v -> showNumberPicker());

        binding.remark.setText(notice.getContent());

        binding.saveBtn.setOnClickListener(v -> {
            if (binding.title.getText().toString().isEmpty()) {
                runOnUiThread(() -> Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show());
            } else {
                handleSave();
            }
        });
    }

    /**
     * 处理保存
     */
    private void handleSave() {
        notice.setTitle(binding.title.getText().toString());
        notice.setContent(binding.remark.getText().toString());
        if (notice.isSaved()) {
            if (notice.update(notice.getId()) > 0) runOnUiThread(() -> {
                long minTime = LitePal
                        .where("isNoticed = ? and userId = ?", "0", userInfo.getString("id"))
                        .min(Notice.class, "noticeTime", Long.class);
                if (minTime == notice.getNoticeTime()) { // 如果是最近的一次提醒 取消之前的定时事件 重启控制服务
                    AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                    if(alarmManager.getNextAlarmClock() != null){
                        PendingIntent pendingIntent = alarmManager.getNextAlarmClock().getShowIntent();
                        alarmManager.cancel(pendingIntent);
                    }
                    Intent intent = new Intent(this, NoticeService.class);
                    PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, notice.getNoticeTime(), pendingIntent);
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notice.getNoticeTime(), pendingIntent);
                    }
                }
                Toast.makeText(this, "修改成功", Toast.LENGTH_SHORT).show();
                finish();
            });
            else runOnUiThread(() -> Toast.makeText(this, "修改失败", Toast.LENGTH_SHORT).show());
        } else {
            if (notice.save()) runOnUiThread(() -> {
                AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
                Intent intent = new Intent(this, NoticeService.class);
                PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, notice.getNoticeTime(), pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, notice.getNoticeTime(), pendingIntent);
                }
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
            else runOnUiThread(() -> Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show());
        }
    }

    /**
     * 处理删除
     */
    private void handleDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除提醒");
        builder.setNegativeButton("确定", (d, v) -> {
            if (LitePal.delete(Notice.class, notice.getId()) > 0) runOnUiThread(() -> {
                Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show();
                finish();
            });
            else runOnUiThread(() -> {
                Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show();
            });
        }).setPositiveButton("取消", (d, v) -> {

        }).create().show();
    }

    /**
     * 显示时间选择器
     */
    private void showTimePicker() {
        DatePickDialog dialog = new DatePickDialog(this);
        dialog.setStartDate(new Date(System.currentTimeMillis()));
        //设置上下年分限制
        dialog.setYearLimt(5);
        //设置标题
        dialog.setTitle("选择时间");
        //设置类型
        dialog.setType(DateType.TYPE_YMDHM);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy年MM月dd日 HH:mm");
        //设置选择回调
        dialog.setOnChangeLisener(null);
        //设置点击确定按钮回调
        dialog.setOnSureLisener(date -> {
            if (date.after(new Date(System.currentTimeMillis()))) {
                runOnUiThread(() -> binding.noticeTime.setText(DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(date)));
                notice.setNoticeTime(date.getTime());
            } else {
                runOnUiThread(() -> Toast.makeText(this, "时间间隔不能小于一分钟", Toast.LENGTH_SHORT).show());
            }
        });
        dialog.show();
    }

    /**
     * 展示数字选择器
     */
    private void showNumberPicker() {
        long maxValue = (notice.getNoticeTime() - System.currentTimeMillis()) / (1000 * 60 * 60 * 24);
        if (maxValue > 29) maxValue = 30;
        if (maxValue < 1) {
            runOnUiThread(() -> Toast.makeText(this, "与当前间隔小于1天, 不允许提前", Toast.LENGTH_SHORT).show());
        } else {
            AtomicInteger value = new AtomicInteger();
            NumberPicker numberPicker = new NumberPicker(this);
            numberPicker.setMinValue(0);
            numberPicker.setMaxValue((int) maxValue);
            numberPicker.setOnValueChangedListener((picker, oldValue, newValue) ->
                    value.set(newValue)
            );
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(numberPicker).setPositiveButton("取消", (dialogInterface, i) -> {
            }).setNegativeButton("设置", (dialogInterface, i) -> {
                        notice.setShiftTime(value.get());
                        runOnUiThread(() -> {
                            String text = value.get() + "";
                            binding.shiftTime.setText(text);
                        });
                    }
            ).create().show();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                finish();
                break;
            case R.id.delete:
                // 删除
                handleDelete();
                break;
            default:
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("edit".equals(type)) getMenuInflater().inflate(R.menu.delete_only, menu);
        else getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }
}
