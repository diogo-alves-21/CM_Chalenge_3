package com.example.appweek8_2.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;


public class ChatFragment extends Fragment {

    private FragmentsModel viewModel;
    private ArrayAdapter<String> messagesAdapter;
    private ListView listView;
    private ImageButton backBtn, sendBtn;
    private TextView contactUsername;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);
        viewModel.setDatabaseHelper(requireContext());

        listView = view.findViewById(R.id.messagesListView);
        backBtn = view.findViewById(R.id.backButton);
        sendBtn = view.findViewById(R.id.sendButton);
        contactUsername= view.findViewById(R.id.textContact);

        contactUsername.setText(viewModel.getSelectedUsername());

        backBtn.setOnClickListener(v -> goBack());

        sendBtn.setOnClickListener(v -> sendMessage());

        return view;
    }

    public void goBack(){
        viewModel.setSelectedUsername(-1);
        FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, new ConversationsListFragment());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void sendMessage(){}
}