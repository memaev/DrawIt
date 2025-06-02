package com.llc.drawit.presentation.util.color;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;

import com.llc.drawit.R;
import com.llc.drawit.databinding.ChooseColorDialogBinding;
import com.llc.drawit.domain.util.drawing.ColorMapper;

import java.util.Optional;

public class PickColorDialog extends DialogFragment {

    private ChooseColorDialogBinding binding;
    private final MutableLiveData<Integer> currentColor = new MutableLiveData<>(Color.BLACK);
    private @NonNull final PickColorDialog.OnPositiveButtonClickListener listener;

    public PickColorDialog(@NonNull final PickColorDialog.OnPositiveButtonClickListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = ChooseColorDialogBinding.inflate(inflater, container, false);

        binding.colorBlack.setOnClickListener(v -> {
            currentColor.postValue(Color.BLACK);
        });
        binding.colorRed.setOnClickListener(v -> {
            currentColor.postValue(Color.RED);
        });
        binding.colorGreen.setOnClickListener(v -> {
            currentColor.postValue(Color.GREEN);
        });
        binding.colorBlue.setOnClickListener(v -> {
            currentColor.postValue(Color.BLUE);
        });

        currentColor.observe(requireActivity(), color -> {
            String colorName = Optional.ofNullable(getContext()).map(context -> context.getString(ColorMapper.colorToStringResourceId(color))).orElse("Unknown");
            String displayedColorString = Optional.ofNullable(getContext()).map(context -> context.getString(R.string.color_picked, colorName)).orElse("Unknown");
            binding.tvColorPicked.setText(displayedColorString);
        });

        binding.btnSelectColor.setOnClickListener(v -> {
            listener.onPositiveButtonClick(currentColor.getValue());
            dismiss();
        });

        return binding.getRoot();
    }

    public interface OnPositiveButtonClickListener {
        void onPositiveButtonClick(int color);
    }
}
