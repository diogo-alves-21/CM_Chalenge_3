package com.example.appweek8_2.Fragments;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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

public class ConversationsListFragment extends Fragment {

    private FragmentsModel viewModel;
    private ArrayAdapter<String> usersAdapter;
    private ListView listView;
    private Button adduser;

    public static ConversationsListFragment newInstance() {
        return new ConversationsListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_conversations_list, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);
        viewModel.setDatabaseHelper(requireContext());

        listView = view.findViewById(R.id.listView);
        adduser = view.findViewById(R.id.buttonAdd);

        adduser.setOnClickListener(v -> createConversation());

        viewModel.loadContacts();

        listView.setOnItemLongClickListener((parent, view1, position, id) -> {
            deleteConversation(position);
            return true;
        });

        viewModel.getUsername().observe(getViewLifecycleOwner(), username -> {
            Log.d("ConversationsListFragment", "Username updated: " + username);
            viewModel.loadContacts(); // Load contacts when username is updated
        });

        viewModel.getContacts().observe(getViewLifecycleOwner(), users -> {
            if (users != null) {
                Log.d("ConversationsListFragment", "Users updated: " + users);
                if (usersAdapter == null) {
                    usersAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, users);
                    listView.setAdapter(usersAdapter);
                } else {
                    usersAdapter.clear();
                    usersAdapter.addAll(users);
                    usersAdapter.notifyDataSetChanged();
                }
            }
        });

        return view;
    }

    public void createConversation() {
        EditText user = new EditText(getContext());
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Adicionar Conversa")
                .setView(user)
                .setPositiveButton("Adicionar", (dialog, which) -> {
                    String newUser = user.getText().toString().trim();
                    if (!newUser.isEmpty()) {
                        viewModel.addUser(newUser);
                        Toast.makeText(getContext(), "Título da nota atualizado", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Título da nota não pode ser vazio", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss())
                .show();
    }

    public void deleteConversation(int position) {
        String userToDelete = usersAdapter.getItem(position);
        if (userToDelete != null) {
            new AlertDialog.Builder(getContext())
                    .setTitle("Apagar Conversa")
                    .setMessage("Têm a certeza que deseja apagar as conversas com " + userToDelete + "?")
                    .setPositiveButton("Apagar", (dialog, which) -> {
                        viewModel.deleteUser(userToDelete);
                        Toast.makeText(getContext(), "Conversation deleted", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                    .show();
        }
    }

}