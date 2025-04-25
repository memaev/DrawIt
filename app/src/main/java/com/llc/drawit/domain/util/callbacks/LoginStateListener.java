package com.llc.drawit.domain.util.callbacks;


/**
 * Login listener
 */
public interface LoginStateListener {
    void onLoggedIn();
    void onError(String msg);
}
