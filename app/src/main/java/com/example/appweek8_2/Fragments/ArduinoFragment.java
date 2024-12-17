package com.example.appweek8_2.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;

import java.util.List;

public class ArduinoFragment extends Fragment{
    private FragmentsModel viewModel;
    private ArrayAdapter<String> usersAdapter;
    private ListView listView;
    private ImageButton backBtn;

    // Método novo para ArduinoFragment
    public static ArduinoFragment newInstance() {
        return new ArduinoFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.arduino_fragment, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);

        listView = view.findViewById(R.id.listView);

        backBtn = view.findViewById(R.id.backButton);

        viewModel.loadConversationsNotification();

        viewModel.getConversationsNotification().observe(getViewLifecycleOwner(), conversationsList -> {
            if (conversationsList != null) {
                Log.d("ConversationsListFragment", "Users updated: " + conversationsList);
                if (usersAdapter == null) {
                    usersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, conversationsList);
                    listView.setAdapter(usersAdapter);
                } else {
                    usersAdapter.clear();
                    usersAdapter.addAll(conversationsList);
                    usersAdapter.notifyDataSetChanged();
                }
            } else {
                Log.d("ConversationsListFragment", "Conversations list is null");
            }
        });

        // Adiciona o listener de clique para modificar a notificação
        listView.setOnItemClickListener((parent, view1, position, id) -> {
            String item = usersAdapter.getItem(position);
            String conversationName = item.split(" ")[0]; // Extrair o nome da conversa (exemplo: "item 1")

            int userId1 = viewModel.getUserId(viewModel.getUsername());
            int userId2 = viewModel.getUserId(conversationName); // O nome do item pode ser usado para encontrar o outro usuário

            // Obtenha o conversationId
            int conversationId = viewModel.getConversationId(userId1, userId2);

            // Obtenha o estado atual da notificação
            String currentNotificationState = viewModel.getNotificationByConversationId(conversationId);

            // Modifica o texto do item clicado com base no estado atual da notificação
            if (item != null) {
                if (currentNotificationState.equals("ligada")) {
                    // Desliga a notificação
                    usersAdapter.remove(item);
                    usersAdapter.insert(item.replace("(notificação ligada)", "(notificação desligada)"), position);
                    // Atualize o estado no banco de dados
                    viewModel.updateNotificationState(conversationId, "desligada");
                } else if (currentNotificationState.equals("desligada")) {
                    // Liga a notificação
                    usersAdapter.remove(item);
                    usersAdapter.insert(item.replace("(notificação desligada)", "(notificação ligada)"), position);
                    // Atualize o estado no banco de dados
                    viewModel.updateNotificationState(conversationId, "ligada");
                }
                usersAdapter.notifyDataSetChanged();
            }
        });

        backBtn.setOnClickListener(v -> goBack());

        return view;
    }

    // Método para voltar para a tela anterior
    public void goBack() {
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new ConversationsListFragment())
                .addToBackStack(null)
                .commit();
    }


}
