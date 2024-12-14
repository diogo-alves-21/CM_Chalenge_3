package com.example.appweek8_2.Fragments;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;
import org.json.JSONObject;

public class ChatFragment extends Fragment {

    private FragmentsModel viewModel;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private ImageButton backBtn, sendBtn;
    private TextView contactUsername, messageInput;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);
        viewModel.setMqttHelper(requireContext());  // Configura o MQTT

        listView = view.findViewById(R.id.messagesListView);
        backBtn = view.findViewById(R.id.backButton);
        sendBtn = view.findViewById(R.id.sendButton);
        contactUsername = view.findViewById(R.id.textContact);
        messageInput = view.findViewById(R.id.messageInput);

        contactUsername.setText(viewModel.getSelectedUsername());

        // Configura a lista de mensagens
        messagesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
        listView.setAdapter(messagesAdapter);

        // Carregar mensagens anteriores
        viewModel.loadMessages();

        // Observar as mensagens e atualizar a lista
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            messagesAdapter.clear();
            if (messages != null) {
                messagesAdapter.addAll(messages);
            }
            messagesAdapter.notifyDataSetChanged();
        });

        // Botão para voltar
        backBtn.setOnClickListener(v -> goBack());

        // Botão para enviar mensagem
        sendBtn.setOnClickListener(v -> sendMessage(viewModel.getSelectedUsername()));

        return view;
    }

    // Método para voltar para a tela anterior
    public void goBack() {
        viewModel.setSelectedUsername(-1);  // Limpar o usuário selecionado
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ConversationsListFragment())
                .addToBackStack(null)
                .commit();
    }

    // Enviar a mensagem via MQTT
    public void sendMessage(String recipientUsername) {
        String message = messageInput.getText().toString();
        if (!message.isEmpty()) {
            try {
                // Recuperar o nome do usuário que está enviando a mensagem dinamicamente
                String senderUsername = viewModel.getUsername();  // Função que recupera o nome do usuário atual (pode ser um método da sua ViewModel ou outra classe)

                // Cria um objeto JSON com o formato necessário
                JSONObject messageJson = new JSONObject();
                messageJson.put("username", senderUsername);
                messageJson.put("mensagem", message);

                // Cria o tópico de envio com base no nome do destinatário
                String topic = recipientUsername + "/challenge3/ADN";

                // Envia a mensagem através do MQTT para o tópico adequado
                viewModel.sendMessage(topic, messageJson.toString());

                String topicArduino = "challenge3/arduino/ADN";

                viewModel.sendMessage(topicArduino, messageJson.toString());

                messageInput.setText("");  // Limpa o campo de entrada
            } catch (Exception e) {
                e.printStackTrace();  // Em caso de erro, imprime a stack trace
            }
        }
    }

}