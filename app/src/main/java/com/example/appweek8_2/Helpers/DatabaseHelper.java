package com.example.appweek8_2.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Messages.db";
    private static final int DATABASE_VERSION = 2;
    public static final String TABLE_MESSAGES = "messages";
    public static final String TABLE_USERS= "users";

    public static final String COLUMN_ID = "id";


    private static final String COLUMN_USER_USERNAME = "username";

    public static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";
    private static final String COLUMN_USER_ID = "user_id";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_USERNAME + " TEXT UNIQUE NOT NULL);");

        // Create messages table
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                COLUMN_USER_ID + " INTEGER NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_USER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + ") ON DELETE CASCADE);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public long addUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        long userId = db.insert(TABLE_USERS, null, values);
        db.close();
        return userId;
    }

    public int getUserId(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_USERNAME + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }
        cursor.close();
        db.close();
        return userId;
    }

    public List<String> getAllUsersExcept(String currentUsername) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> users = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USER_USERNAME + " != ?";
        Cursor cursor = db.rawQuery(query, new String[]{currentUsername});
        if (cursor.moveToFirst()) {
            do {
                String username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME));
                users.add(username);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return users;
    }

    public void addMessage(String username, String message) {
        int userId = getUserId(username);
        if (userId != -1) {
            SQLiteDatabase db = this.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(COLUMN_USER_ID, userId);
            values.put(COLUMN_MESSAGE, message);
            db.insert(TABLE_MESSAGES, null, values);
            db.close();
        }
    }

    public List<String> getMessagesForUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> messages = new ArrayList<>();
        int userId = getUserId(username);

        if (userId != -1) {
            String query = "SELECT " + COLUMN_MESSAGE + " FROM " + TABLE_MESSAGES +
                    " WHERE " + COLUMN_USER_ID + " = ?";
            Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

            if (cursor.moveToFirst()) {
                do {
                    messages.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE)));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }

        db.close();
        return messages;
    }

    public void deleteMessage(String username, String message) {
        int userId = getUserId(username);
        if (userId != -1) {
            SQLiteDatabase db = this.getWritableDatabase();
            String whereClause = COLUMN_USER_ID + " = ? AND " + COLUMN_MESSAGE + " = ?";
            String[] whereArgs = {String.valueOf(userId), message};
            db.delete(TABLE_MESSAGES, whereClause, whereArgs);
            db.close();
        }
    }


    public void deleteUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "username = ?";
        String[] whereArgs = {username};
        db.delete(TABLE_USERS, whereClause, whereArgs);
        db.close();
    }
}
