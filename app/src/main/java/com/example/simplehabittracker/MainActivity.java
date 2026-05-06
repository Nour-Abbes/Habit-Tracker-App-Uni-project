package com.example.simplehabittracker;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private EditText editTextHabit;
    private Button buttonAdd;
    private RecyclerView recyclerViewHabits;

    private DatabaseHelper databaseHelper;
    private HabitAdapter habitAdapter;
    private ArrayList<Habit> habitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextHabit = findViewById(R.id.editTextHabit);
        buttonAdd = findViewById(R.id.buttonAdd);
        recyclerViewHabits = findViewById(R.id.recyclerViewHabits);

        databaseHelper = new DatabaseHelper(this);
        habitList = databaseHelper.getAllHabits();

        habitAdapter = new HabitAdapter(habitList, new HabitAdapter.OnHabitActionListener() {
            @Override
            public void onHabitChecked(Habit habit, boolean isChecked) {
                databaseHelper.updateHabitDone(habit.getId(), isChecked);
            }

            @Override
            public void onHabitDeleted(Habit habit) {
                databaseHelper.deleteHabit(habit.getId());
                loadHabits();
            }
        });

        recyclerViewHabits.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewHabits.setAdapter(habitAdapter);

        buttonAdd.setOnClickListener(v -> addHabit());
    }

    private void addHabit() {
        String title = editTextHabit.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a habit", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.insertHabit(title);
        editTextHabit.setText("");
        loadHabits();
    }

    private void loadHabits() {
        habitList = databaseHelper.getAllHabits();
        habitAdapter.setHabitList(habitList);
    }
}
