package com.llc.drawit.presentation.viewModel;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.llc.drawit.R;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.state.LoginBtnState;
import com.llc.drawit.domain.util.callbacks.LoginStateListener;
import com.google.firebase.auth.PhoneAuthCredential;
import com.llc.drawit.presentation.util.NNull;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

public class LoginActivityViewModel extends ViewModel {

    private MutableLiveData<Boolean> _loading = new MutableLiveData<>(false);
    public LiveData<Boolean> loading = _loading;

    public void login(Context context, String email, String password, LoginStateListener listener) {
        _loading.postValue(true);
        HFirebase.AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    _loading.postValue(false);
                    if (task.isSuccessful())
                        listener.onLoggedIn();
                    else
                        listener.onError(context.getString(R.string.t_error_occurred)
                                + ((task.getException()!=null)? ". " + NNull.str(task.getException().getMessage()) : ""));
                });
    }
}
