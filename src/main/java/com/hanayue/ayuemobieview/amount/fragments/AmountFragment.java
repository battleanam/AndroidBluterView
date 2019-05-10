package com.hanayue.ayuemobieview.amount.fragments;


import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.activities.AmountAnalysisActivity;
import com.hanayue.ayuemobieview.amount.adapter.AmountGroupAdapter;
import com.hanayue.ayuemobieview.amount.model.Amount;
import com.hanayue.ayuemobieview.amount.model.AmountGroup;
import com.hanayue.ayuemobieview.databinding.FragmentAmountBinding;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class AmountFragment extends Fragment {

    private static final String TAG = "AmountFragment";

    List<AmountGroup> data = new ArrayList<>();
    AmountGroupAdapter groupAdapter;
    FragmentAmountBinding binding;
    JSONObject userInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_amount, container, false);
        getUserInfo();
        initTitle();
        loadData();
        binding.recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }
        });

        binding.toMonthBtn.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), AmountAnalysisActivity.class));
        });
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        initTitle();
        data.clear();
        loadData();
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null) userInfo = JSONObject.parseObject(userInfoStr);
        else {
            Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    /**
     * 初始化顶部标题
     */
    private void initTitle() {
        DateFormat dayFormat = DateFormat.getDateInstance(DateFormat.LONG);
        String today = dayFormat.format(System.currentTimeMillis());
        float incomeToday = LitePal
                .where("timeStr like ? and type = ? and userId = ?", today + "%", "收入", userInfo.getString("id"))
                .sum(Amount.class, "count", Float.class);
        String text = String.format(Locale.getDefault(), "%.1f", incomeToday);
        binding.incomeToday.setText(text);
        float expandToday = LitePal
                .where("timeStr like ? and type = ? and userId = ?", today + "%", "支出", userInfo.getString("id"))
                .sum(Amount.class, "count", Float.class);
        text = String.format(Locale.getDefault(), "%.1f", expandToday);
        binding.expandToday.setText(String.valueOf(text));

        String month = today.substring(0, today.indexOf("月"));
        float incomeMonth = LitePal
                .where("timeStr like ? and type = ? and userId = ?", month + "%", "收入", userInfo.getString("id"))
                .sum(Amount.class, "count", Float.class);
        text = String.format(Locale.getDefault(), "%.1f", incomeMonth);
        binding.incomeMonth.setText(String.valueOf(text));
        float expandMonth = LitePal
                .where("timeStr like ? and type = ? and userId = ?", month + "%", "支出", userInfo.getString("id"))
                .sum(Amount.class, "count", Float.class);
        text = String.format(Locale.getDefault(), "%.1f", expandMonth);
        binding.expandMonth.setText(String.valueOf(text));
    }


    /**
     *
     */
    private void loadData() {
        List<AmountGroup> data = new ArrayList<>();
//        int total = LitePal
//                .where("userId = ?", userInfo.getString("id"))
//                .order("noteTime desc")
//                .select("timeStr")
//                .count(Amount.class);
//        int pageSize = 10;
        List<Amount> timeAmounts = LitePal
                .where("userId = ?", userInfo.getString("id"))
                .order("noteTime desc")
                .select("timeStr")
                .find(Amount.class);
        Set<String> times = new LinkedHashSet<>();
        for (Amount amount : timeAmounts) {
            times.add(amount.getTimeStr());
        }
        for (String time : times) {
            List<Amount> amounts = LitePal
                    .where("userId = ? and timeStr = ?", userInfo.getString("id"), time)
                    .order("noteTime asc")
                    .find(Amount.class);
            data.add(new AmountGroup(time, amounts));
        }
//          .limit(page)
//                .offset((page - 1) * pageSize)
        if (groupAdapter == null) {
            this.data.addAll(data);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            binding.recyclerView.setLayoutManager(layoutManager);
            groupAdapter = new AmountGroupAdapter(this.data);
            binding.recyclerView.setAdapter(groupAdapter);
        } else {
            if (getActivity() != null) {
                this.data.clear();
                this.data.addAll(data);
                getActivity().runOnUiThread(() -> groupAdapter.notifyDataSetChanged());
            }
        }
//            if (total - page * pageSize == total % pageSize) { // 如果到了最后一页 退出循环
//                break;
//            }
//            page++;

    }


}
