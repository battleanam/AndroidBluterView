package com.hanayue.ayuemobieview.amount.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.model.AmountTypeItem;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class AmountTypeItemAdapter extends RecyclerView.Adapter<AmountTypeItemAdapter.ViewHolder>{

    private Context context;
    private List<AmountTypeItem> items;
    private View.OnClickListener onItemClickListener;

    public AmountTypeItemAdapter(List<AmountTypeItem> items, View.OnClickListener onItemClickListener){
        this.items = items;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null){
            context = viewGroup.getContext();
        }

        View view = LayoutInflater.from(context).inflate(R.layout.anount_type_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.relativeLayout.setOnClickListener(onItemClickListener);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        AmountTypeItem item = items.get(i);
        viewHolder.imageView.setImageResource(item.getImgId());
        viewHolder.textView.setText(item.getText());
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        CircleImageView imageView;
        TextView textView;

        ViewHolder(View view){
            super(view);
            relativeLayout = (RelativeLayout) view;
            imageView = view.findViewById(R.id.img);
            textView = view.findViewById(R.id.text);
        }
    }
}
