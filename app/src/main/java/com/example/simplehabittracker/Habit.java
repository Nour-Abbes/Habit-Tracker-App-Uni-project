package com.example.simplehabittracker;

public class Habit {
    private int id;
    private String title;
    private String description;
    private String category;
    private boolean done;
    private int completedCount;
    private int streak;

    public Habit(int id, String title, String description, String category, boolean done,
                 int completedCount, int streak) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.category = category;
        this.done = done;
        this.completedCount = completedCount;
        this.streak = streak;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCategory() {
        return category;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getCompletedCount() {
        return completedCount;
    }

    public void setCompletedCount(int completedCount) {
        this.completedCount = completedCount;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }
}
