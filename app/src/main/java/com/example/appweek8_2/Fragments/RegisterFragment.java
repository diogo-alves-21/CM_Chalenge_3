package com.example.appweek8_2.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;

import javax.annotation.Nullable;


public class RegisterFragment extends Fragment {

    private FragmentsModel viewModel;
    private EditText username;
    private Button btnLogin;
    private TextView tvRegister;

    public RegisterFragment newInstance() {
       return new RegisterFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @androidx.annotation.Nullable ViewGroup container, @androidx.annotation.Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_register, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);

        username = view.findViewById(R.id.registerUsername);
        tvRegister = view.findViewById(R.id.tvRegisterTitle);
        btnLogin = view.findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> registerUser());



        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Registro realizado com sucesso!!", Toast.LENGTH_SHORT).show();
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ConversationsListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "Não foi possível realizar o registro!!", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }

    private void registerUser(){
        String usernameText =  username.getText().toString().trim();
        viewModel.createUser(usernameText);

    }
}