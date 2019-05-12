package com.hanayue.ayuemobieview.user;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.ActivityLoginBinding;
import com.hanayue.ayuemobieview.tools.HttpUtil;
import com.hanayue.ayuemobieview.tools.MD5Util;
import com.hanayue.ayuemobieview.user.activities.RegisterActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    public static final int UPDATE_CODE = 1;
    public static final int UPDATE_CODE_ENABLED = 2;

    ActivityLoginBinding binding;

    private Handler handler;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        handler = new Handler(msg -> {
            switch (msg.what) {
                case UPDATE_CODE:
                    binding.getCheckCode.setText(msg.getData().getString("str"));
                    return true;
                case UPDATE_CODE_ENABLED:
                    binding.getCheckCode.setEnabled(true);
                    return true;
                default:
                    return false;
            }
        });
        initView();
    }

    private void initView() {
        binding.codeLoginBtn.setOnClickListener(v -> {
            if (((TextView) v).getText().equals("验证码登录")) {
                ((TextView) v).setText("账号密码登录");
                binding.password.setVisibility(View.GONE);
                binding.checkCodeLayout.setVisibility(View.VISIBLE);
            } else {
                ((TextView) v).setText("验证码登录");
                binding.password.setVisibility(View.VISIBLE);
                binding.checkCodeLayout.setVisibility(View.GONE);
            }
        });

        binding.registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        binding.getCheckCode.setOnClickListener(v -> {
            Button button = (Button) v;
            String regex = "1[3|5|7|8|]\\d{9}";
            String phone = String.valueOf(binding.phoneNumber.getText());
            if (TextUtils.isEmpty(phone)) {
                Toast.makeText(LoginActivity.this, "手机号不能为空", Toast.LENGTH_SHORT).show();
            } else if (phone.matches(regex)) {
                JSONObject json = new JSONObject();
                json.put("to", phone);
                button.setEnabled(false);
                new Thread(() -> {
                    sendMessage(UPDATE_CODE, "30s");
                    long millis = 30 * 1000;
                    while ((millis -= 1000) != 0) {
                        try {
                            Thread.sleep(1000);
                            sendMessage(UPDATE_CODE, millis / 1000 + "s");

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendMessage(UPDATE_CODE, "发送验证码");
                    sendMessage(UPDATE_CODE_ENABLED, "");
                }).start();
                HttpUtil.post(HttpUtil.USER_HOST_URL + "/sendMessage", json.toJSONString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络连接失败，请联网后重试", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        assert response.body() != null;
                        JSONObject res = JSONObject.parseObject(response.body().string());
                        runOnUiThread(() -> {
                            if (res.getIntValue("code") == 1000) {
                                Toast.makeText(LoginActivity.this, "验证码发送成功，有效时长三分钟", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(LoginActivity.this, "验证码发送失败，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                Toast.makeText(LoginActivity.this, "手机号码格式不正确，请确认后发送", Toast.LENGTH_SHORT).show();
            }
        });

        binding.loginBtn.setOnClickListener(v -> {
            String account = binding.phoneNumber.getText().toString();
            String password = binding.password.getText().toString();
            String checkCode = binding.checkCode.getText().toString();
            String regex = "1[3|5|7|8|]\\d{9}";
            if (TextUtils.isEmpty(account)) {
                Toast.makeText(this, "请输入手机号码", Toast.LENGTH_SHORT).show();
            } else if (!account.matches(regex)) {
                Toast.makeText(this, "手机号码不合法", Toast.LENGTH_SHORT).show();
            } else if (binding.codeLoginBtn.getText().equals("验证码登录")) {
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
                } else if (password.length() < 6) {
                    Toast.makeText(this, "密码长度必须大于6位", Toast.LENGTH_SHORT).show();
                } else if (password.length() > 16) {
                    Toast.makeText(this, "密码长度过长，不能超过16位", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject json = new JSONObject();
                    json.put("account", account);
                    json.put("password", MD5Util.md5Decode(password));
                    doLogin(json);
                }
            } else {
                if (TextUtils.isEmpty(checkCode)) {
                    Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
                } else {
                    JSONObject json = new JSONObject();
                    json.put("account", account);
                    json.put("checkCode", checkCode);
                    doLogin(json);
                }
            }
        });
    }

    /**
     * 登陆
     *
     * @param json 登陆的信息
     */
    private void doLogin(JSONObject json) {
        HttpUtil.post(HttpUtil.USER_HOST_URL + "/doLogin", json.toJSONString(), new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "网络连接失败，请稍后重试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                assert response.body() != null;
                JSONObject res = JSONObject.parseObject(response.body().string());
                runOnUiThread(() -> {
                    if(res.getIntValue("code") == 1000){
                        JSONObject data = res.getJSONObject("data");
                        // 记录下登录用户的信息
                        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(LoginActivity.this).edit();
                        editor.putString("userInfo", data.toJSONString());
                        editor.apply();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    }else {
                        Toast.makeText(LoginActivity.this, res.getString("message"), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    /**
     * 发送消息给主线程
     *
     * @param what 消息类型
     * @param str  传递的信息
     */
    private void sendMessage(int what, String str) {
        Message message = new Message();
        message.what = what;
        if (!TextUtils.isEmpty(str)) {
            Bundle bundle = new Bundle();
            bundle.putString("str", str);
            message.setData(bundle);
        }
        handler.sendMessage(message);
    }
}
