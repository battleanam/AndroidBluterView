package com.hanayue.ayuemobieview.weather.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.weather.model.City;
import com.hanayue.ayuemobieview.weather.model.County;
import com.hanayue.ayuemobieview.weather.model.Province;

public class ChooseAreaUtility {

    /**
     * 解析处理服务器返回的省级数据
     *
     * @param allProvinces 服务器返回的json字符串
     * @return 处理结果
     */
    public static boolean handleProvinceResponse(JSONArray allProvinces) {
        for (int i = 0; i < allProvinces.size(); i++) {
            JSONObject provinceJson = allProvinces.getJSONObject(i);
            Province province = new Province();
            province.setProvinceCode(provinceJson.getInteger("id"));
            province.setProvinceName(provinceJson.getString("name"));
            province.save();
        }
        return true;
    }

    /**
     * 解析处理服务器返回的市级数据
     *
     * @param allCities 服务器返回的json字符串
     * @return 处理结果
     */
    public static boolean handleCityResponse(JSONArray allCities) {
        for (int i = 0; i < allCities.size(); i++) {
            JSONObject cityJson = allCities.getJSONObject(i);
            City city = new City();
            city.setCityCode(cityJson.getInteger("id"));
            city.setCityName(cityJson.getString("name"));
            city.setProvinceId(cityJson.getInteger("pid"));
            city.setIsLeaf(cityJson.getInteger("isLeaf"));
            if (city.getIsLeaf() == 1) city.setValue(cityJson.getString("value"));
            city.save();
        }
        return true;

    }

    /**
     * 解析处理服务器返回的区县级数据
     *
     * @param allCounties 服务器返回的json字符串
     * @return 处理结果
     */
    public static boolean handleCountyResponse(JSONArray allCounties) {
        for (int i = 0; i < allCounties.size(); i++) {
            JSONObject countyJson = allCounties.getJSONObject(i);
            County county = new County();
            county.setCountyCode(countyJson.getInteger("id"));
            county.setCountyName(countyJson.getString("name"));
            county.setValue(countyJson.getString("value"));
            county.setCityId(countyJson.getInteger("pid"));
            county.save();
        }
        return true;
    }

}
