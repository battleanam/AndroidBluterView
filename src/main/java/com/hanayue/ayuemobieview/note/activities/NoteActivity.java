package com.hanayue.ayuemobieview.note.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.ActivityNoteBinding;
import com.hanayue.ayuemobieview.note.model.Note;
import com.hanayue.ayuemobieview.tools.HttpUtil;

import org.litepal.LitePal;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class NoteActivity extends AppCompatActivity {

    public static final String NOTE_ID = "note_id";
    public static final String NOTE_TITLE = "note_title";
    public static final String NOTE_CONTENT = "note_content";
    public static final String NOTE_TIME = "note_time";
    public static final String NOTE_CREATE_TIME = "note_create_time";


    private ActivityNoteBinding binding;
    private String type;
    private Note note;
    private boolean serverAlso = false; // 是否同时删除服务器备份
    private JSONObject userInfo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_note);
        getUserInfo();
        initView();
        Intent intent = new Intent();
        intent.putExtra("res", R.id.nav_note);
        setResult(1, intent);
    }

    /**
     * 从简单存储中获取登录信息
     */
    private void getUserInfo() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String userInfoStr = preferences.getString("userInfo", null);
        if (userInfoStr != null) userInfo = JSONObject.parseObject(userInfoStr);
        else {
            Toast.makeText(this, "未登录", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * 初始化视图
     */
    private void initView() {


        type = getIntent().getStringExtra(MainActivity.NAV_TYPE); // 获取是添加还是编辑

        initNote(); // 初始化note 需要在拿到 type 后

        binding.toolbar.setTitle("edit".equals(type) ? "编辑便签" : "添加便签");
        setSupportActionBar(binding.toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        binding.noteTime.setText(getNoteTime());

        binding.noteContent.setText(note.getContent());
        binding.noteContent.setOnFocusChangeListener((v, b) -> {
            if (b) { // 如果是获得焦点的话
                setToolBarMenu("edit", binding.toolbar.getMenu());
            } else {
                note.setContent(((EditText) v).getText().toString());
            }
        });

    }

    /**
     * 初始化note
     */
    private void initNote() {
        note = new Note();
        if ("edit".equals(type)) {
            note.setId(getIntent().getLongExtra(NOTE_ID, -1));
            String content = getIntent().getStringExtra(NOTE_CONTENT);
            if (content.length() <= 20) {
                note.setTitle(content);
            } else {
                note.setTitle(content.substring(0, 19));
            }
            note.setContent(content);
            note.setNoteTime(getIntent().getLongExtra(NOTE_TIME, new Date().getTime()));
            note.setCreateTime(getIntent().getLongExtra(NOTE_CREATE_TIME, new Date().getTime()));
        }
    }

    /**
     * 设置页面中展示的时间
     */
    private String getNoteTime() {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        long date = System.currentTimeMillis(); // 取当前系统时间
        if ("edit".equals(type)) { // 如果是编辑状态的话 取传递过来的时间
            date = note.getNoteTime();
        }
        return "最近更新 " + dateFormat.format(date);
    }

    /**
     * 设置顶部工具栏的菜单
     *
     * @param status 状态  edit 正在输入 none新建文件 内容为空的时候 detail编辑且不在输入的时候对于已创建的来讲
     */
    private void setToolBarMenu(String status, Menu menu) {
        menu.clear();
        if ("edit".equals(status)) {
            getMenuInflater().inflate(R.menu.toolbar_note_focus, menu);
        } else if ("none".equals(status)) {
            getMenuInflater().inflate(R.menu.toolbar, menu);
        } else if ("detail".equals(status)) {
            getMenuInflater().inflate(R.menu.toolbar_note, menu);
        }
    }

    /**
     * 顶部菜单被创建
     *
     * @param menu 创建的菜单
     * @return bool
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("edit".equals(type)) getMenuInflater().inflate(R.menu.toolbar_note, menu);
        else getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    /**
     * 菜单项的点击事件
     *
     * @param item 菜单项
     * @return bool
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // 返回
                finish();
                break;
            case R.id.delete:
                // 删除
                handleDelete();
                break;
            case R.id.backup:
                // 上传服务器
                handleBackup();
                break;
            case R.id.save:
                // 保存
                handleSave();
                break;
            default:
        }
        return true;
    }

    /**
     * 处理删除
     * 询问删除 对话框设置同时删除服务器备份
     */
    private void handleDelete() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String loadFromServer = preferences.getString("LoadNoteFormServer", null);
        boolean onlyServer = loadFromServer != null && loadFromServer.length() > 0;
        String[] checkItems = {"同时删除云备份"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (!onlyServer) { // 不是只删除服务器的话 添加上复选框
            builder.setMultiChoiceItems(checkItems, null, (dialog, which, isChecked) ->
                    serverAlso = isChecked
            );
        }
        builder.setTitle("删除便签")
                .setPositiveButton("确定", (dialog, id) -> {

                    if (!onlyServer && !serverAlso && LitePal.delete(Note.class, note.getId()) > 0) {
                        Toast.makeText(this, "本地删除成功", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    if (serverAlso || onlyServer) {
                        // todo 访问后台接口删除备份
                        String userId = userInfo.getString("id");
                        JSONObject param = new JSONObject();
                        param.put("userId", userId);
                        param.put("noteId", note.getId());
                        HttpUtil.post(HttpUtil.NOTE_HOST_URL + "/delete", param.toJSONString(), new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                runOnUiThread(() -> Toast.makeText(NoteActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show());
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                if (!onlyServer && LitePal.delete(Note.class, note.getId()) > 0) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(NoteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        finish();
                                    });
                                } else if (onlyServer) {
                                    assert response.body() != null;
                                    String resStr = response.body().string();
                                    JSONObject res = JSONObject.parseObject(resStr);
                                    runOnUiThread(() -> {
                                        Toast.makeText(NoteActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                        if (res.getIntValue("code") == 1000) finish();
                                    });

                                }
                            }
                        });
                    }
                }).setNegativeButton("取消", (dialog, id) -> {

        });
        builder.create().show();
    }

    /**
     * 处理上传服务器
     */
    private void handleBackup() {
        JSONObject noteJson = new JSONObject();
        String userId = userInfo.getString("id");
        noteJson.put("userId", userId);
        noteJson.put("noteId", note.getId());
        noteJson.put("title", note.getTitle());
        noteJson.put("content", note.getContent());
        noteJson.put("createTime", note.getCreateTime());
        noteJson.put("noteTime", note.getNoteTime());
        HttpUtil.post(HttpUtil.NOTE_HOST_URL + "/insertOne", noteJson.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(NoteActivity.this, "服务器连接失败", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                String resStr = response.body().string();
                JSONObject res = JSONObject.parseObject(resStr);
                runOnUiThread(() -> Toast.makeText(NoteActivity.this, res.getString("message"), Toast.LENGTH_SHORT).show());
            }
        });
    }

    /**
     * 处理保存到本地
     */
    private void handleSave() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String loadFromServer = preferences.getString("LoadNoteFormServer", null);
        boolean saveToServer = loadFromServer != null && loadFromServer.length() > 0;

        binding.noteLayout.clearFocus();
        hintKeyboard();
        setToolBarMenu("detail", binding.toolbar.getMenu());
        String content = binding.noteContent.getText().toString();
        if (content.length() <= 20) {
            note.setTitle(content);
        } else {
            note.setTitle(content.substring(0, 19));
        }
        if (userInfo != null) {
            note.setUserId(userInfo.getString("id"));
        }
        note.setContent(content);
        note.setNoteTime(System.currentTimeMillis());
        binding.noteTime.setText(getNoteTime());

        if(saveToServer){ // 如果查看服务器便签的化 点击保存后上传到服务器
            handleBackup();
            return;
        }

        if ("add".equals(type)) {
            note.setCreateTime(System.currentTimeMillis());
            if (note.save()) Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show();
        } else {
            if (note.update(note.getId()) > 0)
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
            else Toast.makeText(this, "更新失败", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 关掉软键盘
     */
    private void hintKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm.isActive() && getCurrentFocus() != null) {
            if (getCurrentFocus().getWindowToken() != null) {
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    @Override
    protected void onDestroy() {
        Note a = LitePal.find(Note.class, note.getId());
        if (note.getContent().isEmpty() && a != null) { // 如果是内容是空的 删除
            note.delete();
        }
        super.onDestroy();
    }
}
