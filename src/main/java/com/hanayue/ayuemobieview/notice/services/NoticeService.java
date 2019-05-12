package com.hanayue.ayuemobieview.notice.services;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.notice.model.Notice;

import org.litepal.LitePal;

import java.util.List;

public class NoticeService extends Service {

    public static final String NOTICE_CHANNEL_ID = "NoticeService";

    JSONObject userInfo;
    List<Notice> notices;
    List<Notice> badNotices; // 过了时间但是没提醒的


    public NoticeService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getUserInfo(); // 获取用户信息
        initNotices(); // 初始化提醒列表
        startNotice(); // 开始计时提醒
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null)
            userInfo = com.alibaba.fastjson.JSONObject.parseObject(userInfoStr);
        else super.onDestroy();
    }

    /**
     * 初始化提醒列表
     */
    private void initNotices() {
        badNotices = LitePal
                .where("userId = ? and isNoticed = ? and noticeTime < ?", userInfo.getString("id"), "0", System.currentTimeMillis() + "")
                .order("noticeTime")
                .find(Notice.class);
        notices = LitePal
                .where("userId = ? and isNoticed = ?", userInfo.getString("id"), "0")
                .order("noticeTime")
                .find(Notice.class);
    }

    /**
     * 开始计时提醒
     */
    private void startNotice() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        new Thread(() -> {
            for (Notice notice : badNotices) {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, NOTICE_CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle(notice.getTitle())
                                .setContentText(notice.getContent())
                                .setWhen(System.currentTimeMillis());
                Notification notification = builder.build();
                manager.notify(notice.getId(), notification);
                notice.setIsNoticed(1);
                notice.update(notice.getId());
            }
        }).start();
        if (notices.size() > 0) {
            Notice notice = notices.get(0);
            new Thread(() -> {
                NotificationCompat.Builder builder =
                        new NotificationCompat.Builder(this, NOTICE_CHANNEL_ID)
                                .setSmallIcon(R.drawable.icon)
                                .setContentTitle(notice.getTitle())
                                .setContentText(notice.getContent())
                                .setWhen(notice.getNoticeTime() + 1000); // 防止延时
                Notification notification = builder.build();
                manager.notify(notice.getId(), notification);
                notice.setIsNoticed(1);
                notice.update(notice.getId());

            }).start();
        }
        long noticeTime = SystemClock.elapsedRealtime() + 1; // 等待一分钟进行下一次提醒
        if (notices.size() > 1) { // 如果还有未提醒的事件 就添加进队列
            Notice notice = notices.get(1);
            noticeTime = (notice.getNoticeTime() - System.currentTimeMillis() + SystemClock.elapsedRealtime()) - notice.getShiftTime() * 1000 * 60 * 60 * 24; // 将提醒时间更换为新的提醒的时间
        }
        noticeTime = noticeTime - noticeTime % (60 * 1000);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent(this, NoticeService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, noticeTime, pendingIntent);
        } else {
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, noticeTime, pendingIntent);
        }

    }
}
