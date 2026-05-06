package com.example.simplehabittracker;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class HabitAdapter extends RecyclerView.Adapter<HabitAdapter.HabitViewHolder> {
    private ArrayList<Habit> habitList;
    private OnHabitActionListener listener;

    public interface OnHabitActionListener {
        void onHabitChecked(Habit habit, boolean isChecked);
        void onHabitDeleted(Habit habit);
    }

    public HabitAdapter(ArrayList<Habit> habitList, OnHabitActionListener listener) {
        this.habitList = habitList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HabitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_habit, parent, false);
        return new HabitViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HabitViewHolder holder, int position) {
        Habit habit = habitList.get(position);

        holder.textViewTitle.setText(habit.getTitle());

        // Clear old listener before setting checked state.
        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(habit.isDone());

        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habit.setDone(isChecked);
            listener.onHabitChecked(habit, isChecked);
        });

        holder.buttonDelete.setOnClickListener(v -> listener.onHabitDeleted(habit));
    }

    @Override
    public int getItemCount() {
        return habitList.size();
    }

    public void setHabitList(ArrayList<Habit> newHabitList) {
        habitList = newHabitList;
        notifyDataSetChanged();
    }

    static class HabitViewHolder extends RecyclerView.ViewHolder {
        TextView textViewTitle;
        CheckBox checkBoxDone;
        Button buttonDelete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}
