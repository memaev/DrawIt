package com.llc.drawit.presentation.bottomSheet;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.llc.drawit.databinding.AddMembersBsLayoutBinding;
import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.util.callbacks.MembersListener;
import com.llc.drawit.domain.util.database.Result;
import com.llc.drawit.presentation.recyclerView.FullMembersListAdapter;
import com.llc.drawit.presentation.viewModel.AddMemberViewModel;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AddMemberBottomSheet extends BottomSheetDialogFragment implements MembersListener {

    private AddMembersBsLayoutBinding binding;
    private AddMemberViewModel viewModel;
    private FullMembersListAdapter adapter;

    // listener for managing members that was provided from activity
    private final MembersListener onMemberSelectedFromActivity;

    private final String whiteboardId;

    public AddMemberBottomSheet(MembersListener onMemberSelectedFromActivity, String whiteboardId) {
        this.onMemberSelectedFromActivity = onMemberSelectedFromActivity;
        this.whiteboardId = whiteboardId;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(AddMemberViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = AddMembersBsLayoutBinding.inflate(inflater, container, false);

        viewModel.loadAllUsers(res -> {
            if (res.getResultCode() == Result.FAILURE)
                return;

            //set adapter for the list of all users
            binding.rvMembersFull.setLayoutManager(new LinearLayoutManager(requireContext()));
            adapter = new FullMembersListAdapter(res.getData(), this);
            binding.rvMembersFull.setAdapter(adapter);
        });

        //triggers when the text in SearchView changes
        binding.membersSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (adapter == null) return false;

                adapter.filter(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (adapter == null) return false;

                adapter.filter(newText);
                return true;
            }
        });

        return binding.getRoot();
    }

    // will be triggered when a user is selected from the list of all users
    @Override
    public void onMemberSelected(User user) {
        //add user to the board and add the board to the user
        viewModel.addMember(whiteboardId, user, res -> {
            if (res.getResultCode() != Result.FAILURE)
                onMemberSelectedFromActivity.onMemberAdded(user); //perform action in activity and close bottom sheet
            dismiss();
        });
    }
}
