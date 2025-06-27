package com.llc.drawit.data.repositoryImpl;

import android.net.Uri;

import com.llc.drawit.domain.repository.StorageRepository;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;

public class StorageRepositoryImpl implements StorageRepository {
    @Override
    public void uploadImage(String imageId, Uri path, LoadManager<String> manager) {
        if (path == null) {
            manager.onResult(new LoadData<>(Result.FAILURE, "Failed to load image"));
            return;
        }

        // uploading the image to Firebase Storage, if the upload is successful, getting the link to download this image and calling the finish callback
        HFirebase.STORAGE.child(imageId).putFile(path).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HFirebase.STORAGE.child(imageId).getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        manager.onResult(new LoadData<>(Result.SUCCESS, task1.getResult().toString()));
                    } else {
                        manager.onResult(new LoadData<>(Result.FAILURE, "Failed to load image"));
                    }
                });
            } else {
                manager.onResult(new LoadData<>(Result.FAILURE, "Failed to load image, error: " + task.getException().getMessage()));
            }
        });
    }
}
