package com.llc.drawit.presentation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.llc.drawit.databinding.ActivityMainBinding;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.presentation.bottomSheet.BottomSheetNewWhiteboard;
import com.llc.drawit.presentation.recyclerView.WhiteboardsAdapter;
import com.llc.drawit.presentation.viewModel.MainActivityViewModel;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private MainActivityViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (HFirebase.AUTH.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(MainActivityViewModel.class);
        loadUser();
        loadWhiteboards();

        binding.refreshLayoutMain.setOnRefreshListener(() -> { //onRefresh
            loadUser();
            loadWhiteboards();
        });

        binding.btnAddWhiteboard.setOnClickListener(v -> {
            BottomSheetNewWhiteboard bottomSheetNewWhiteboard = new BottomSheetNewWhiteboard();
            bottomSheetNewWhiteboard.show(getSupportFragmentManager(), "create-whiteboard-bottom-sheet");
        });
    }

    private void loadUser(){
        // load and display info about the current user
        viewModel.loadCurrentUser();
        viewModel.currentUser.observe(this, user -> {
            if (!user.getProfileImageUrl().isEmpty())
                Glide.with(getBaseContext()).load(user.getProfileImageUrl()).into(binding.imageProfileUser);

            binding.textviewUserName.setText(user.getName());
            binding.textviewTag.setText("@" + user.getTag());
            binding.tvPhoneNumber.setText(user.getEmail());
        });
    }

    private void loadWhiteboards(){
        // load and display whiteboards list
        viewModel.loadWhiteboards();
        viewModel.whiteboards.observe(this, whiteboards -> {
            binding.rvWhiteboards.setLayoutManager(new LinearLayoutManager(this));
            binding.rvWhiteboards.setAdapter(new WhiteboardsAdapter(whiteboards));

            runOnUiThread(() -> {
                binding.refreshLayoutMain.setRefreshing(false);
            });
        });
    }
}