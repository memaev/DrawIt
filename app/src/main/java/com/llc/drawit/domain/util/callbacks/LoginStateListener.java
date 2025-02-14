package com.llc.drawit.domain.util.callbacks;

// Слушатель состояния входа в аккаунт
public interface LoginStateListener {
    void onLoggedIn();
    void onError(String msg);
}
