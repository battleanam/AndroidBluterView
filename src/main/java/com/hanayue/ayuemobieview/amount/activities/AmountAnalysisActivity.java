package com.hanayue.ayuemobieview.amount.activities;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.adapter.AmountStatAdpater;
import com.hanayue.ayuemobieview.amount.model.Amount;
import com.hanayue.ayuemobieview.amount.model.AmountStat;
import com.hanayue.ayuemobieview.databinding.ActivityAmountAnalysisBinding;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import lecho.lib.hellocharts.listener.ColumnChartOnValueSelectListener;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.PieChartData;
import lecho.lib.hellocharts.model.SliceValue;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;
import lecho.lib.hellocharts.view.PieChartView;

public class AmountAnalysisActivity extends AppCompatActivity {

    private List<AmountStat> barStats = new ArrayList<>();
    private JSONObject userInfo;
    private String type = "收入"; // 类型

    private ActivityAmountAnalysisBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_amount_analysis);
        getUserInfo();
        loadBarStats();
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        type = getIntent().getStringExtra(AmountActivity.AMOUNT_TYPE);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null) userInfo = JSONObject.parseObject(userInfoStr);
        else {
            Toast.makeText(this, "未登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 加载统计图的数据源
     */
    private void loadBarStats() {
        List<Amount> timeAmounts = LitePal
                .where("userId = ?", userInfo.getString("id"))
                .order("noteTime asc")
                .select("timeStr")
                .find(Amount.class);
        Set<String> times = new LinkedHashSet<>();
        for (Amount amount : timeAmounts) {
            String time = amount.getTimeStr();
            times.add(time.substring(0, time.indexOf("月") + 1));
        }
        List<String> keys = new ArrayList<>();
        List<Float> values = new ArrayList<>();
        for (String time : times) {
            float value = LitePal
                    .where("userId = ? and timeStr like ? and type = ?", userInfo.getString("id"), time + "%", type)
                    .order("noteTime asc")
                    .sum(Amount.class, "count", Float.class);
            barStats.add(new AmountStat(time, value));
            keys.add(time);
            values.add(value);
        }

        showBarChart(keys, values);
        if (keys.size() > 0) onBarSelect(keys.size() - 1, keys, values);
    }

    /**
     * 展示柱图
     *
     * @param xAxisKeys X轴刻度
     * @param yAxisKeys Y轴的值
     */
    private void showBarChart(List<String> xAxisKeys, List<Float> yAxisKeys) {
        ColumnChartView chart = binding.columnChart;
        chart.setZoomEnabled(false); // 禁用缩放
        chart.setOnValueTouchListener(new ColumnChartOnValueSelectListener() { // 点击事件监听
            @Override
            public void onValueSelected(int columnIndex, int subColumnIndex, SubcolumnValue subcolumnValue) {
                onBarSelect(columnIndex, xAxisKeys, yAxisKeys);
            }

            @Override
            public void onValueDeselected() {

            }
        });

        chart.setValueSelectionEnabled(true); // 设置图表数据选中后展示

        List<SubcolumnValue> subcolumnValues = new ArrayList<>(); // 子类目数据集合

        List<AxisValue> xAxisValues = new ArrayList<>();
        Axis xAxis = new Axis()
                .setHasLines(false)
                .setLineColor(ChartUtils.COLOR_BLUE)
                .setTextSize(ChartUtils.sp2px(.8f, 12))
                .setTextColor(Color.BLACK);
        Axis yAxis = new Axis()
                .setHasLines(false)
                .setInside(true)
                .setTextSize(ChartUtils.sp2px(.0f, 12));
        List<AxisValue> yAxisValues = new ArrayList<>();

        List<Column> columns = new ArrayList<>();
        Column column;

        float maxY = 0f;

        for (int i = 0; i < xAxisKeys.size(); i++) {
            subcolumnValues = new ArrayList<>();
            SubcolumnValue subcolumnValue = new SubcolumnValue(yAxisKeys.get(i), ChartUtils.COLOR_BLUE);
            subcolumnValue.setLabel(String.format(Locale.getDefault(), "%.2f", yAxisKeys.get(i)) + "元");
            subcolumnValues.add(subcolumnValue);

            xAxisValues.add(new AxisValue(i).setLabel(xAxisKeys.get(i)));

            if (yAxisKeys.get(i) > maxY) maxY = yAxisKeys.get(i);
            column = new Column(subcolumnValues).setHasLabels(true);
            columns.add(column);
        }

        maxY = ((int) (maxY / 10 + 4)) * 10;

        for (float i = 0; i < maxY; i += 10) {
            yAxisValues.add(new AxisValue(i));
        }

        xAxis.setValues(xAxisValues); // 设置x轴的刻度
        yAxis.setValues(yAxisValues);

        ColumnChartData chartData = new ColumnChartData(columns);
        chartData.setAxisYRight(yAxis);
        chartData.setAxisXBottom(xAxis);
        chartData.setValueLabelBackgroundAuto(false);
        chartData.setBaseValue(0);// 设置基准线
        chartData.setStacked(true);
//        chartData.setValueLabelBackgroundEnabled(false); // 禁用数据背景颜色
        chartData.setValueLabelBackgroundColor(Color.TRANSPARENT);
        chartData.setValueLabelTextSize(ChartUtils.sp2px(0.7f, 12));
        chart.setColumnChartData(chartData);

        Viewport viewport = new Viewport(chart.getMaximumViewport());
        viewport.left = columns.size() - 6;
        viewport.right = columns.size();
        chart.setCurrentViewport(viewport);
    }

    /**
     * 当柱图的柱子被选中的事件
     *
     * @param columnIndex 柱子的下标
     */
    private void onBarSelect(int columnIndex, List<String> xAxisKeys, List<Float> yAxisKeys) {

        if (xAxisKeys.size() > 0) {
            binding.time.setText(xAxisKeys.get(columnIndex));
            binding.count.setText(String.format(Locale.getDefault(), "%.2f", yAxisKeys.get(columnIndex)));
        } else {
            String time = DateFormat.getDateInstance(DateFormat.LONG).format(System.currentTimeMillis());
            binding.time.setText(time.substring(0, time.indexOf("月") + 1));
            binding.count.setText(String.format(Locale.getDefault(), "%.2f", 0f));
        }

        List<Amount> amounts = LitePal
                .where("userId = ? and timeStr like ? and type = ?", userInfo.getString("id"), xAxisKeys.get(columnIndex) + "%", type)
                .find(Amount.class);


        Set<String> sourceTypes = new LinkedHashSet<>();
        for (Amount amount : amounts) {
            sourceTypes.add(amount.getSourceType());
        }

        List<AmountStat> stats = new ArrayList<>();
        for (String sourceType : sourceTypes) {
            float count = LitePal
                    .where("userId = ? and timeStr like ? and type = ? and sourceType = ?", userInfo.getString("id"), xAxisKeys.get(columnIndex) + "%", type, sourceType)
                    .sum(Amount.class, "count", Float.class);
            AmountStat stat = new AmountStat(sourceType, count);
            stats.add(stat);
        }
        showPieChart(stats, yAxisKeys.get(columnIndex));

        AmountStatAdpater adpater = new AmountStatAdpater(stats, yAxisKeys.get(columnIndex));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adpater);
    }

    /**
     * 展示饼图
     *
     * @param stats 数据源
     * @param count 当月花费总量
     */
    private void showPieChart(List<AmountStat> stats, float count) {
        PieChartView pieChart = binding.pieChart;
        pieChart.setZoomEnabled(false); // 禁用缩放

        List<SliceValue> pieValues = new ArrayList<>();
        float radiusCount = 0;
        for (AmountStat stat : stats) {
            SliceValue pieValue = new SliceValue(stat.getValue(), ChartUtils.nextColor());
            float radius = stat.getValue() / count * 100;
            String label = String.format(Locale.getDefault(), "%.2f", radius);
            if (stats.indexOf(stat) == stats.size() - 1) {
                label = String.format(Locale.getDefault(), "%.2f", 100f - radiusCount);
            }
            radiusCount += radius;
            pieValue.setLabel(stat.getxAxis() + "(" + label + "%)");
            pieValues.add(pieValue);
        }

        PieChartData pieChartData = new PieChartData(pieValues);
        pieChartData.setHasLabels(true); // 设置显示标签
        pieChartData.setHasLabelsOutside(true);
        pieChartData.setValueLabelBackgroundEnabled(false);
        pieChartData.setValueLabelsTextColor(Color.GRAY);
        pieChartData.setValueLabelTextSize(ChartUtils.sp2px(.7f, 12));
        pieChartData.setSlicesSpacing(1); // 每部分之间的间隔
        pieChart.setPieChartData(pieChartData);

    }
}
