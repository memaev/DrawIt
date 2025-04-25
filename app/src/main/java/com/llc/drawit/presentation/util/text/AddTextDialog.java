package com.llc.drawit.presentation.util.text;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.llc.drawit.databinding.AddTextDialogBinding;

public class AddTextDialog extends DialogFragment {

    private AddTextDialogBinding binding;
    private @NonNull final OnPositiveButtonClickListener listener;

    public AddTextDialog(@NonNull final OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddTextDialogBinding.inflate(inflater, container, false);

        binding.btnApplyText.setOnClickListener(v -> {
            String text = binding.etText.getText().toString();
            if (text.isEmpty()) {
                Toast.makeText(getContext(), "Enter text", Toast.LENGTH_SHORT).show();
                return;
            }
            listener.onPositiveButtonClick(text);
            dismiss();
        });

        return binding.getRoot();
    }

    public interface OnPositiveButtonClickListener {
        void onPositiveButtonClick(String userInput);
    }
}
