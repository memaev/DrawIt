package com.llc.drawit.presentation.viewModel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.repository.StorageRepository;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.database.LoadManager;
import com.llc.drawit.domain.util.database.Result;

import java.util.Objects;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterActivityViewModel extends ViewModel {

    private MutableLiveData<User> _user = new MutableLiveData<>(new User());
    public LiveData<User> user = _user;

    private MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> loading = _loading;

    private StorageRepository storageRepository;
    private UserRepository userRepository;

    @Inject
    public RegisterActivityViewModel(StorageRepository storageRepository, UserRepository userRepository){
        this.storageRepository = storageRepository;
        this.userRepository = userRepository;
    }

    public void setProfileImage(String profileImageUrl){
        Objects.requireNonNull(this._user.getValue()).setProfileImageUrl(profileImageUrl);
    }

    public void setUserNameTag(String name, String tag){
        Objects.requireNonNull(this._user.getValue()).setName(name);
        Objects.requireNonNull(this._user.getValue()).setTag(tag);
    }

    public void uploadImage(Uri imagePath, LoadManager<String> manager){
        if (HFirebase.AUTH.getCurrentUser()==null){
            manager.onResult(new LoadData<>(Result.FAILURE, null));
            return;
        }
        storageRepository.uploadImage(HFirebase.AUTH.getCurrentUser().getUid(), imagePath, data -> {
            setProfileImage(data.getData());
            manager.onResult(data);
        });
    }

    public void register(User user, LoadManager<String> manager){
        userRepository.register(user, manager);
    }
}
