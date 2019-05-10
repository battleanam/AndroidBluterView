package com.hanayue.ayuemobieview.amount.model;

/**
 * 统计模型
 */
public class AmountStat {
    private String xAxis;
    private float value;

    public AmountStat() {
    }

    public AmountStat(String xAxis, float value) {
        this.xAxis = xAxis;
        this.value = value;
    }

    public String getxAxis() {
        return xAxis;
    }

    public void setxAxis(String xAxis) {
        this.xAxis = xAxis;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }
}
