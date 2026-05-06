package com.example.simplehabittracker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "habits.db";
    private static final int DATABASE_VERSION = 4;

    private static final String TABLE_HABITS = "habits";
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USER_ID = "user_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_DESCRIPTION = "description";
    private static final String COLUMN_CATEGORY = "category";
    private static final String COLUMN_IS_DONE = "is_done";
    private static final String COLUMN_COMPLETED_COUNT = "completed_count";
    private static final String COLUMN_STREAK = "streak";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + TABLE_HABITS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USER_ID + " INTEGER, "
                + COLUMN_TITLE + " TEXT, "
                + COLUMN_DESCRIPTION + " TEXT, "
                + COLUMN_CATEGORY + " TEXT, "
                + COLUMN_IS_DONE + " INTEGER, "
                + COLUMN_COMPLETED_COUNT + " INTEGER, "
                + COLUMN_STREAK + " INTEGER)";

        db.execSQL(createTable);
        createUsersTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_DESCRIPTION + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_CATEGORY + " TEXT DEFAULT 'Other'");
        }
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_COMPLETED_COUNT + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_STREAK + " INTEGER DEFAULT 0");
        }
        if (oldVersion < 4) {
            createUsersTable(db);
            db.execSQL("ALTER TABLE " + TABLE_HABITS + " ADD COLUMN " + COLUMN_USER_ID + " INTEGER DEFAULT 0");
        }
    }

    private void createUsersTable(SQLiteDatabase db) {
        String createUsers = "CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " ("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + COLUMN_USERNAME + " TEXT, "
                + COLUMN_PASSWORD + " TEXT)";

        db.execSQL(createUsers);
    }

    public long registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);

        long result = db.insert(TABLE_USERS, null, values);
        db.close();

        return result;
    }

    public int checkUserLogin(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();
        int userId = -1;

        Cursor cursor = db.query(
                TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{username, password},
                null,
                null,
                null
        );

        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        db.close();

        return userId;
    }

    public void insertHabit(int userId, String title, String description, String category) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_ID, userId);
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IS_DONE, 0);
        values.put(COLUMN_COMPLETED_COUNT, 0);
        values.put(COLUMN_STREAK, 0);

        db.insert(TABLE_HABITS, null, values);
        db.close();
    }

    public ArrayList<Habit> getAllHabits(int userId) {
        ArrayList<Habit> habitList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.query(
                TABLE_HABITS,
                null,
                COLUMN_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null,
                null,
                COLUMN_ID + " DESC"
        );

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION));
                String category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY));
                boolean isDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1;
                int completedCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COUNT));
                int streak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK));

                habitList.add(new Habit(id, title, description, category, isDone, completedCount, streak));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return habitList;
    }

    public void updateHabitDone(int id, boolean isDone) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DONE, isDone ? 1 : 0);

        if (isDone) {
            Cursor cursor = db.query(
                    TABLE_HABITS,
                    new String[]{COLUMN_IS_DONE, COLUMN_COMPLETED_COUNT, COLUMN_STREAK},
                    COLUMN_ID + "=?",
                    new String[]{String.valueOf(id)},
                    null,
                    null,
                    null
            );

            int completedCount = 0;
            int streak = 0;
            boolean wasDone = false;

            if (cursor.moveToFirst()) {
                wasDone = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_DONE)) == 1;
                completedCount = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COMPLETED_COUNT));
                streak = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STREAK));
            }

            cursor.close();

            if (!wasDone) {
                values.put(COLUMN_COMPLETED_COUNT, completedCount + 1);
                values.put(COLUMN_STREAK, streak + 1);
            }
        }

        db.update(TABLE_HABITS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void updateHabit(int id, String title, String description, String category) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, title);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_CATEGORY, category);

        db.update(TABLE_HABITS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void resetHabitsForNewDay(int userId) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DONE, 0);

        db.update(TABLE_HABITS, values, COLUMN_USER_ID + "=?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public void deleteHabit(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_HABITS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
