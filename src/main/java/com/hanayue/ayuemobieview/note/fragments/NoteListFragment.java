package com.hanayue.ayuemobieview.note.fragments;

import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.FragmentNoteListBinding;
import com.hanayue.ayuemobieview.note.adapter.NoteAdapter;
import com.hanayue.ayuemobieview.note.model.Note;
import com.hanayue.ayuemobieview.tools.HttpUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NoteListFragment extends Fragment {

    FragmentNoteListBinding binding;
    JSONObject userInfo;

    private List<Note> notes = new ArrayList<>();
    private NoteAdapter noteAdapter;

    private boolean loadFromServer = false; // 是否从服务器获取数据

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_note_list, container, false);
        getUserInfo();
        initNotes();
        initView();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.swipeRefresh.setRefreshing(true);
        refreshNotes();
    }

    private void initView() {

        initNotes();

        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
        editor.putString("LoadNoteFormServer", loadFromServer ? "1111" : "");
        editor.apply();

        // 展示便签列表
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.recyclerView.setLayoutManager(layoutManager);
        noteAdapter = new NoteAdapter(notes);
        binding.recyclerView.setAdapter(noteAdapter);

        //下拉刷新
        binding.swipeRefresh.setColorSchemeResources(R.color.colorPrimary);
        binding.swipeRefresh.setOnRefreshListener(this::refreshNotes);

        binding.fab.setOnClickListener(v -> {
            loadFromServer = !loadFromServer;
            editor.putString("LoadNoteFormServer", loadFromServer ? "1111" : "");
            editor.apply();
            binding.fab.setImageResource(loadFromServer ? R.mipmap.sd_storage : R.mipmap.storage);
            refreshNotes();
            MainActivity activity = (MainActivity) getActivity();
            activity.setTitle(loadFromServer ? "云便签管理" : "本地便签管理");
        });
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null) userInfo = JSONObject.parseObject(userInfoStr);
        else if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                Toast.makeText(getActivity(), "未登录", Toast.LENGTH_SHORT).show();
                getActivity().finish();
            });
        }
    }

    /**
     * 初始化便签列表
     */
    private void initNotes() {
        notes.addAll(LitePal.order("createTime desc").find(Note.class));
    }

    /**
     * 更新便签列表
     */
    private void refreshNotes() {
        new Thread(() -> {
            notes.clear();
            if (loadFromServer) {
                requesServer();
            } else {
                notes.addAll(LitePal.where("userId = ?", userInfo.getString("id")).order("createTime desc").find(Note.class));
                getActivity().runOnUiThread(() -> {
                    noteAdapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        }).start();
    }

    /**
     * 从服务器获取数据
     */
    private void requesServer() {

        String userId = userInfo.getString("id");
        JSONObject param = new JSONObject();
        param.put("userId", userId);
        HttpUtil.post(HttpUtil.NOTE_HOST_URL + "/selectListByParam", param.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                notes.clear();
                getActivity().runOnUiThread(() -> {
                    Toast.makeText(getActivity(), "服务器连接失败", Toast.LENGTH_SHORT).show();
                    noteAdapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);
                });
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String resStr = response.body().string();
                JSONObject res = JSONObject.parseObject(resStr);
                notes.clear();
                if (res.getIntValue("code") == 1000) {
                    notes.addAll(res.getJSONArray("data").toJavaList(Note.class));
                }
                getActivity().runOnUiThread(() -> {
                    noteAdapter.notifyDataSetChanged();
                    binding.swipeRefresh.setRefreshing(false);
                });
            }
        });
    }
}
