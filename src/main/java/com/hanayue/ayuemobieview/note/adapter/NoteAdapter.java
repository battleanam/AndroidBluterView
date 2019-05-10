package com.hanayue.ayuemobieview.note.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.hanayue.ayuemobieview.MainActivity;
import com.hanayue.ayuemobieview.R;
import com.hanayue.ayuemobieview.note.activities.NoteActivity;
import com.hanayue.ayuemobieview.note.model.Note;

import java.text.DateFormat;
import java.util.List;

public class NoteAdapter extends RecyclerView.Adapter<NoteAdapter.ViewHolder> {


    private Context context;
    private List<Note> notes;

    static class ViewHolder extends RecyclerView.ViewHolder{

        CardView cardView;
        TextView noteTitle;
        TextView noteContent;
        TextView noteTime;

        ViewHolder(View view){
            super(view);
            cardView = (CardView) view;
            noteTitle = view.findViewById(R.id.note_title);
            noteContent = view.findViewById(R.id.note_content);
            noteTime = view.findViewById(R.id.note_time);
        }
    }

    public NoteAdapter(List<Note> notes){
        this.notes = notes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if(context == null){
            context = viewGroup.getContext();
        }
        View view = LayoutInflater.from(context).inflate(R.layout.note_item, viewGroup, false);
        final ViewHolder holder = new ViewHolder(view);
        holder.cardView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            Note note = notes.get(position);
            Intent intent = new Intent(context, NoteActivity.class);
            intent.putExtra(MainActivity.NAV_TYPE, "edit");
            intent.putExtra(NoteActivity.NOTE_ID, note.getId());
            intent.putExtra(NoteActivity.NOTE_TITLE, note.getTitle());
            intent.putExtra(NoteActivity.NOTE_CONTENT, note.getContent());
            intent.putExtra(NoteActivity.NOTE_TIME, note.getNoteTime());
            intent.putExtra(NoteActivity.NOTE_CREATE_TIME, note.getCreateTime());
            context.startActivity(intent);
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Note note = notes.get(i);
        viewHolder.noteTitle.setText(note.getTitle());
        viewHolder.noteContent.setText(note.getContent());
        DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.SHORT);
        String noteTime = dateFormat.format(note.getCreateTime());
        viewHolder.noteTime.setText(noteTime);
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }
}
