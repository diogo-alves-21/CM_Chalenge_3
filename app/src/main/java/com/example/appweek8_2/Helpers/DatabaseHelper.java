package com.example.appweek8_2.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.SQLClientInfoException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "Messages.db";
    private static final int DATABASE_VERSION = 3;

    // Tables
    public static final String TABLE_USERS = "users";
    public static final String TABLE_CONVERSATIONS = "conversations";
    public static final String TABLE_MESSAGES = "messages";

    // Common columns
    public static final String COLUMN_ID = "id";

    // Users table columns
    private static final String COLUMN_USER_USERNAME = "username";

    // Conversations table columns
    private static final String COLUMN_PARTICIPANT_1_ID = "participant_1_id";
    private static final String COLUMN_PARTICIPANT_2_ID = "participant_2_id";
    private static final String COLUMN_NOTIFICATION = "notification";

    // Messages table columns
    public static final String COLUMN_CONVERSATION_ID = "conversation_id";
    public static final String COLUMN_SENDER_ID = "sender_id";
    public static final String COLUMN_MESSAGE = "message";
    private static final String COLUMN_TIMESTAMP = "timestamp";


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users table
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_USERNAME + " TEXT UNIQUE NOT NULL);");

        // Create conversations table
        db.execSQL("CREATE TABLE " + TABLE_CONVERSATIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_PARTICIPANT_1_ID + " INTEGER NOT NULL, " +
                COLUMN_PARTICIPANT_2_ID + " INTEGER NOT NULL, " +
                COLUMN_NOTIFICATION + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + COLUMN_PARTICIPANT_1_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_PARTICIPANT_2_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "));" );

        // Create messages table
        db.execSQL("CREATE TABLE " + TABLE_MESSAGES + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CONVERSATION_ID + " INTEGER NOT NULL, " +
                COLUMN_SENDER_ID + " INTEGER NOT NULL, " +
                COLUMN_MESSAGE + " TEXT NOT NULL, " +
                COLUMN_TIMESTAMP + " TEXT, " +
                "FOREIGN KEY (" + COLUMN_CONVERSATION_ID + ") REFERENCES " + TABLE_CONVERSATIONS + "(" + COLUMN_ID + "), " +
                "FOREIGN KEY (" + COLUMN_SENDER_ID + ") REFERENCES " + TABLE_USERS + "(" + COLUMN_ID + "));" );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Add a new user
    public long addUser(String username) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verificar se o usuário já existe
        String query = "SELECT COUNT(*) FROM users WHERE username = ?";
        Cursor cursor = db.rawQuery(query, new String[]{username});

        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            if (count > 0) {
                // O usuário já existe
                Log.d("DatabaseHelper", "O usuário " + username + " já existe.");
                cursor.close();
                return -1;
            }
        }

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER_USERNAME, username);
        long userId = db.insert(TABLE_USERS, null, values);
        db.close();
        return userId;
    }

    // Create a conversation
    public long createConversation(int participant1Id, int participant2Id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PARTICIPANT_1_ID, participant1Id);
        values.put(COLUMN_PARTICIPANT_2_ID, participant2Id);
        values.put(COLUMN_NOTIFICATION, "ligada");
        long conversationId = db.insert(TABLE_CONVERSATIONS, null, values);
        db.close();
        return conversationId;
    }

    // Get conversation ID given two participants in a specific order
    public Integer getConversationId(int participant1Id, int participant2Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Integer conversationId = null;

        // SQL query to find a conversation with the specified participants in the given order
        String query = "SELECT " + COLUMN_ID + " FROM " + TABLE_CONVERSATIONS +
                " WHERE " + COLUMN_PARTICIPANT_1_ID + " = ? AND " + COLUMN_PARTICIPANT_2_ID + " = ?";

        // Execute the query with the two participant IDs as parameters
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(participant1Id),
                String.valueOf(participant2Id)
        });

        if (cursor.moveToFirst()) {
            // Get the conversation ID if found
            conversationId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID));
        }

        cursor.close();
        db.close();
        return conversationId;
    }

    // Get conversation ID given two participants in a specific order
    public String getNotificationByConversationId(int conversationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String notification = null;

        // SQL query to find a conversation with the specified participants in the given order
        String query = "SELECT " + COLUMN_NOTIFICATION + " FROM " + TABLE_CONVERSATIONS +
                " WHERE " + COLUMN_ID + " = ?";

        // Execute the query with the two participant IDs as parameters
        Cursor cursor = db.rawQuery(query, new String[]{
                String.valueOf(conversationId)
        });

        if (cursor.moveToFirst()) {
            // Get the conversation ID if found
            notification = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTIFICATION));
        }

        cursor.close();
        db.close();
        return notification;
    }

    public void updateNotificationByConversationId(int conversationId, String newState) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTIFICATION, newState);

        db.update(TABLE_CONVERSATIONS, values, COLUMN_ID + " = ?", new String[]{String.valueOf(conversationId)});
        db.close();
    }

    // Get conversations by participant ID
    public List<Integer> getConversationsByParticipantId(int participant1Id) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Integer> participant2Ids = new ArrayList<>();

        // SQL query to get all conversations where the participant is involved
        String query = "SELECT " + COLUMN_PARTICIPANT_2_ID + " FROM " + TABLE_CONVERSATIONS +
                " WHERE " + COLUMN_PARTICIPANT_1_ID + " = ?";

        // Execute the query with the participantId as parameter
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(participant1Id)});

        if (cursor.moveToFirst()) {
            do {
                int participant2Id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_PARTICIPANT_2_ID));
                participant2Ids.add(participant2Id);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return participant2Ids;
    }

    // Add a message to a conversation
    public long addMessage(int conversationId, int senderId, String message, String timestamp) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CONVERSATION_ID, conversationId);
        values.put(COLUMN_SENDER_ID, senderId);
        values.put(COLUMN_MESSAGE, message);
        values.put(COLUMN_TIMESTAMP, timestamp);
        long messageId = db.insert(TABLE_MESSAGES, null, values);
        db.close();
        return messageId;
    }

    // Get messages for a conversation
    public List<Message> getMessagesForConversation(int conversationId) {
        SQLiteDatabase db = this.getReadableDatabase();
        List<Message> messages = new ArrayList<>();

        // SQL query to select all relevant columns: sender_id, message, and timestamp
        String query = "SELECT " + COLUMN_SENDER_ID + ", " + COLUMN_MESSAGE + ", " + COLUMN_TIMESTAMP +
                " FROM " + TABLE_MESSAGES +
                " WHERE " + COLUMN_CONVERSATION_ID + " = ?";

        // Execute the query with the provided conversationId
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(conversationId)});

        if (cursor.moveToFirst()) {
            do {
                int senderId = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SENDER_ID));
                String messageText = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MESSAGE));
                String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP));

                // Create a new Message object and add it to the list
                Message message = new Message(senderId, messageText, timestamp);
                messages.add(message);
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return messages;
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

    public String getUserById(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String username = null;

        // Consulta para selecionar o nome de usuário com base no ID do usuário
        String query = "SELECT " + COLUMN_USER_USERNAME + " FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            // Obtém o nome de usuário da coluna correspondente
            username = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER_USERNAME));
        }

        cursor.close();
        db.close();
        return username;
    }

    // Get all users except the given username
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

    // Get all users
    public List<String> getAllUsers() {
        SQLiteDatabase db = this.getReadableDatabase();
        List<String> users = new ArrayList<>();
        String query = "SELECT " + COLUMN_USER_USERNAME + " FROM " + TABLE_USERS; // Seleciona apenas o nome de usuário
        Cursor cursor = db.rawQuery(query, null);

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

    public String getMostRecentTimestamp(int conversationID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String mostRecentTimestamp = null;

        // Query para buscar o timestamp mais recente da tabela de mensagens
        String query = "SELECT MAX(" + COLUMN_TIMESTAMP + ") AS mostRecent FROM " + TABLE_MESSAGES +
                " WHERE " + COLUMN_CONVERSATION_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(conversationID)});

        if (cursor.moveToFirst()) {
            mostRecentTimestamp = cursor.getString(cursor.getColumnIndexOrThrow("mostRecent"));
        }

        cursor.close();
        db.close();
        return mostRecentTimestamp;
    }

    public void deleteConversation(int conversationId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verifica se a conversa existe antes de tentar apagá-la
        String query = "SELECT * FROM " + TABLE_CONVERSATIONS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(conversationId)});

        if (cursor.moveToFirst()) {
            // Deleta a conversa (e automaticamente suas mensagens associadas devido à FK com ON DELETE CASCADE)
            String whereClause = COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(conversationId)};
            db.delete(TABLE_CONVERSATIONS, whereClause, whereArgs);
        }

        cursor.close();
        db.close();
    }

    public void deleteUser(int userId) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Verifica se o usuário existe antes de tentar apagá-lo
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            // Deleta o usuário
            String whereClause = COLUMN_ID + " = ?";
            String[] whereArgs = {String.valueOf(userId)};
            db.delete(TABLE_USERS, whereClause, whereArgs);
        }

        cursor.close();
        db.close();
    }
}
