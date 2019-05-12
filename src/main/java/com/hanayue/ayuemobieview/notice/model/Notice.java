package com.hanayue.ayuemobieview.notice.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

public class Notice extends LitePalSupport {

    @Column(unique = true, nullable = false)
    private int id;
    private String userId;
    private String title;
    private long noticeTime;
    private int shiftTime;
    private String content;
    @Column(nullable = false, defaultValue = "0")
    private int isNoticed; // 是否提醒过了 1是0否

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getNoticeTime() {
        return noticeTime;
    }

    public void setNoticeTime(long noticeTime) {
        this.noticeTime = noticeTime;
    }

    public int getShiftTime() {
        return shiftTime;
    }

    public void setShiftTime(int shiftTime) {
        this.shiftTime = shiftTime;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getIsNoticed() {
        return isNoticed;
    }

    public void setIsNoticed(int isNoticed) {
        this.isNoticed = isNoticed;
    }
}
