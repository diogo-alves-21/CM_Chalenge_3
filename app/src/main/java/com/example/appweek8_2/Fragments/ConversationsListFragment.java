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
import android.widget.ListView;
import android.widget.Toast;

import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;

import java.util.List;

public class ConversationsListFragment extends Fragment {

    private FragmentsModel viewModel;
    private ArrayAdapter<String> usersAdapter;
    private ListView listView;
    private Button addconversation;
    private Button buttonArduino;

    public static ConversationsListFragment newInstance() {
        return new ConversationsListFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversations_list, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);
        viewModel.setDatabaseHelper(requireContext());

        listView = view.findViewById(R.id.listView);
        addconversation = view.findViewById(R.id.buttonAdd);
        buttonArduino = view.findViewById(R.id.buttonArduino);

        addconversation.setOnClickListener(v -> createConversation());

        buttonArduino.setOnClickListener(v -> openArduinoFragment());

        viewModel.loadConversations();

        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            deleteConversation(position);
            return true;
        });

        listView.setOnItemClickListener((parent, view1, position, id) -> {
            openChat(position);
        });

        viewModel.getConversations().observe(getViewLifecycleOwner(), conversationsList -> {
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

        return view;
    }

    public void createConversation() {
        EditText user_2 = new EditText(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Conversa")
                .setView(user_2)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String username = user_2.getText().toString().trim();

                    // Verificar se o nome do usuário não está vazio
                    if (!username.isEmpty()) {
                        // Adicionar o usuário apenas se o nome não for vazio

                        if (viewModel.getUserId(viewModel.getUsername())==-1){
                            viewModel.addUser(viewModel.getUsername());
                        }

                        if (viewModel.getUserId(username)==-1){
                            viewModel.addUser(username);

                            int newUser = viewModel.getUserId(username);
                            int currentUser = viewModel.getUserId(viewModel.getUsername());
                            viewModel.createConversation(currentUser, newUser);
                            
                            Toast.makeText(getContext(), "Conversa criada", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getContext(), "Conversa já existe", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "O nome do usuário não pode ser vazio", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void deleteConversation(int position) {
        viewModel.setSelectedUsername(usersAdapter.getItem(position));

        int conversationToDelete = viewModel.getConversationId(viewModel.getUserId(viewModel.getUsername()), viewModel.getUserId(usersAdapter.getItem(position)));

        if (conversationToDelete != -1) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Apagar Conversa")
                    .setMessage("Têm a certeza que deseja apagar as mensagens da conversa " + viewModel.getSelectedUsername() + "?")
                    .setPositiveButton("Apagar", (dialog, which) -> {
                        viewModel.deleteConversation(conversationToDelete, viewModel.getUserId(viewModel.getSelectedUsername()));
                        Toast.makeText(getContext(), "Conversation deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

    public void openChat(int position){
        viewModel.setSelectedUsername(usersAdapter.getItem(position));
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        Log.d("SelectUser","aaaa: "+ viewModel.getSelectedUsername());
        transaction.replace(R.id.fragment_container, new ChatFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Método para abrir o ArduinoFragment
    private void openArduinoFragment() {
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ArduinoFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

}