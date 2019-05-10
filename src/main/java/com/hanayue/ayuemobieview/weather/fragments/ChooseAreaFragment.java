package com.hanayue.ayuemobieview.weather.fragments;


import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.tools.HttpUtil;
import com.hanayue.ayuemobieview.weather.activities.WeatherActivity;
import com.hanayue.ayuemobieview.weather.model.City;
import com.hanayue.ayuemobieview.weather.model.County;
import com.hanayue.ayuemobieview.weather.model.Province;
import com.hanayue.ayuemobieview.weather.util.ChooseAreaUtility;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * 选择区域的Fragment
 */
public class ChooseAreaFragment extends Fragment {

    public static final int LEVEL_PROVINCE = 0;
    public static final int LEVEL_CITY = 1;
    public static final int LEVEL_COUNTY = 2;

    private ProgressDialog progressDialog; // 加载中的弹窗

    private TextView titleView; // 顶部的标题

    private Button backBtn; // 返回按钮

    private ListView listView; // 展示的列表

    private ArrayAdapter<String> adapter; // ListView 的适配器

    private List<String> dataList = new ArrayList<>(); // 展示的数据

    private List<Province> provinceList; // 省份列表

    private List<City> cityList; // 城市列表

    private List<County> countyList; // 区县列表

    private Province currentProvince; // 当前选中的省份

    private City currentCity; // 当前选中的城市

    private int currentLevel; // 当前在选择什么


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.choose_area, container, false);
        init(view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        listView.setOnItemClickListener((adapterView, view, position, id) -> {
            if (currentLevel == LEVEL_PROVINCE) {
                currentProvince = provinceList.get(position);
                queryCity();
            } else if (currentLevel == LEVEL_CITY) {
                currentCity = cityList.get(position);
                String cityName = currentProvince.getProvinceName() + currentCity.getCityName();
                if (currentCity.getIsLeaf() == 0) queryCounty();
                else {
                    WeatherActivity activity = (WeatherActivity) getActivity();
                    assert activity != null;
                    activity.drawerLayout.closeDrawers();
                    activity.swipeRefresh.setRefreshing(true);
                    activity.requestWeather(currentCity.getValue());
                    activity.titleCity.setText(cityName);
                }
            } else if (currentLevel == LEVEL_COUNTY) {
                String cityName = currentCity.getCityName() + countyList.get(position).getCountyName();
                WeatherActivity activity = (WeatherActivity) getActivity();
                assert activity != null;
                activity.drawerLayout.closeDrawers();
                activity.swipeRefresh.setRefreshing(true);
                activity.requestWeather(countyList.get(position).getValue());
                activity.titleCity.setText(cityName);
            }
        });

        backBtn.setOnClickListener(v -> {
            if (currentLevel == LEVEL_COUNTY) {
                queryCity();
            } else if (currentLevel == LEVEL_CITY) {
                queryProvince();
            }
        });

        queryProvince();
    }

    /**
     * 初始化界面中的各个组件
     *
     * @param view 视图
     */
    private void init(View view) {
        titleView = view.findViewById(R.id.choose_area_title);
        backBtn = view.findViewById(R.id.choose_area_back_btn);
        listView = view.findViewById(R.id.choose_area_list_view);
        adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, dataList);
        listView.setAdapter(adapter);
    }

    /**
     * 显示加载中的弹窗
     */
    private void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage("正在加载");
            progressDialog.setCanceledOnTouchOutside(false); // 禁止点击其他地方关闭弹窗
        }
        progressDialog.show();
    }

    /**
     * 关闭加载中弹窗
     */
    private void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }


    /**
     * 从服务器加载数据
     *
     * @param id    需要加载下级区域的节点的ID
     * @param level 加载省还是市还是区县 取顶部常量的值
     */
    private void queryFromServer(int id, final int level) {
        showProgressDialog();
        RequestBody body = new FormBody.Builder()
                .add("pid", id + "")
                .build();
        JSONObject json = new JSONObject();
        json.put("pid", id);
        HttpUtil.post(HttpUtil.LOCATION_HOST_URL + "/selectListByParam", json.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                getActivity().runOnUiThread(() -> {
                    closeProgressDialog();
                    Toast.makeText(getContext(), "加载失败", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseText = response.body().string();
                boolean result = false;
                JSONObject res = JSONObject.parseObject(responseText);
                if (res.getInteger("code") == 1000) {
                    if (LEVEL_PROVINCE == level) {
                        result = ChooseAreaUtility.handleProvinceResponse(res.getJSONArray("data"));
                    } else if (LEVEL_CITY == level) {
                        result = ChooseAreaUtility.handleCityResponse(res.getJSONArray("data"));
                    } else if (LEVEL_COUNTY == level) {
                        result = ChooseAreaUtility.handleCountyResponse(res.getJSONArray("data"));
                    }

                }
                if (result) {
                    getActivity().runOnUiThread(() -> {
                        closeProgressDialog();
                        if (LEVEL_PROVINCE == level) {
                            queryProvince();
                        } else if (LEVEL_CITY == level) {
                            queryCity();
                        } else {
                            queryCounty();
                        }
                    });
                }
            }
        });
    }

    /**
     * 查询省份
     */
    private void queryProvince() {
        titleView.setText("中国");
        backBtn.setVisibility(View.GONE);
        provinceList = LitePal.findAll(Province.class);
        if (provinceList.size() > 0) {
            dataList.clear();
            for (Province province : provinceList) {
                dataList.add(province.getProvinceName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_PROVINCE;
        } else {
            queryFromServer(-1, LEVEL_PROVINCE);
        }
    }

    /**
     * 查询省份
     */
    private void queryCity() {
        String title = "中国-" + currentProvince.getProvinceName();
        titleView.setText(title);
        backBtn.setVisibility(View.VISIBLE);
        cityList = LitePal.where("provinceid = ?", String.valueOf(currentProvince.getProvinceCode())).find(City.class);
        if (cityList.size() > 0) {
            dataList.clear();
            for (City city : cityList) {
                dataList.add(city.getCityName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_CITY;
        } else {
            queryFromServer(currentProvince.getProvinceCode(), LEVEL_CITY);
        }
    }

    /**
     * 查询省份
     */
    private void queryCounty() {
        String title = "中国-" + currentProvince.getProvinceName() + "-" + currentCity.getCityName();
        titleView.setText(title);
        backBtn.setVisibility(View.VISIBLE);
        countyList = LitePal.where("cityId = ?", String.valueOf(currentCity.getCityCode())).find(County.class);
        if (countyList.size() > 0) {
            dataList.clear();
            for (County county : countyList) {
                dataList.add(county.getCountyName());
            }
            adapter.notifyDataSetChanged();
            listView.setSelection(0);
            currentLevel = LEVEL_COUNTY;
        } else {
            queryFromServer(currentCity.getCityCode(), LEVEL_COUNTY);
        }
    }


}
