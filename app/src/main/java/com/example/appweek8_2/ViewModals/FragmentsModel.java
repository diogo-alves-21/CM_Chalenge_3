package com.example.appweek8_2.ViewModals;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appweek8_2.Helpers.DatabaseHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Closeable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentsModel extends ViewModel {

    protected MutableLiveData<Boolean> loginSuccess;
    protected FirebaseFirestore firestore;
    protected MutableLiveData<String> username = new MutableLiveData<>("");
    protected MutableLiveData<List<String>> messages = new MutableLiveData<>();
    protected MutableLiveData<List<String>> users = new MutableLiveData<>();
    protected MutableLiveData<String> selectedUsername = new MutableLiveData<>();
    protected DatabaseHelper dbHelper;

    public FragmentsModel() {
        this.loginSuccess = new MutableLiveData<>(false);
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void setDatabaseHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }


    public LiveData<List<String>> getMessages() {
        return messages;
    }

    public void loadMessages() {
        if (dbHelper != null && username.getValue() != null &&!username.getValue().isEmpty()) {
            List<String> userMessages = dbHelper.getMessagesForUser(username.getValue());
            messages.setValue(userMessages); // Update LiveData with fetched messages
        }
    }


    public void deleteMessage(String username, String message) {
        if (dbHelper != null) {
            dbHelper.deleteMessage(username, message);
            loadMessages();
        }
    }

    public void addMessage(String username, String message) {
        if (dbHelper != null) {
            dbHelper.addMessage(username, message);
            loadMessages();
        }
    }


    public int getUserId(String username) {
        if (dbHelper != null) {
            return dbHelper.getUserId(username);
        }
        return -1;
    }


    public void loadContacts() {
        Log.d("Username", "my username: " + username.getValue());
        if (dbHelper != null && !getUsername().isEmpty()) {
            List<String> contacts = dbHelper.getAllUsersExcept(username.getValue());
            users.setValue(contacts);
        }
    }

    public LiveData<List<String>> getContacts() {
        return users;
    }

    public void addUser(String username) {
        if (dbHelper != null) {
            dbHelper.addUser(username);
            //Log.d("Username", "Users updated: " + username);
            loadContacts();
        }
    }

    public void deleteUser(String username) {
        if (dbHelper != null) {
            dbHelper.deleteUser(username);
            loadContacts(); // Refresh the contacts list
        }
    }

    public String getSelectedUsername() {
        return selectedUsername.getValue();
    }

    public void setSelectedUsername(int index) {
        if (index == -1){
            this.selectedUsername = new MutableLiveData<>("");
        }
        else {
            this.selectedUsername.setValue(users.getValue().get(index));
        }

    }

    public String getUsername() {
        return username.getValue();
    }

    public void setUsername(String username) {
        this.username.setValue(username);  // Set username using LiveData
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }


    public void verifyUsername(String username) {
        if (username.isEmpty()) {
            loginSuccess.setValue(false);
            return;
        }
        firestore.collection("Users").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setUsername(username);
                        Log.d("Username", "login: " + getUsername());
                        loginSuccess.setValue(true);
                    } else {
                        loginSuccess.setValue(false);
                    }
                })
                .addOnFailureListener(e -> {
                    loginSuccess.setValue(false);
                });
    }

    public void createUser(String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        firestore.collection("Users").document(username).set(userData)
                .addOnSuccessListener(aVoid -> {
                    loginSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loginSuccess.setValue(false);
                });
    }
}
