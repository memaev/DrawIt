package com.llc.drawit.data.repositoryImpl;

import android.net.Uri;

import com.llc.drawit.domain.repository.StorageRepository;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;

public class StorageRepositoryImpl implements StorageRepository {
    @Override
    public void uploadImage(String uid, Uri path, LoadManager<String> manager) {
        if (uid == null || path == null) {
            manager.onResult(new LoadData<>(Result.FAILURE, "Не удалось загрузить картинку"));
            return;
        }

        // uploading the image to Firebase Storage, if the upload is successful, getting the link to download this image and calling the finish callback
        HFirebase.STORAGE.child(uid).putFile(path).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                HFirebase.STORAGE.child(uid).getDownloadUrl().addOnCompleteListener(task1 -> {
                    if (task1.isSuccessful()) {
                        manager.onResult(new LoadData<>(Result.SUCCESS, task1.getResult().toString()));
                    } else {
                        manager.onResult(new LoadData<>(Result.FAILURE, "Не удалось загрузить картинку"));
                    }
                });
            } else {
                manager.onResult(new LoadData<>(Result.FAILURE, "Не удалось загрузить картинку"));
            }
        });
    }
}
