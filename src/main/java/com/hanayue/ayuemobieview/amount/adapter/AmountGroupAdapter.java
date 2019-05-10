package com.hanayue.ayuemobieview.amount.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.amount.model.AmountGroup;

import java.util.List;

public class AmountGroupAdapter extends RecyclerView.Adapter<AmountGroupAdapter.ViewHolder> {

    private Context context;
    private List<AmountGroup> data;

    public AmountGroupAdapter(List<AmountGroup> data) {
        this.data = data;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (context == null) {
            context = viewGroup.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.amount_group, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        AmountGroup amountGroup = data.get(i);
        viewHolder.time.setText(amountGroup.getTime());
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        AmountGroupItemAdapter adapter = new AmountGroupItemAdapter(amountGroup.getAmounts());
        viewHolder.recyclerView.setLayoutManager(layoutManager);
        viewHolder.recyclerView.setAdapter(adapter);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout relativeLayout;
        TextView time;
        RecyclerView recyclerView;

        ViewHolder(View view) {
            super(view);
            relativeLayout = (RelativeLayout) view;
            time = view.findViewById(R.id.time);
            recyclerView = view.findViewById(R.id.recycler_view);
        }
    }
}
