package com.hanayue.ayuemobieview.notice.fragments;


import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.FragmentNoticeBinding;
import com.hanayue.ayuemobieview.notice.adapter.NoticeAdapter;
import com.hanayue.ayuemobieview.notice.model.Notice;

import org.litepal.LitePal;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoticeFragment extends Fragment {

    FragmentNoticeBinding binding;
    JSONObject userInfo;
    NoticeAdapter adapter;
    List<Notice> notices = new ArrayList<>();

    public NoticeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notice, container, false);
        getUserInfo();
        initNotices();

        //下拉刷新
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        binding.swipeRefresh.setOnRefreshListener(this::initNotices);

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        initNotices();
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null)
            userInfo = com.alibaba.fastjson.JSONObject.parseObject(userInfoStr);
        else if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            });
        }
    }

    private void initNotices() {
        List<Notice> notices = LitePal
                .where("userId = ? and isNoticed = ?", userInfo.getString("id"), "0")
                .order("noticeTime")
                .find(Notice.class);
        if (adapter == null) {
            this.notices.addAll(notices);
            adapter = new NoticeAdapter(this.notices);
            RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            binding.recyclerView.setLayoutManager(layoutManager);
            binding.recyclerView.setAdapter(adapter);
        } else {
            this.notices.clear();
            this.notices.addAll(notices);
            this.adapter.notifyDataSetChanged();
        }
        binding.swipeRefresh.setRefreshing(false);
    }

}
