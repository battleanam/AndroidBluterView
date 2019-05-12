package com.hanayue.ayuemobieview.notice.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.notice.activities.NoticeActivity;
import com.hanayue.ayuemobieview.notice.model.Notice;

import java.text.DateFormat;
import java.util.List;

public class NoticeAdapter extends RecyclerView.Adapter<NoticeAdapter.ViewHolder> {

    private List<Notice> notices;
    private Context context;
    private boolean triggerClick = true; // 能不能出发点击事件

    public NoticeAdapter(List<Notice> notices) {
        this.notices = notices;
    }

    public NoticeAdapter(List<Notice> notices, boolean triggerClick) {
        this.notices = notices;
        this.triggerClick = triggerClick;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) {
            context = viewGroup.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.notice_item, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Notice notice = notices.get(i);
        String index = i + 1 + "";
        viewHolder.index.setText(index);
        viewHolder.title.setText(notice.getTitle());
        String noticeTime = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT).format(notice.getNoticeTime());
        viewHolder.noticeTime.setText(noticeTime.substring(noticeTime.indexOf("年") + 1));
        String shiftTime = "提前" + notice.getShiftTime() + "日";
        viewHolder.shiftTime.setText(shiftTime);
        viewHolder.content.setText(notice.getContent());
        if (triggerClick)
            viewHolder.relativeLayout.setOnClickListener(v -> {
                Intent intent = new Intent(context, NoticeActivity.class);
                intent.putExtra(NoticeActivity.NOTICE_ID, notice.getId());
                intent.putExtra(MainActivity.NAV_TYPE, "edit");
                context.startActivity(intent);
            });
    }

    @Override
    public int getItemCount() {
        return notices.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView index;
        TextView title;
        TextView noticeTime;
        TextView shiftTime;
        TextView content;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            relativeLayout = (RelativeLayout) itemView;
            index = itemView.findViewById(R.id.index);
            title = itemView.findViewById(R.id.title);
            noticeTime = itemView.findViewById(R.id.notice_time);
            shiftTime = itemView.findViewById(R.id.shift_time);
            content = itemView.findViewById(R.id.content);
        }
    }
}
