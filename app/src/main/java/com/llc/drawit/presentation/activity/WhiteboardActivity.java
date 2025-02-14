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
import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.drawing.DrawingInstrument;
import com.llc.drawit.domain.util.callbacks.MembersListener;
import com.llc.drawit.domain.util.callbacks.OnAddText;
import com.llc.drawit.presentation.bottomSheet.AddMemberBottomSheet;
import com.llc.drawit.presentation.util.color.PickColorDialog;
import com.llc.drawit.presentation.util.text.AddTextDialog;
import com.llc.drawit.presentation.recyclerView.MembersAdapter;
import com.llc.drawit.presentation.viewModel.WhiteboardActivityViewModel;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WhiteboardActivity extends AppCompatActivity implements MembersListener, OnAddText {

    private ActivityWhiteboardBinding binding;
    private WhiteboardActivityViewModel viewModel;
    private MembersAdapter membersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWhiteboardBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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

        String whiteboardId = getIntent().getStringExtra(Constants.WHITEBOARD_ID);

        viewModel = new ViewModelProvider(this).get(WhiteboardActivityViewModel.class);
        viewModel.loadWhiteboard(whiteboardId);

        binding.btnAddMember.setOnClickListener(v -> {
            AddMemberBottomSheet addMemberBottomSheet = new AddMemberBottomSheet(this, whiteboardId);
            addMemberBottomSheet.show(getSupportFragmentManager(), "add-member-bs");
        });

        viewModel.currWhiteboard.observe(this, whiteboard -> {
            if (whiteboard==null) return;
            binding.tvWhiteboardName.setText(whiteboard.getName());

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

        checkInstrument();
        //переключение инструментов
        binding.btnPen.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.PEN);
            checkInstrument();

            PickColorDialog pickColorDialog = new PickColorDialog(
                    color -> {
                        binding.drawView.setCurrentColor(color);
                    });
            pickColorDialog.show(getSupportFragmentManager(), "pick-color-dialog");
        });
        binding.btnText.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.TEXT);
            checkInstrument();
        });
        binding.btnEraser.setOnClickListener(v -> {
            binding.drawView.setCurrentInstrument(DrawingInstrument.ERASE);
            checkInstrument();
        });

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        binding.drawView.init(this, metrics, viewModel.drawings, WhiteboardActivity.this);
    }

    //проверяет какой инструмент выбран и отображает их соответствующе
    private void checkInstrument() {
        switch (binding.drawView.getCurrentInstrument()) {
            case PEN: {
                binding.btnPen.setBackgroundResource(R.drawable.white_bg);
                binding.btnText.setBackgroundResource(R.drawable.transpanent_bg);
                binding.btnEraser.setBackgroundResource(R.drawable.transpanent_bg);
                break;
            }

            case TEXT: {
                binding.btnText.setBackgroundResource(R.drawable.white_bg);
                binding.btnPen.setBackgroundResource(R.drawable.transpanent_bg);
                binding.btnEraser.setBackgroundResource(R.drawable.transpanent_bg);
                break;
            }

            case ERASE: {
                binding.btnText.setBackgroundResource(R.drawable.transpanent_bg);
                binding.btnPen.setBackgroundResource(R.drawable.transpanent_bg);
                binding.btnEraser.setBackgroundResource(R.drawable.white_bg);
                break;
            }
        }
    }

    @Override
    public void onMemberAdded(User user) {
        membersAdapter.addMember(user);
    }

    //при остановке активности прекращает прослушивание изменений на доске в БД
    @Override
    protected void onStop() {
        viewModel.stopListening();

        super.onStop();
    }

    //добавление текста на доску
    @Override
    public void invoke(CPoint pos) {
        AddTextDialog addTextDialog = new AddTextDialog(text -> {
            binding.drawView.addText(pos, text);
        });
        addTextDialog.show(getSupportFragmentManager(), "add-text-dialog");
    }
}