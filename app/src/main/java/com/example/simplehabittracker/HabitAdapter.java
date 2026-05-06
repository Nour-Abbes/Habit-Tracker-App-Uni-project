package com.example.simplehabittracker;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
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
        void onHabitEdited(Habit habit);
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
        holder.textViewDescription.setText(habit.getDescription());
        holder.textViewCategory.setText(habit.getCategory());
        holder.textViewCompletedCount.setText("Done: " + habit.getCompletedCount() + " times");
        holder.textViewStreak.setText("Streak: " + habit.getStreak() + " days");
        setCardBackground(holder.itemView, habit);

        // Clear old listener before setting checked state.
        holder.checkBoxDone.setOnCheckedChangeListener(null);
        holder.checkBoxDone.setChecked(habit.isDone());

        holder.checkBoxDone.setOnCheckedChangeListener((buttonView, isChecked) -> {
            habit.setDone(isChecked);
            listener.onHabitChecked(habit, isChecked);
        });

        holder.buttonEdit.setOnClickListener(v -> listener.onHabitEdited(habit));
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
        TextView textViewDescription;
        TextView textViewCategory;
        TextView textViewCompletedCount;
        TextView textViewStreak;
        CheckBox checkBoxDone;
        Button buttonEdit;
        Button buttonDelete;

        public HabitViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTitle = itemView.findViewById(R.id.textViewTitle);
            textViewDescription = itemView.findViewById(R.id.textViewDescription);
            textViewCategory = itemView.findViewById(R.id.textViewCategory);
            textViewCompletedCount = itemView.findViewById(R.id.textViewCompletedCount);
            textViewStreak = itemView.findViewById(R.id.textViewStreak);
            checkBoxDone = itemView.findViewById(R.id.checkBoxDone);
            buttonEdit = itemView.findViewById(R.id.buttonEdit);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }

    private void setCardBackground(View itemView, Habit habit) {
        int color;

        if (habit.isDone()) {
            color = Color.rgb(221, 235, 222);
        } else if ("Study".equals(habit.getCategory())) {
            color = Color.rgb(211, 232, 255);
        } else if ("Health".equals(habit.getCategory())) {
            color = Color.rgb(213, 242, 218);
        } else if ("Sport".equals(habit.getCategory())) {
            color = Color.rgb(255, 229, 198);
        } else {
            color = Color.rgb(235, 235, 235);
        }

        GradientDrawable background = new GradientDrawable();
        background.setColor(color);
        background.setCornerRadius(18);
        itemView.setBackground(background);
    }
}
