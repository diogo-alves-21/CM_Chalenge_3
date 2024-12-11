package com.example.appweek8_2.Fragments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.example.appweek8_2.MainActivity;
import com.example.appweek8_2.R;
import com.example.appweek8_2.ViewModals.FragmentsModel;
import com.google.firebase.auth.FirebaseAuth;

public class LoginFragment extends Fragment {

    private FragmentsModel viewModel;
    private EditText username;
    private Button btnLogin;
    private TextView tvRegister;

    public static LoginFragment newInstance() {
        return new LoginFragment();
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        viewModel = new ViewModelProvider(requireActivity()).get(FragmentsModel.class);

        username = view.findViewById(R.id.username);
        tvRegister = view.findViewById(R.id.tvRegister);
        btnLogin = view.findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> navigateToRegister());

        viewModel.getLoginSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Login realizado com sucesso!!", Toast.LENGTH_SHORT).show();
                Log.d("UserModel", "Login successful, username: " + viewModel.getUsername().getValue());
                FragmentTransaction transaction = requireActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, new ConversationsListFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            } else {
                Toast.makeText(getContext(), "Este username é inválido!!", Toast.LENGTH_SHORT).show();
            }
        });

        return view;
    }

    private void loginUser() {
        String usernameText = username.getText().toString().trim();
        viewModel.verifyUsername(usernameText);
    }

    private void navigateToRegister() {
        ((MainActivity) getActivity()).replaceFragment(new RegisterFragment());
    }
}