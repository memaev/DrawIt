package com.llc.drawit.presentation.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Pair;
import android.view.View;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.llc.drawit.databinding.ActivityMainBinding;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.presentation.bottomSheet.BottomSheetNewWhiteboard;
import com.llc.drawit.presentation.recyclerView.WhiteboardsAdapter;
import com.llc.drawit.presentation.util.StringUtil;
import com.llc.drawit.presentation.util.color.DefaultProfileImageColorsUtil;
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

        binding.btnLogout.setOnClickListener(v -> {
            HFirebase.AUTH.signOut();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
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
            if (user.getProfileImageUrl().isEmpty()) {
                binding.imageProfileUser.setVisibility(View.INVISIBLE);

                // we want to display the initials of the user
                Pair<Integer, Integer> randomColorsPair = DefaultProfileImageColorsUtil.getRandomColorsPair();
                binding.defaultProfileImage.setBackgroundTintList(ColorStateList.valueOf(randomColorsPair.first));
                binding.defaultProfileImage.setTextColor(getColor(randomColorsPair.second));

                // display the initials
                binding.defaultProfileImage.setText(StringUtil.getInitials(user.getName()));
            }
            else {
                Glide.with(getBaseContext()).load(user.getProfileImageUrl()).into(binding.imageProfileUser);
                binding.defaultProfileImage.setVisibility(View.GONE);
            }

            binding.tvUsername.setText(user.getName());
            binding.tvUsertag.setText("@" + user.getTag());
            binding.tvEmail.setText(user.getEmail());
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