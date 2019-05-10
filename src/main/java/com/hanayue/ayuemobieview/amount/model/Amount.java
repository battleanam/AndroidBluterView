package com.hanayue.ayuemobieview.amount.model;

import org.litepal.annotation.Column;
import org.litepal.crud.LitePalSupport;

import java.text.DateFormat;

public class Amount extends LitePalSupport {
    @Column(unique = true, nullable = false)
    private int id; //ID
    private String userId;  // 用户ID
    private float count; //花费了多少钱
    private long noteTime; //记录的时间
    private String timeStr; // 年月日的日期 用于分组
    private String type; //收入还是支出
    private String sourceType; //来源类型
    private String moneyType; //资本类型  现金还是支付宝？
    private String remark; //备注

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

    public float getCount() {
        return count;
    }

    public void setCount(float count) {
        this.count = count;
    }

    public long getNoteTime() {
        return noteTime;
    }

    public void setNoteTime(long noteTime) {
        this.noteTime = noteTime;
        setTimeStr(DateFormat.getDateInstance(DateFormat.LONG).format(noteTime));
    }

    public String getTimeStr() {
        return timeStr;
    }

    public void setTimeStr(String timeStr) {
        this.timeStr = timeStr;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public String getMoneyType() {
        return moneyType;
    }

    public void setMoneyType(String moneyType) {
        this.moneyType = moneyType;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}


