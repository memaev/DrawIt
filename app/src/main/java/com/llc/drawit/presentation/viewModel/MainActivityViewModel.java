package com.llc.drawit.presentation.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadManager;
import com.llc.drawit.domain.util.database.Result;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel // annotation for Dagger Hilt, which indicates that this is a ViewModel class
public class MainActivityViewModel extends ViewModel {
    private MutableLiveData<User> _currentUser = new MutableLiveData<>();
    public LiveData<User> currentUser = _currentUser;

    private MutableLiveData<List<Whiteboard>> _whiteboards = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<Whiteboard>> whiteboards = _whiteboards;

    private UserRepository userRepository;

    @Inject
    public MainActivityViewModel(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public void loadCurrentUser(){
        if (HFirebase.AUTH.getCurrentUser() == null)
            return;

        userRepository.loadUser(HFirebase.AUTH.getCurrentUser().getUid(), data -> {
            if (data.getResultCode() == Result.FAILURE)
                return;

            User currUser = data.getData();
            _currentUser.postValue(currUser);
        });
    }

    public void loadWhiteboards(){
        if (HFirebase.AUTH.getCurrentUser() == null) return;
        LoadManager<List<Whiteboard>> loadManager = data -> {
            if (data.getResultCode() == Result.FAILURE)
                return;


            _whiteboards.postValue(data.getData());
        };
        userRepository.getWhiteboards(HFirebase.AUTH.getCurrentUser().getUid(), loadManager);
    }

}
