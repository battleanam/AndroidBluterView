package com.hanayue.ayuemobieview.amount.model;

import java.util.List;

public class AmountGroup {
    private String time;
    private List<Amount> amounts;

    public AmountGroup() {
    }

    public AmountGroup(String time, List<Amount> amounts) {
        this.time = time;
        this.amounts = amounts;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public List<Amount> getAmounts() {
        return amounts;
    }

    public void setAmounts(List<Amount> amounts) {
        this.amounts = amounts;
    }
}
