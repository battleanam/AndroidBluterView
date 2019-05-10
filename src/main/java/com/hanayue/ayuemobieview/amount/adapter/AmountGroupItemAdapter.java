package com.hanayue.ayuemobieview.amount.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.activities.AmountActivity;
import com.hanayue.ayuemobieview.amount.model.Amount;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class AmountGroupItemAdapter extends RecyclerView.Adapter<AmountGroupItemAdapter.ViewHolder>{

    private static final Map<String, Integer> EXPAND_SOURCE_TYPES = new HashMap<>();
    private static final Map<String, Integer> INCOME_SOURCE_TYPES = new HashMap<>();
    private static String[] EXPAND_TYPE_KEYS = {"消费", "转账", "餐饮", "交通", "娱乐", "购物", "通讯", "AA", "红包", "生活", "租房", "医疗", "教育", "其他"};
    private static String[] INCOME_TYPE_KEYS = {"出售", "工资", "投资", "租金", "受赠", "转让", "其他"};

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

    private Context context;
    private List<Amount> amounts;

    public AmountGroupItemAdapter(List<Amount> amounts) {
        this.amounts = amounts;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) context = viewGroup.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.amount_group_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.itemLayout.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            int id = amounts.get(position).getId();
            Intent intent = new Intent(context, AmountActivity.class);
            intent.putExtra(AmountActivity.AMOUNT_ID, id);
            intent.putExtra(MainActivity.NAV_TYPE, "edit");
            context.startActivity(intent);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Amount amount = amounts.get(i);
        DateFormat format = DateFormat.getTimeInstance(DateFormat.SHORT);
        viewHolder.noteTime.setText(format.format(amount.getNoteTime()));
        viewHolder.moneyType.setText(amount.getMoneyType());
        viewHolder.count.setText(String.format(Locale.getDefault(),"%.2f",amount.getCount()));
        viewHolder.sourceType.setText(amount.getSourceType());
        viewHolder.icon.setImageResource("支出".equals(amount.getType()) ? EXPAND_SOURCE_TYPES.get(amount.getSourceType()) : INCOME_SOURCE_TYPES.get(amount.getSourceType()));
        viewHolder.type.setImageResource("支出".equals(amount.getType()) ? R.mipmap.zhichu2 : R.mipmap.shouru2);
    }

    @Override
    public int getItemCount() {
        return amounts.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout itemLayout;
        CircleImageView icon;
        CircleImageView type;
        TextView sourceType;
        TextView moneyType;
        TextView count;
        TextView noteTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            itemLayout = (LinearLayout) itemView;
            icon = itemLayout.findViewById(R.id.amount_item_icon);
            type = itemLayout.findViewById(R.id.type);
            sourceType = itemLayout.findViewById(R.id.amount_item_source_type);
            moneyType = itemLayout.findViewById(R.id.amount_item_money_type);
            count = itemLayout.findViewById(R.id.amount_item_count);
            noteTime = itemLayout.findViewById(R.id.amount_item_note_time);
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

}
