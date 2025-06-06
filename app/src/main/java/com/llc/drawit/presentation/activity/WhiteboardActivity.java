package com.llc.drawit.presentation.activity;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;

import com.llc.drawit.R;
import com.llc.drawit.databinding.ActivityWhiteboardBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.drawing.DrawingInstrument;
import com.llc.drawit.domain.util.callbacks.MembersListener;
import com.llc.drawit.presentation.bottomSheet.AddMemberBottomSheet;
import com.llc.drawit.presentation.util.color.PickColorDialog;
import com.llc.drawit.presentation.util.text.AddTextDialog;
import com.llc.drawit.presentation.recyclerView.MembersAdapter;
import com.llc.drawit.presentation.viewModel.WhiteboardActivityViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WhiteboardActivity extends AppCompatActivity {

    private ActivityWhiteboardBinding binding;
    private WhiteboardActivityViewModel viewModel;
    private MembersAdapter membersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWhiteboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpBackNavigation();

        viewModel = new ViewModelProvider(this).get(WhiteboardActivityViewModel.class);

        String whiteboardId = getIntent().getStringExtra(Constants.WHITEBOARD_ID);
        viewModel.loadWhiteboard(whiteboardId);

        binding.btnAddMember.setOnClickListener(v -> {
            AddMemberBottomSheet addMemberBottomSheet = new AddMemberBottomSheet(new MembersListener() {
                @Override
                public void onMemberAdded(User user) {
                    membersAdapter.addMember(user);
                }

                @Override
                public void onMemberSelected(User user) {
                    MembersListener.super.onMemberSelected(user);
                }
            }, whiteboardId);
            addMemberBottomSheet.show(getSupportFragmentManager(), "add-member-bs");
        });

        startStateObservers();
        setUpInstrumentsClickListeners();
        initDrawView();
    }

    private void setUpBackNavigation() {
        getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                viewModel.stopListening();
                startActivity(new Intent(WhiteboardActivity.this, MainActivity.class));
                finish();
            }
        });

        binding.btnBack.setOnClickListener(v -> {
            viewModel.stopListening();
            startActivity(new Intent(WhiteboardActivity.this, MainActivity.class));
            finish();
        });
    }

    private void setUpInstrumentsClickListeners() {
        // instruments switching
        binding.btnPen.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.PEN);
            indicateSelectedInstrument();

            PickColorDialog pickColorDialog = new PickColorDialog(color -> binding.drawView.setCurrentColor(color));
            pickColorDialog.show(getSupportFragmentManager(), "pick-color-dialog");
        });
        binding.btnText.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.TEXT);
            indicateSelectedInstrument();
        });
        binding.btnEraser.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.ERASE);
            indicateSelectedInstrument();
        });
    }

    private void startStateObservers() {
        viewModel.currWhiteboard.observe(this, whiteboard -> {
            if (whiteboard==null) return;
            binding.tvWhiteboardName.setText(whiteboard.name());

            binding.drawView.addListener(drawing -> viewModel.addDrawing(drawing));
        });

        viewModel.members.observe(this, members -> {
            if (members == null) members = new ArrayList<>();
            binding.rvMembers.setLayoutManager(new LinearLayoutManager(
                    WhiteboardActivity.this,
                    LinearLayoutManager.HORIZONTAL,
                    true
            ));
            membersAdapter = new MembersAdapter(members);
            binding.rvMembers.setAdapter(membersAdapter);
        });
    }

    private void initDrawView() {
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        binding.drawView.init(pos -> {
            AddTextDialog addTextDialog = new AddTextDialog(text -> binding.drawView.addText(pos, text));
            addTextDialog.show(getSupportFragmentManager(), "add-text-dialog");
        }, metrics, viewModel.drawings, WhiteboardActivity.this);
    }

    @Override
    protected void onResume() {
        indicateSelectedInstrument();

        super.onResume();
    }

    // displaying what instrument is selected
    private void indicateSelectedInstrument() {
        DrawingInstrument picked = binding.drawView.getCurrentInstrument();
        binding.btnPen.setBackgroundResource((picked == DrawingInstrument.PEN) ? R.drawable.white_bg : R.drawable.transpanent_bg);
        binding.btnText.setBackgroundResource((picked == DrawingInstrument.TEXT) ? R.drawable.white_bg : R.drawable.transpanent_bg);
        binding.btnEraser.setBackgroundResource((picked == DrawingInstrument.ERASE) ? R.drawable.white_bg : R.drawable.transpanent_bg);
    }

    // when activity is stopped need to stop listener on RealtimeDatabase changes
    @Override
    protected void onStop() {
        viewModel.stopListening();

        super.onStop();
    }
}