package com.example.simplehabittracker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private EditText editTextHabit;
    private EditText editTextDescription;
    private Spinner spinnerCategory;
    private Button buttonAdd;
    private Button buttonNewDay;
    private Button buttonLogout;
    private TextView textViewDate;
    private TextView textViewUsername;
    private TextView textViewProgress;
    private TextView textViewEmpty;
    private RecyclerView recyclerViewToDo;
    private RecyclerView recyclerViewDone;

    private DatabaseHelper databaseHelper;
    private HabitAdapter toDoAdapter;
    private HabitAdapter doneAdapter;
    private ArrayList<Habit> allHabitList;
    private ArrayList<Habit> toDoHabitList;
    private ArrayList<Habit> doneHabitList;
    private TextView selectedDayView;
    private String[] categories = {"Study", "Health", "Sport", "Other"};
    private SharedPreferences sharedPreferences;
    private int userId;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = sharedPreferences.getInt("user_id", -1);
        username = sharedPreferences.getString("username", "");

        if (userId == -1) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        editTextHabit = findViewById(R.id.editTextHabit);
        editTextDescription = findViewById(R.id.editTextDescription);
        spinnerCategory = findViewById(R.id.spinnerCategory);
        buttonAdd = findViewById(R.id.buttonAdd);
        buttonNewDay = findViewById(R.id.buttonNewDay);
        buttonLogout = findViewById(R.id.buttonLogout);
        textViewDate = findViewById(R.id.textViewDate);
        textViewUsername = findViewById(R.id.textViewUsername);
        textViewProgress = findViewById(R.id.textViewProgress);
        textViewEmpty = findViewById(R.id.textViewEmpty);
        recyclerViewToDo = findViewById(R.id.recyclerViewToDo);
        recyclerViewDone = findViewById(R.id.recyclerViewDone);

        showTodayDate();
        textViewUsername.setText("User: " + username);
        setupCalendarDays();
        setCategorySpinner(spinnerCategory);

        databaseHelper = new DatabaseHelper(this);
        allHabitList = new ArrayList<>();
        toDoHabitList = new ArrayList<>();
        doneHabitList = new ArrayList<>();

        HabitAdapter.OnHabitActionListener actionListener = new HabitAdapter.OnHabitActionListener() {
            @Override
            public void onHabitChecked(Habit habit, boolean isChecked) {
                databaseHelper.updateHabitDone(habit.getId(), isChecked);
                loadHabits();
            }

            @Override
            public void onHabitEdited(Habit habit) {
                showEditHabitDialog(habit);
            }

            @Override
            public void onHabitDeleted(Habit habit) {
                databaseHelper.deleteHabit(habit.getId());
                loadHabits();
            }
        };

        toDoAdapter = new HabitAdapter(toDoHabitList, actionListener);
        doneAdapter = new HabitAdapter(doneHabitList, actionListener);

        recyclerViewToDo.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewToDo.setAdapter(toDoAdapter);
        recyclerViewDone.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewDone.setAdapter(doneAdapter);

        buttonAdd.setOnClickListener(v -> addHabit());
        buttonNewDay.setOnClickListener(v -> startNewDay());
        buttonLogout.setOnClickListener(v -> logout());
        loadHabits();
    }

    private void setCategorySpinner(Spinner spinner) {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                categories
        );
        categoryAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(categoryAdapter);
    }

    private void showTodayDate() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM d, yyyy", Locale.getDefault());
        textViewDate.setText(dateFormat.format(calendar.getTime()));
    }

    private void setupCalendarDays() {
        TextView[] dayViews = {
                findViewById(R.id.textViewMon),
                findViewById(R.id.textViewTue),
                findViewById(R.id.textViewWed),
                findViewById(R.id.textViewThu),
                findViewById(R.id.textViewFri),
                findViewById(R.id.textViewSat),
                findViewById(R.id.textViewSun)
        };

        int today = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        int todayIndex = today == Calendar.SUNDAY ? 6 : today - Calendar.MONDAY;

        for (int i = 0; i < dayViews.length; i++) {
            TextView dayView = dayViews[i];
            dayView.setOnClickListener(v -> selectDay((TextView) v));

            if (i == todayIndex) {
                selectDay(dayView);
            }
        }
    }

    private void selectDay(TextView dayView) {
        if (selectedDayView != null) {
            selectedDayView.setBackgroundColor(0xFFEFEFEF);
            selectedDayView.setTextColor(0xFF333333);
        }

        selectedDayView = dayView;
        selectedDayView.setBackgroundColor(0xFFB7D7F2);
        selectedDayView.setTextColor(0xFF1E3A5F);
        Toast.makeText(this, "Selected " + selectedDayView.getText(), Toast.LENGTH_SHORT).show();
    }

    private void addHabit() {
        String title = editTextHabit.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String category = spinnerCategory.getSelectedItem().toString();

        if (title.isEmpty()) {
            Toast.makeText(this, "Please enter a habit", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseHelper.insertHabit(userId, title, description, category);
        editTextHabit.setText("");
        editTextDescription.setText("");
        spinnerCategory.setSelection(0);
        loadHabits();
    }

    private void showEditHabitDialog(Habit habit) {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 0);

        EditText editTitle = new EditText(this);
        editTitle.setHint("Habit title");
        editTitle.setText(habit.getTitle());
        layout.addView(editTitle);

        EditText editDescription = new EditText(this);
        editDescription.setHint("Habit description");
        editDescription.setText(habit.getDescription());
        layout.addView(editDescription);

        Spinner editCategory = new Spinner(this);
        setCategorySpinner(editCategory);
        editCategory.setSelection(getCategoryPosition(habit.getCategory()));
        layout.addView(editCategory);

        new AlertDialog.Builder(this)
                .setTitle("Edit Habit")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    String title = editTitle.getText().toString().trim();
                    String description = editDescription.getText().toString().trim();
                    String category = editCategory.getSelectedItem().toString();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Please enter a habit title", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    databaseHelper.updateHabit(habit.getId(), title, description, category);
                    loadHabits();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getCategoryPosition(String category) {
        for (int i = 0; i < categories.length; i++) {
            if (categories[i].equals(category)) {
                return i;
            }
        }

        return 0;
    }

    private void startNewDay() {
        databaseHelper.resetHabitsForNewDay(userId);
        loadHabits();
        Toast.makeText(this, "New day started", Toast.LENGTH_SHORT).show();
    }

    private void logout() {
        sharedPreferences.edit().clear().apply();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void loadHabits() {
        allHabitList = databaseHelper.getAllHabits(userId);
        toDoHabitList.clear();
        doneHabitList.clear();

        for (Habit habit : allHabitList) {
            if (habit.isDone()) {
                doneHabitList.add(habit);
            } else {
                toDoHabitList.add(habit);
            }
        }

        toDoAdapter.setHabitList(toDoHabitList);
        doneAdapter.setHabitList(doneHabitList);
        updateProgressAndEmptyMessage();
    }

    private void updateProgressAndEmptyMessage() {
        int completed = doneHabitList.size();
        int total = allHabitList.size();

        textViewProgress.setText("Progress: " + completed + " / " + total + " completed today");

        if (total == 0) {
            textViewEmpty.setVisibility(View.VISIBLE);
        } else {
            textViewEmpty.setVisibility(View.GONE);
        }
    }
}
