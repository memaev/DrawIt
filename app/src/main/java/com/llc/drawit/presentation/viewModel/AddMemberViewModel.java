package com.llc.drawit.presentation.viewModel;

import androidx.lifecycle.ViewModel;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.repository.WhiteboardRepository;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddMemberViewModel extends ViewModel {

    private UserRepository userRepository;
    private WhiteboardRepository whiteboardRepository;

    @Inject
    public AddMemberViewModel(UserRepository userRepository, WhiteboardRepository whiteboardRepository) {
        this.userRepository = userRepository;
        this.whiteboardRepository = whiteboardRepository;
    }

    public void loadAllUsers(LoadManager<List<User>> manager) {
        userRepository.loadAllUsers(manager);
    }

    public void addMember(String whiteboardId, User user, LoadManager<String> manager) {
        whiteboardRepository.addMember(whiteboardId, user.getUid(), res -> {
            if (res.getResultCode() == Result.FAILURE) {
                manager.onResult(new LoadData<>(Result.SUCCESS, null));
                return;
            }
            userRepository.addWhiteboardId(user.getUid(), whiteboardId, manager);
        });
    }
}
