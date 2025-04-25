package com.llc.drawit.presentation.bottomSheet;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.llc.drawit.databinding.NewWhiteboardBsBinding;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.database.Result;
import com.llc.drawit.presentation.activity.WhiteboardActivity;
import com.llc.drawit.presentation.viewModel.NewWhiteboardBsViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BottomSheetNewWhiteboard extends BottomSheetDialogFragment {

    private NewWhiteboardBsViewModel viewModel;
    private NewWhiteboardBsBinding binding;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(NewWhiteboardBsViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = NewWhiteboardBsBinding.inflate(inflater, container, false);

        binding.btnCreateWhiteboard.setOnClickListener(v -> {
            String whiteboardName = binding.etWhiteboardName.getText().toString();
            if (whiteboardName.isEmpty()) {
                Toast.makeText(requireContext(), "Enter the name of the new board", Toast.LENGTH_SHORT).show();
                return;
            }
            // start creating a new whiteboard
            viewModel.createWhiteboard(whiteboardName, data -> {
                if (data.getResultCode() == Result.FAILURE) {
                    Toast.makeText(requireContext(), data.getData(), Toast.LENGTH_SHORT).show();
                    dismiss();
                    return;
                }
                dismiss();

                Toast.makeText(requireContext(), "New board created successfully", Toast.LENGTH_SHORT).show();
                Intent toDrawActIntent = new Intent(requireContext(), WhiteboardActivity.class);
                toDrawActIntent.putExtra(Constants.WHITEBOARD_ID, data.getData());
                startActivity(toDrawActIntent);
            });
        });

        return binding.getRoot();
    }
}
