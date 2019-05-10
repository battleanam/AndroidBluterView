package com.hanayue.ayuemobieview.user.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.databinding.ActivityRegisterBinding;
import com.hanayue.ayuemobieview.tools.HttpUtil;
import com.hanayue.ayuemobieview.tools.MD5Util;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class RegisterActivity extends AppCompatActivity {

    public static final int UPDATE_CODE = 1;
    public static final int UPDATE_CODE_ENABLED = 2;

    private ActivityRegisterBinding binding;

    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_register);
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

    /**
     * 初始化视图
     */
    private void initView() {
        binding.phoneNumber.setOnFocusChangeListener((v, focused) -> {
            if (focused) {
                binding.checkCodeLayout.setVisibility(View.VISIBLE);
            } else if (TextUtils.isEmpty(binding.phoneNumber.getText())) {
                binding.checkCodeLayout.setVisibility(View.GONE);
            }
        });

        binding.getCheckCode.setOnClickListener(v -> {
            Button button = (Button) v;
            String regex = "1[3|5|7|8|]\\d{9}";
            String phone = String.valueOf(binding.phoneNumber.getText());
            if (phone.matches(regex)) {
                JSONObject json = new JSONObject();
                json.put("to", phone);
                button.setEnabled(false);
                new Thread(() -> {
                    sendMessage(UPDATE_CODE, "30s" );
                    long millis = 30 * 1000;
                    while ((millis -= 1000) != 0) {
                        try {
                            Thread.sleep(1000);
                            sendMessage(UPDATE_CODE, millis / 1000 + "s" );

                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    sendMessage(UPDATE_CODE, "发送验证码" );
                    sendMessage(UPDATE_CODE_ENABLED, "" );
                }).start();
                HttpUtil.post(HttpUtil.USER_HOST_URL + "/sendMessage", json.toJSONString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> Toast.makeText(RegisterActivity.this, "网络连接失败，请联网后重试", Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        assert response.body() != null;
                        JSONObject res = JSONObject.parseObject(response.body().string());
                        runOnUiThread(() -> {
                            if (res.getIntValue("code") == 1000) {
                                Toast.makeText(RegisterActivity.this, "验证码发送成功，有效时长三分钟", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(RegisterActivity.this, "验证码发送失败，请稍后重试", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            } else {
                Toast.makeText(RegisterActivity.this, "手机号码格式不正确，请确认后发送", Toast.LENGTH_SHORT).show();
            }
        });

        binding.registerBtn.setOnClickListener(v -> {
            String username = binding.username.getText().toString();
            String passwd = binding.password.getText().toString();
            String phone = binding.phoneNumber.getText().toString();
            String code = binding.checkCode.getText().toString();
            String regex = "1[3|5|7|8|]\\d{9}";
            if(TextUtils.isEmpty(username)){
                Toast.makeText(this, "请输入用户名", Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(passwd)){
                Toast.makeText(this, "请输入密码", Toast.LENGTH_SHORT).show();
            }else if(passwd.length() < 6){
                Toast.makeText(this, "密码长度必须大于6位", Toast.LENGTH_SHORT).show();
            }else if(passwd.length() > 16){
                Toast.makeText(this, "密码长度过长，不能超过16位", Toast.LENGTH_SHORT).show();
            }else if(TextUtils.isEmpty(phone)){
                Toast.makeText(this, "您需要通过验证才能注册", Toast.LENGTH_SHORT).show();
            }else if(!phone.matches(regex)) {
                Toast.makeText(this, "手机号码格式不正确", Toast.LENGTH_SHORT).show();
            } else if(TextUtils.isEmpty(code)){
                Toast.makeText(this, "请输入验证码", Toast.LENGTH_SHORT).show();
            }else {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("username", username);
                jsonObject.put("password", MD5Util.md5Decode(passwd));
                jsonObject.put("account", phone);
                jsonObject.put("checkCode", code);
                HttpUtil.post(HttpUtil.USER_HOST_URL + "/doRegister", jsonObject.toJSONString(), new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        runOnUiThread(() -> {
                            Toast.makeText(RegisterActivity.this, "网络连接失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        });
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        assert response.body() != null;
                        String resStr = response.body().string();
                        JSONObject res = JSONObject.parseObject(resStr);
                        runOnUiThread(() -> {
                            if(res.getIntValue("code") == 1000){
                                Toast.makeText(RegisterActivity.this, res.getString("message"), Toast.LENGTH_SHORT).show();
                            }else {
                                Toast.makeText(RegisterActivity.this, res.getString("message"), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        });
    }

    /**
     * 发送消息给主线程
     * @param what 消息类型
     * @param str 传递的信息
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
