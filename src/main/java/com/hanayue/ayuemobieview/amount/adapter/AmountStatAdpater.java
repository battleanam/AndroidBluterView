package com.hanayue.ayuemobieview.amount.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.model.AmountStat;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AmountStatAdpater extends RecyclerView.Adapter<AmountStatAdpater.ViewHolder> {

    private static final Map<String, Integer> EXPAND_SOURCE_TYPES = new HashMap<>();
    private static final Map<String, Integer> INCOME_SOURCE_TYPES = new HashMap<>();
    private static String[] EXPAND_TYPE_KEYS = {"消费", "转账", "餐饮", "交通", "娱乐", "购物", "通讯", "AA", "红包", "生活", "租房", "医疗", "教育", "出售", "工资", "投资", "租金", "受赠", "转让", "其他"};

    static { // 初始化消费来源集合
        int[] expandValues = {R.mipmap.xiaofei, R.mipmap.zhuanzhang, R.mipmap.canyin, R.mipmap.jiaotong, R.mipmap.yule, R.mipmap.gouwu, R.mipmap.tongxun, R.mipmap.aa, R.mipmap.hongbao, R.mipmap.shenghuo, R.mipmap.zufang, R.mipmap.yiliao, R.mipmap.jiaoyu, R.mipmap.chushou, R.mipmap.gongzi, R.mipmap.touzi, R.mipmap.zufang, R.mipmap.shouzeng, R.mipmap.zhuanrang, R.mipmap.qita};
        for (int i = 0; i < EXPAND_TYPE_KEYS.length; i++) {
            EXPAND_SOURCE_TYPES.put(EXPAND_TYPE_KEYS[i], expandValues[i]);
        }
    }

    private Context context;
    List<AmountStat> amounts;
    float total;

    public AmountStatAdpater(List<AmountStat> amounts, float total) {
        this.amounts = amounts;
        this.total = total;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) {
            context = viewGroup.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.amount_stats_item, viewGroup, false);
        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        AmountStat amount = amounts.get(i);
        viewHolder.count.setText(String.format(Locale.getDefault(), "%.2f", amount.getValue()));
        viewHolder.sourceType.setText(amount.getxAxis());
        viewHolder.progressBar.setProgress((int) (amount.getValue() / total * 100));
        viewHolder.img.setImageResource(EXPAND_SOURCE_TYPES.get(amount.getxAxis()));
    }

    @Override
    public int getItemCount() {
        return amounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        CircleImageView img;
        TextView count;
        TextView sourceType;
        ProgressBar progressBar;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView;
            img = relativeLayout.findViewById(R.id.img);
            count = relativeLayout.findViewById(R.id.count);
            sourceType = relativeLayout.findViewById(R.id.source_type);
            progressBar = relativeLayout.findViewById(R.id.progress_bar);
        }
    }
}
