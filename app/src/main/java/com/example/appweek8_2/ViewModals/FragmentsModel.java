package com.example.appweek8_2.ViewModals;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.appweek8_2.Helpers.DatabaseHelper;
import com.example.appweek8_2.Helpers.Message;
import com.example.appweek8_2.Helpers.MqttHelper;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentsModel extends ViewModel {

    private static final String TAG = "FragmentsModel";

    protected MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private boolean loginAttempted = false;
    protected FirebaseFirestore firestore;
    protected MutableLiveData<String> username = new MutableLiveData<>("");
    protected MutableLiveData<List<String>> conversations = new MutableLiveData<>();
    protected MutableLiveData<List<String>> conversationsNotification = new MutableLiveData<>();
    protected MutableLiveData<List<Message>> messages = new MutableLiveData<>();
    protected MutableLiveData<List<String>> users = new MutableLiveData<>();
    protected MutableLiveData<String> selectedUsername = new MutableLiveData<>();
    protected DatabaseHelper dbHelper;
    private final MutableLiveData<Integer> conversationTrigger = new MutableLiveData<>();

    Handler mainHandler = new Handler(Looper.getMainLooper());


    // MQTT Helper
    private MqttHelper mqttHelper;

    public FragmentsModel() {
        this.loginSuccess = new MutableLiveData<>(false);
        this.firestore = FirebaseFirestore.getInstance();
    }

    public void setDatabaseHelper(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Initialize MQTT helper
    public void setMqttHelper() {
        String topic = getUsername()+"/challenge3/ADN";
        mqttHelper = new MqttHelper(topic);

        // Set up the message listener
        mqttHelper.setMqttConnectionListener(new MqttHelper.MqttConnectionListener() {
            @Override
            public void onConnected() {
                Log.d(TAG, "MQTT Connected!");
            }

            @Override
            public void onConnectionFailed(Throwable exception) {
                Log.e(TAG, "MQTT Connection Failed: " + exception.getMessage());
            }
        });

        mqttHelper.setMessageCallback((message) -> {
            // Log da mensagem completa recebida
            String payload = new String(message.getPayload());
            Log.d(TAG, "Message received on topic " + topic + ": " + payload);

            try {
                // Converte o payload JSON para um objeto JSONObject
                JSONObject jsonObject = new JSONObject(payload);
                String username = jsonObject.getString("username");
                String message_receive = jsonObject.getString("mensagem");

                Log.d(TAG, "Extracted username: " + username);
                Log.d(TAG, "Extracted message: " + message_receive);

                // Adiciona a mensagem ao modelo usando os dados extraídos
                // Para exemplo, assumimos que `conversation` é 1 e você deve adaptar conforme necessário
                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                int sender = getUserId(username); // Implementar lógica para mapear username para sender ID
                int conversation = getConversationId(getUserId(getUsername()),sender);

                addMessage(conversation, sender, message_receive, timestamp);

                mainHandler.post(() -> {
                    loadMessagesForConversation(conversation);
                });

            } catch (JSONException e) {
                Log.d(TAG, "Failed to parse JSON payload: " + e.getMessage());
            }
        });
    }

    // Method to send a message
    public void sendMessage(String topic, String message) {
        if (mqttHelper != null) {
            mqttHelper.publishMessage(topic, message);
        }
    }


    // Getter para o LiveData
    public LiveData<List<String>> getConversations() {
        return conversations;
    }

    // Getter para o LiveData
    public LiveData<List<String>> getConversationsNotification() {
        return conversationsNotification;
    }

    // Getter para o LiveData das mensagens
    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public void addUser(String username) {
        if (dbHelper != null) {
            dbHelper.addUser(username);
            //Log.d("Username", "Users updated: " + username);
            //loadContacts();
        }
    }

    public int getUserId(String username) {
        if (dbHelper != null) {
            return dbHelper.getUserId(username);
        }
        return -1;
    }

    public void createConversation(int username1, int username2) {
        if (dbHelper != null) {
            dbHelper.createConversation(username1, username2);
            loadConversations();
        }
    }

    public void loadConversations() {
        if (dbHelper != null && username.getValue() != null && !username.getValue().isEmpty()) {
            // Obter o ID do usuário com base no nome de usuário
            int userId = dbHelper.getUserId(username.getValue());
            List<Integer> userConversations = dbHelper.getConversationsByParticipantId(userId);

            // Lista de pares: Conversation ID e Most Recent Timestamp
            List<Pair<Integer, String>> conversationsWithTimestamps = new ArrayList<>();

            for (Integer conversationId : userConversations) {
                // Obter o timestamp mais recente para esta conversa
                int conversation = dbHelper.getConversationId(getUserId(getUsername()),conversationId);
                String mostRecentTimestamp = dbHelper.getMostRecentTimestamp(conversation);
                // Adicionar par (conversationId, mostRecentTimestamp) à lista
                conversationsWithTimestamps.add(new Pair<>(conversationId, mostRecentTimestamp));
            }

            // Ordenar a lista de conversas com base nos timestamps, em ordem decrescente
            conversationsWithTimestamps.sort((pair1, pair2) -> {
                if (pair1.second == null && pair2.second == null) return 0;
                if (pair1.second == null) return 1;
                if (pair2.second == null) return -1;
                return pair2.second.compareTo(pair1.second); // Decrescente
            });

            // Lista para armazenar os nomes de usuário dos participantes
            List<String> participantUsernames = new ArrayList<>();

            // Obter os nomes de usuário dos participantes com base na ordem ordenada
            for (Pair<Integer, String> conversationWithTimestamp : conversationsWithTimestamps) {
                Integer conversationId = conversationWithTimestamp.first;
                String participantUsername = dbHelper.getUserById(conversationId);
                if (participantUsername != null) {
                    participantUsernames.add(participantUsername);
                }
            }

            // Log para verificar a ordenação
            Log.d("LoadConversations", "User ID: " + userId + ", Ordered Usernames: " + participantUsernames);

            // Atualizar o LiveData com os nomes de usuário dos participantes
            conversations.setValue(participantUsernames);
        }
    }

    public void loadConversationsNotification() {
        if (dbHelper != null && username.getValue() != null && !username.getValue().isEmpty()) {
            // Obter o ID do usuário com base no nome de usuário
            int userId = dbHelper.getUserId(username.getValue());
            List<Integer> userConversations = dbHelper.getConversationsByParticipantId(userId);

            // Lista para armazenar os nomes de usuário dos participantes 2
            List<String> participantUsernames2 = new ArrayList<>();

            for (Integer conversationId : userConversations) {
                String participant2Username = dbHelper.getUserById(conversationId);
                int conversation = dbHelper.getConversationId(getUserId(getUsername()),conversationId);
                String notificacao = dbHelper.getNotificationByConversationId(conversation);
                Log.d("LoadConversations", notificacao);
                if (participant2Username != null) {
                    if (notificacao.equals("ligada")){
                        participantUsernames2.add(participant2Username + " (notificação ligada)");
                    }
                    if (notificacao.equals("desligada")){
                        participantUsernames2.add(participant2Username + " (notificação desligada)");
                    }

                }
            }

            // Adicionando um log para exibir os nomes de usuário dos participantes 2
            Log.d("LoadConversations", "User ID: " + userId + ", Participant Usernames: " + participantUsernames2);

            // Atualizar o LiveData com os nomes de usuário dos participantes 2
            conversationsNotification.setValue(participantUsernames2);
        }
    }

    public String getNotificationByConversationId(int conversation){
        return dbHelper.getNotificationByConversationId(conversation);
    }

    public void updateNotificationState(int conversationId, String newState) {
        // Atualiza o estado da notificação no banco de dados
        dbHelper.updateNotificationByConversationId(conversationId, newState); // Atualiza a coluna de notificação
    }

    public int getConversationId(int username1, int username2) {
        if (dbHelper != null) {
            return dbHelper.getConversationId(username1, username2);
        }
        return -1;
    }

    public void deleteConversation(int conversation, int user){
        if (dbHelper != null) {
            dbHelper.deleteConversation(conversation);
            dbHelper.deleteUser(user);
            loadConversations();
        }
    }

    public void addMessage(int conversation, int sender, String message, String timestamp) {
        if (dbHelper != null) {
            dbHelper.addMessage(conversation, sender, message, timestamp);
        }
    }

    public void loadMessagesForConversation(int conversationId) {
        if (dbHelper != null && username.getValue() != null && !username.getValue().isEmpty()) {
            // Logando o início da operação
            Log.d("ChatFragment", "Loading messages for conversation ID: " + conversationId);

            // Recupera mensagens da base de dados
            List<Message> conversationMessages = dbHelper.getMessagesForConversation(conversationId);

            // Loga a quantidade de mensagens recuperadas
            Log.d("ChatFragment", "Number of messages loaded: " + (conversationMessages != null ? conversationMessages.size() : 0));

            // Loga cada mensagem recuperada
            if (conversationMessages != null) {
                for (Message message : conversationMessages) {
                    Log.d("ChatFragment", "Message : " + message);
                }
            }

            // Atualiza o LiveData
            messages.setValue(conversationMessages);

            // Logando após a atualização do LiveData
            Log.d("ChatFragment", "Messages loaded and LiveData updated.");
        } else {
            // Log para identificar problemas na inicialização
            if (dbHelper == null) {
                Log.e("ChatFragment", "DBHelper is null. Cannot load messages.");
            }
            if (username.getValue() == null || username.getValue().isEmpty()) {
                Log.e("ChatFragment", "Username is null or empty. Cannot load messages.");
            }
        }
    }

    // Método para popular com mensagens simuladas
    public void loadSimulatedMessages(int conversation) {
        if (dbHelper == null) {
            Log.e("ChatFragment", "DBHelper is null. Cannot add messages to the database.");
            return;
        }

        // Mensagens enviadas
        addMessage(
                conversation,
                getUserId(getUsername()),
                "Mensagem enviada 5",
                "2024-12-16 10:00:00"
        );
        addMessage(
                conversation,
                getUserId(getUsername()),
                "Mensagem enviada 5",
                "2024-12-16 10:05:00"
        );

        // Mensagens recebidas
        addMessage(
                conversation,
                getUserId(getSelectedUsername()),
                "Mensagem recebida 5",
                "2024-12-16 10:10:00"
        );
        addMessage(
                conversation,
                getUserId(getSelectedUsername()),
                "Mensagem recebida 5",
                "2024-12-16 10:15:00"
        );

        // Log após inserir mensagens na base de dados
        Log.d("ChatFragment", "Simulated messages added to the database for conversation: " + conversation);

        // Atualiza as mensagens no LiveData a partir da base de dados
        loadMessagesForConversation(conversation);
    }


    public List<Message> getMessagesForConversation(int conversation) {
        if (dbHelper != null) {
            return dbHelper.getMessagesForConversation(conversation);
        }
        return null;
    }

    public String getUserById(int userID){
        return dbHelper.getUserById(userID);
    }

    public List<String> getAllUsers() {
        return dbHelper.getAllUsers(); // Chama o método direto do DatabaseHelper
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


    public String getSelectedUsername() {
        return selectedUsername.getValue();
    }

    public void setSelectedUsername(String index) {
        this.selectedUsername.setValue(index);
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
        loginAttempted = true;
        if (username.isEmpty()) {
            loginSuccess.setValue(false);
            return;
        }
        firestore.collection("Users").document(username).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        setUsername(username);
                        if (getUserId(username) == -1){
                            addUser(username);
                        }
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

    public boolean isLoginAttempted() {
        return loginAttempted;
    }

    public void createUser(String username) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        setUsername(username);
        firestore.collection("Users").document(username).set(userData)
                .addOnSuccessListener(aVoid -> {
                    loginSuccess.setValue(true);
                })
                .addOnFailureListener(e -> {
                    loginSuccess.setValue(false);
                });
    }
}
