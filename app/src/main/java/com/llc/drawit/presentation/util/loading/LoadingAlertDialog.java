package com.llc.drawit.presentation.util.loading;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.llc.drawit.databinding.LoadingAlertDialogBinding;

public class LoadingAlertDialog extends DialogFragment {

    private LoadingAlertDialogBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = LoadingAlertDialogBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }
}
