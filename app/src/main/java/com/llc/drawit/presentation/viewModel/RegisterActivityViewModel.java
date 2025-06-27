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
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;

import java.util.Objects;
import java.util.UUID;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterActivityViewModel extends ViewModel {

    private MutableLiveData<User> _user = new MutableLiveData<>(User.builder().build());
    public LiveData<User> user = _user;

    private MutableLiveData<Boolean> _loading = new MutableLiveData<>();
    public LiveData<Boolean> loading = _loading;

    private MutableLiveData<Uri> _pickedUserProfileImage = new MutableLiveData<>(null);
    public LiveData<Uri> pickedUserProfileImage = _pickedUserProfileImage;

    private StorageRepository storageRepository;
    private UserRepository userRepository;

    @Inject
    public RegisterActivityViewModel(StorageRepository storageRepository, UserRepository userRepository){
        this.storageRepository = storageRepository;
        this.userRepository = userRepository;
    }

    public void savePickedProfileImageUri(Uri uri) {
        _pickedUserProfileImage.postValue(uri);
    }

    public void setProfileImage(String profileImageUrl){
        Objects.requireNonNull(this._user.getValue()).setProfileImageUrl(profileImageUrl);
    }

    public void setUserNameTag(String name, String tag){
        Objects.requireNonNull(this._user.getValue()).setName(name);
        Objects.requireNonNull(this._user.getValue()).setTag(tag);
    }

    public void uploadImage(Uri imagePath, LoadManager<String> manager){
        storageRepository.uploadImage(UUID.randomUUID().toString(), imagePath, data -> {
            setProfileImage(data.getData());
            manager.onResult(data);
        });
    }

    public void register(User user, LoadManager<String> manager){
        _loading.postValue(true);
        if (pickedUserProfileImage.getValue() != null) {
            uploadImage(pickedUserProfileImage.getValue(), result -> {
                if (result.getData() != null) {
                    userRepository.register(user, manager);
                    _loading.postValue(false);
                }
            });
        } else {
            userRepository.register(user, manager);
            _loading.postValue(false);
        }
    }
}
