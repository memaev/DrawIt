package com.llc.drawit.presentation.activity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.llc.drawit.R;
import com.llc.drawit.databinding.ActivityRegisterBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.database.Result;
import com.llc.drawit.presentation.util.loading.LoadingAlertDialog;
import com.llc.drawit.presentation.viewModel.RegisterActivityViewModel;

import java.io.IOException;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private RegisterActivityViewModel viewModel;

    // ActivityResultLauncher for picking an image and uploading to the server
    ActivityResultLauncher<Intent> pickImageActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData()!=null && result.getData().getData()!=null){
                    Uri filePath = result.getData().getData();
                    viewModel.savePickedProfileImageUri(filePath);
                }
            }
    );

    //to obtain permission to access the gallery
    ActivityResultLauncher<String> requestPermissionLauncher= registerForActivityResult(
            new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted){
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    pickImageActivityResultLauncher.launch(intent);
                }else{
                    Toast.makeText(getBaseContext(), getString(R.string.t_no_gallery_permission), Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(RegisterActivityViewModel.class);

        setStateObservers();

        binding.imagePerson.setOnClickListener(v -> launchImagePicker());
        binding.llAddProfilePhoto.setOnClickListener(v -> launchImagePicker());

        binding.buttonRegister.setOnClickListener(v -> {
            String email = binding.editTextEmail.getText().toString().trim();
            String password = binding.editTextPassword.getText().toString().trim();
            String tag = binding.editTextTag.getText().toString().trim();
            String name = binding.editTextName.getText().toString().trim();
            User user = new User(name, tag, email, password, "");

            if (email.isEmpty() || password.isEmpty() || tag.isEmpty() || name.isEmpty()){
                Toast.makeText(this, getString(R.string.t_please_fill_out_fields), Toast.LENGTH_SHORT).show();
                return;
            }
            viewModel.setUserNameTag(name, tag);
            viewModel.register(user, data -> {
                if (data.getResultCode() == Result.SUCCESS) {
                    startActivity(new Intent(RegisterActivity.this, MainActivity.class));
                    finish();
                } else {
                    Toast.makeText(this, getString(R.string.t_registration_error), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }

    private void launchImagePicker() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            permission = Manifest.permission.READ_MEDIA_IMAGES;
        else
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(getBaseContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            pickImageActivityResultLauncher.launch(intent);
        }
    }

    private void setStateObservers() {
        viewModel.pickedUserProfileImage.observe(this, uri -> {
            if (uri == null) {
                binding.llAddProfilePhoto.setVisibility(View.VISIBLE);
                binding.cardViewImagePerson.setVisibility(View.GONE);
            } else {
                binding.llAddProfilePhoto.setVisibility(View.GONE);
                binding.cardViewImagePerson.setVisibility(View.VISIBLE);
                binding.imagePerson.setImageURI(uri);
            }
        });

        viewModel.loading.observe(this, isLoading -> {
            binding.buttonRegister.setLoading(isLoading);
            binding.editTextEmail.setEnabled(!isLoading);
            binding.editTextName.setEnabled(!isLoading);
            binding.editTextTag.setEnabled(!isLoading);
            binding.editTextPassword.setEnabled(!isLoading);
            binding.llAddProfilePhoto.setEnabled(!isLoading);
            binding.cardViewImagePerson.setEnabled(!isLoading);
            binding.imagePerson.setEnabled(!isLoading);
        });
    }

}
