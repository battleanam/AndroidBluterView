package com.hanayue.ayuemobieview.amount.model;

/**
 * 选择支出类型的单个项的模型
 */
public class AmountTypeItem {

    private int ImgId;
    private String text;

    public AmountTypeItem() {
    }

    public AmountTypeItem(int imgId, String text) {
        ImgId = imgId;
        this.text = text;
    }

    public int getImgId() {
        return ImgId;
    }

    public void setImgId(int imgId) {
        ImgId = imgId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
