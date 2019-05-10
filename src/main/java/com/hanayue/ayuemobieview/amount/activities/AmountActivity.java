package com.hanayue.ayuemobieview.amount.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.codbking.widget.DatePickDialog;
import com.codbking.widget.bean.DateType;
import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.adapter.AmountTypeItemAdapter;
import com.hanayue.ayuemobieview.amount.model.Amount;
import com.hanayue.ayuemobieview.amount.model.AmountTypeItem;
import com.hanayue.ayuemobieview.databinding.ActivityAmountBinding;

import org.litepal.LitePal;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class AmountActivity extends AppCompatActivity {

    private static final String TAG = "AmountActivity";

    public static final Map<String, Integer> EXPAND_SOURCE_TYPES = new HashMap<>();
    public static final Map<String, Integer> INCOME_SOURCE_TYPES = new HashMap<>();
    public static String[] EXPAND_TYPE_KEYS = {"消费", "转账", "餐饮", "交通", "娱乐", "购物", "通讯", "AA", "红包", "生活", "租房", "医疗", "教育", "其他"};
    public static String[] INCOME_TYPE_KEYS = {"出售", "工资", "投资", "租金", "受赠", "转让", "其他"};

    static { // 初始化消费来源集合
        int[] expandValues = {R.mipmap.xiaofei, R.mipmap.zhuanzhang, R.mipmap.canyin, R.mipmap.jiaotong, R.mipmap.yule, R.mipmap.gouwu, R.mipmap.tongxun, R.mipmap.aa, R.mipmap.hongbao, R.mipmap.shenghuo, R.mipmap.zufang, R.mipmap.yiliao, R.mipmap.jiaoyu, R.mipmap.qita};
        for (int i = 0; i < EXPAND_TYPE_KEYS.length; i++) {
            EXPAND_SOURCE_TYPES.put(EXPAND_TYPE_KEYS[i], expandValues[i]);
        }
        int[] incomValues = {R.mipmap.chushou, R.mipmap.gongzi, R.mipmap.touzi, R.mipmap.zufang, R.mipmap.shouzeng, R.mipmap.zhuanrang, R.mipmap.qita};
        for (int i = 0; i < INCOME_TYPE_KEYS.length; i++) {
            INCOME_SOURCE_TYPES.put(INCOME_TYPE_KEYS[i], incomValues[i]);
        }
    }

    public static final String AMOUNT_TYPE = "amountType";
    public static final String AMOUNT_ID = "amountId";

    ActivityAmountBinding binding;

    Amount amount; // 当前页面的收支实例

    JSONObject userInfo;

    String type; // 添加还是编辑

    private Map<String, Integer> sourceTypes;
    private String[] typeKeys;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getUserInfo();
        type = getIntent().getStringExtra(MainActivity.NAV_TYPE);
        initAmount();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_amount);
        initView();
        Intent intent = new Intent();
        intent.putExtra("res", R.id.nav_money);
        setResult(3, intent);
    }

    /**
     * 初始化视图
     */
    private void initView() {

        sourceTypes = "支出".equals(amount.getType()) ? EXPAND_SOURCE_TYPES : INCOME_SOURCE_TYPES;
        typeKeys = "支出".equals(amount.getType()) ? EXPAND_TYPE_KEYS : INCOME_TYPE_KEYS;

        binding.toolbar.setTitle("");
        setSupportActionBar(binding.toolbar);


        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        if("edit".equals(type)) binding.count.setText(String.format(Locale.getDefault(), "%.2f", amount.getCount()));

        binding.noteTime.setText(transformNoteTimeText(new Date(amount.getNoteTime())));
        binding.noteTimeLayout.setOnClickListener(v -> showTimePicker());

        binding.moneyType.setText(amount.getMoneyType());
        binding.moneyTypeLayout.setOnClickListener(v -> showMoneyTypePicker());

        binding.sourceType.setText(amount.getSourceType());
        int sourceImgId = sourceTypes.get(amount.getSourceType());
        binding.sourceTypeImg.setImageResource(sourceImgId);
        binding.sourceTypeLayout.setOnClickListener(v -> showSourceTypePicker());

        binding.saveBtn.setOnClickListener(v -> this.handleSave());
    }

    /**
     * 初始话amount
     */
    private void initAmount() {
        if ("add".equals(type)) {
            amount = new Amount();
            amount.setUserId(userInfo.getString("id"));
            amount.setNoteTime(System.currentTimeMillis());
            amount.setType(getIntent().getStringExtra(AMOUNT_TYPE));
            amount.setMoneyType("现金");
            amount.setSourceType("支出".equals(amount.getType()) ? "消费" : "出售");
        } else {
            amount = LitePal.find(Amount.class, getIntent().getIntExtra(AMOUNT_ID, 0));
        }
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
     * 修改日期格式
     *
     * @param date 原始日期
     * @return 格式为 yyyy年MM月dd日 上午hh:mm 的日期字符串
     */
    private String transformNoteTimeText(Date date) {
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT); // 将日期转换为字符串的format 2019年01月01日 下午02:22
        String dateStr = dateFormat.format(date);
        Calendar calendar = Calendar.getInstance();
        if (date.getYear() == calendar.get(Calendar.YEAR)) { // 如果是本年的话 去掉年份
            dateStr = dateStr.substring(dateStr.indexOf("年") + 1);
        }
        return dateStr;
    }

    /**
     * 显示时间选择器
     */
    private void showTimePicker() {
        DatePickDialog dialog = new DatePickDialog(this);
        dialog.setStartDate(new Date(System.currentTimeMillis()));
        //设置上下年分限制
        dialog.setYearLimt(5);
        //设置标题
        dialog.setTitle("选择时间");
        //设置类型
        dialog.setType(DateType.TYPE_YMDHM);
        //设置消息体的显示格式，日期格式
        dialog.setMessageFormat("yyyy年MM月dd日 HH:mm");
        //设置选择回调
        dialog.setOnChangeLisener(null);
        //设置点击确定按钮回调
        dialog.setOnSureLisener(date -> {
            runOnUiThread(() -> binding.noteTime.setText(transformNoteTimeText(date)));
            amount.setNoteTime(date.getTime());
        });
        dialog.show();
    }

    /**
     * 展示选择支出类型的选择器
     */
    private void showMoneyTypePicker() {
        String[] types = {"现金", "支付宝", "微信", "银行卡", "信用卡", "其他"};
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setItems(types, (dialog, which) -> {
            runOnUiThread(() -> binding.moneyType.setText(types[which]));
            amount.setMoneyType(types[which]);
            dialog.dismiss();
        });
        builder.create().show();
    }


    /**
     * 展示选择收入支出类型的弹窗
     */
    private void showSourceTypePicker() {
        List<AmountTypeItem> items = new ArrayList<>();
        for (String SOURCE_TYPE_KEY : typeKeys) {
            items.add(new AmountTypeItem(sourceTypes.get(SOURCE_TYPE_KEY), SOURCE_TYPE_KEY));
        }
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.amount_type_dialog, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("选择" + amount.getType() + "类型");
        builder.setView(view);
        AlertDialog dialog = builder.create();
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(view.getContext(), 4);
        RecyclerView recyclerView = view.findViewById(R.id.amount_type_dialog_recycler_view);
        recyclerView.setLayoutManager(layoutManager);
        AmountTypeItemAdapter adapter = new AmountTypeItemAdapter(items, v -> {
            TextView text = v.findViewById(R.id.text);
            amount.setSourceType(text.getText().toString());
            runOnUiThread(() -> {
                binding.sourceType.setText(amount.getSourceType());
                binding.sourceTypeImg.setImageResource(sourceTypes.get(amount.getSourceType()));
                dialog.dismiss();
            });
        });
        recyclerView.setAdapter(adapter);
        dialog.show();
    }

    /**
     * 顶部菜单被创建
     *
     * @param menu 创建的菜单
     * @return bool
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if ("edit".equals(type)) getMenuInflater().inflate(R.menu.delete_only, menu);
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
            default:
        }
        return true;
    }

    /**
     * 删除一笔支出 或者 收入
     */
    private void handleDelete() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("删除收支记录").setNegativeButton("取消", (d, v) -> {

        }).setPositiveButton("确定", (d, v) -> {
            if (LitePal.delete(Amount.class, amount.getId()) > 0) {
                runOnUiThread(() -> Toast.makeText(this, "删除成功", Toast.LENGTH_SHORT).show());
                finish();
            }else {
                runOnUiThread(() -> Toast.makeText(this, "删除失败", Toast.LENGTH_SHORT).show());
            }
        }).create().show();

    }

    /**
     * 保存一笔支出或者收入
     */
    private void handleSave() {
        String count = binding.count.getText().toString();
        String remark = binding.remark.getText().toString();
        if (count.isEmpty()) {
            runOnUiThread(() -> Toast.makeText(this, "请输入金额", Toast.LENGTH_SHORT).show());
        } else {
            amount.setCount(Float.parseFloat(count));
            amount.setRemark(remark);
            if (amount.isSaved() && amount.update(amount.getId()) > 0) runOnUiThread(() -> {
                Toast.makeText(this, "更新成功", Toast.LENGTH_SHORT).show();
                finish();
            });
            else if (amount.save()) runOnUiThread(() -> {
                Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        Log.d(TAG, "handleSave: " + JSON.toJSONString(amount));
    }
}
