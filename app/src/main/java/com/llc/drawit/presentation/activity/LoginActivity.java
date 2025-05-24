package com.llc.drawit.presentation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.llc.drawit.R;
import com.llc.drawit.databinding.ActivityLoginBinding;
import com.llc.drawit.domain.util.callbacks.LoginStateListener;
import com.llc.drawit.presentation.viewModel.LoginActivityViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private LoginActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(LoginActivityViewModel.class);

        listenLoadingDialogChanges();

        binding.btnLogin.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString();
            String password = binding.etPassword.getText().toString();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, getString(R.string.t_please_fill_out_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.login(LoginActivity.this, email, password, new LoginStateListener() {
                @Override
                public void onLoggedIn() {
                    Toast.makeText(LoginActivity.this, getString(R.string.t_successfully_logged_in), Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                }

                @Override
                public void onError(String msg) {
                    Toast.makeText(LoginActivity.this, msg, Toast.LENGTH_SHORT).show();
                }
            });
        });

        binding.tvNoAccount.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void listenLoadingDialogChanges() {
        viewModel.loading.observe(this, isLoading -> {
            binding.btnLogin.setLoading(isLoading);
            binding.etEmail.setEnabled(!isLoading);
            binding.etPassword.setEnabled(!isLoading);
        });
    }
}