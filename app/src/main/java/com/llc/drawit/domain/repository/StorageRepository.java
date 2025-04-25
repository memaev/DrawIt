package com.llc.drawit.domain.repository;

import android.net.Uri;

import com.llc.drawit.domain.util.database.LoadManager;

public interface StorageRepository {
    void uploadImage(String uid, Uri path, LoadManager<String> manager);
}
