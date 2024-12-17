package com.example.appweek8_2.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.appweek8_2.Helpers.Message;
import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    private FragmentsModel viewModel;
    private ArrayAdapter<String> receivedMessagesAdapter;
    private ArrayAdapter<String> sentMessagesAdapter;
    private ListView receivedMessagesListView, sentMessagesListView;
    private ImageButton backBtn, sendBtn;
    private TextView contactUsername, messageInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);
        viewModel.setMqttHelper();  // Configura o MQTT



        // Configurações de layout
        receivedMessagesListView = view.findViewById(R.id.receivedMessagesListView);
        sentMessagesListView = view.findViewById(R.id.sentMessagesListView);
        backBtn = view.findViewById(R.id.backButton);
        sendBtn = view.findViewById(R.id.sendButton);
        contactUsername = view.findViewById(R.id.textContact);
        messageInput = view.findViewById(R.id.messageInput);

        contactUsername.setText(viewModel.getSelectedUsername());

        // Configura os adaptadores para mensagens recebidas e enviadas
        receivedMessagesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        sentMessagesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);

        receivedMessagesListView.setAdapter(receivedMessagesAdapter);
        sentMessagesListView.setAdapter(sentMessagesAdapter);

        // Carregar mensagens anteriores
        int conversationID= viewModel.getConversationId( viewModel.getUserId(viewModel.getUsername()),viewModel.getUserId(viewModel.getSelectedUsername()));
        //viewModel.loadSimulatedMessages(conversationID);
        viewModel.loadMessagesForConversation(conversationID);

        // Observa as mensagens carregadas e faz a separação
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            List<String> sentMessages = new ArrayList<>();
            List<String> receivedMessages = new ArrayList<>();

            // Separar mensagens enviadas e recebidas com base no sender_id
            if (messages != null) {
                for (Message message : messages) {
                    Log.d("ChatFragment", "Mensagem: " + message.getMessageText() + ", SenderId: " + message.getSenderId());

                    // Comparar o senderId com o ID do usuário atual
                    if (message.getSenderId() == viewModel.getUserId(viewModel.getUsername())) {
                        sentMessages.add(message.getMessageText() + " | " + message.getTimestamp());
                    } else {
                        receivedMessages.add(message.getMessageText() + " | " + message.getTimestamp());
                    }
                }
            }

            // Adicionando logs para verificar as listas de mensagens separadas
            Log.d("ChatFragment", "Mensagens enviadas: " + sentMessages.size());
            Log.d("ChatFragment", "Mensagens recebidas: " + receivedMessages.size());

            // Atualizar os adaptadores com as mensagens separadas
            receivedMessagesAdapter.clear();
            sentMessagesAdapter.clear();

            receivedMessagesAdapter.addAll(receivedMessages);
            sentMessagesAdapter.addAll(sentMessages);

            // Notificar os adaptadores para atualizar a UI
            receivedMessagesAdapter.notifyDataSetChanged();
            sentMessagesAdapter.notifyDataSetChanged();
        });

        // Botão para voltar
        backBtn.setOnClickListener(v -> goBack());

        // Botão para enviar mensagem
        sendBtn.setOnClickListener(v -> sendMessage(viewModel.getSelectedUsername(), conversationID));

        return view;
    }

    // Método para voltar para a tela anterior
    public void goBack() {
        viewModel.setSelectedUsername(null);  // Limpar o usuário selecionado
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ConversationsListFragment())
                .addToBackStack(null)
                .commit();
    }

    // Enviar a mensagem via MQTT
    public void sendMessage(String recipientUsername, int conversationID) {
        String message = messageInput.getText().toString();
        if (!message.isEmpty()) {
            try {
                // Recuperar o nome do usuário que está enviando a mensagem dinamicamente
                String senderUsername = viewModel.getUsername();  // Função que recupera o nome do usuário atual (pode ser um método da sua ViewModel ou outra classe)

                // Obter o timestamp atual no formato desejado
                String timestamp = java.time.LocalDateTime.now()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

                // Cria um objeto JSON com o formato necessário
                JSONObject messageJson = new JSONObject();
                messageJson.put("username", senderUsername);
                messageJson.put("mensagem", message);

                // Cria o tópico de envio com base no nome do destinatário
                String topic = recipientUsername + "/challenge3/ADN";

                // Envia a mensagem através do MQTT para o tópico adequado
                viewModel.sendMessage(topic, messageJson.toString());

                String notification = viewModel.getNotificationByConversationId(conversationID);

                if (notification.equals("ligada")){

                    String topicArduino = "challenge3/arduino/ADN";

                    viewModel.sendMessage(topicArduino, messageJson.toString());
                }

                messageInput.setText("");  // Limpa o campo de entrada

                viewModel.addMessage(conversationID,viewModel.getUserId(viewModel.getUsername()),message,timestamp);

                viewModel.loadMessagesForConversation(conversationID);
            } catch (Exception e) {
                e.printStackTrace();  // Em caso de erro, imprime a stack trace
            }
        }
    }

}