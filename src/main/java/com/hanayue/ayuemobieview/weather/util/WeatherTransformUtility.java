package com.hanayue.ayuemobieview.weather.util;

import com.alibaba.fastjson.JSONObject;

public class WeatherTransformUtility {

    /**
     * 将天气代码转换为天气
     *
     * @param skycon 天气代码
     * @return
     */
    public static String transformSkycon(String skycon) {
        switch (skycon) {
            case "CLEAR_DAY":
            case "CLEAR_NIGHT":
                return "晴";
            case "PARTLY_CLOUDY_DAY":
            case "PARTLY_CLOUDY_NIGHT":
                return "多云";
            case "CLOUDY":
                return "阴";
            case "WIND":
                return "大风";
            case "HAZE":
                return "雾霾";
            case "RAIN":
                return "降雨";
            case "SNOW":
                return "降雪";
            default:
                return "晴";
        }
    }

    /**
     * 将风向角度转换为中文
     *
     * @param wind 角度
     * @return 中文
     */
    public static String transformWind(JSONObject wind) {
        double direction = wind.getDouble("direction");
        double speed = wind.getDouble("speed");
        String res = "北风";
        if (direction <= 22.5 || direction > 337.5) res = "北风";
        else if (direction <= 67.5) res = "东北风";
        else if (direction <= 112.5) res = "东风";
        else if (direction <= 157.5) res = "东南风";
        else if (direction <= 202.5) res = "南风";
        else if (direction <= 247.5) res = "西南风";
        else if (direction <= 292.5) res = "西风";
        else if (direction <= 337.5) res = "西北风";
        String speedStr = transformWindSpeed(speed);
        if ("无风".equals(speedStr)) return speedStr;
        return res + transformWindSpeed(speed);
    }

    /**
     * 将风速转换为风力等级
     *
     * @param speed 风速
     * @return 风力等级
     */
    private static String transformWindSpeed(double speed) {
        if (speed <= 0.2) return "无风";
        else if (speed <= 1.5) return "一级";
        else if (speed <= 3.3) return "二级";
        else if (speed <= 5.4) return "三级";
        else if (speed <= 7.9) return "四级";
        else if (speed <= 10.7) return "五级";
        else if (speed <= 13.8) return "六级";
        else if (speed <= 17.1) return "七级";
        else if (speed <= 20.7) return "八级";
        else if (speed <= 24.4) return "九级";
        else if (speed <= 28.4) return "十级";
        else if (speed <= 32.6) return "十二级";
        else if (speed <= 36.9) return "十三级";
        else if (speed <= 41.4) return "十四级";
        else if (speed <= 46.1) return "十五级";
        else if (speed <= 50.9) return "十六级";
        else if (speed <= 56.0) return "十七级";
        else return "十七级以上";
    }

}
