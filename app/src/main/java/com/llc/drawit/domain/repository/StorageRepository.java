package com.llc.drawit.domain.repository;

import android.net.Uri;

import com.llc.drawit.domain.util.callbacks.LoadManager;

public interface StorageRepository {
    void uploadImage(String imageId, Uri path, LoadManager<String> manager);
}
