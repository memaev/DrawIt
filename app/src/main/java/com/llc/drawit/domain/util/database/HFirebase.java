package com.llc.drawit.domain.util.database;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class HFirebase {
    public static DatabaseReference DB = FirebaseDatabase.getInstance().getReference();
    public static FirebaseAuth AUTH = FirebaseAuth.getInstance();
    public static StorageReference STORAGE = FirebaseStorage.getInstance().getReference();
}
