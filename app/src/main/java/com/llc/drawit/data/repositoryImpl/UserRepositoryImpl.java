package com.llc.drawit.data.repositoryImpl;

import androidx.annotation.NonNull;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.repository.WhiteboardRepository;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.llc.drawit.presentation.util.NNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import dagger.Lazy;

public class UserRepositoryImpl implements UserRepository{

    // in this database repository we also need access to data loading for whiteboard
    private Lazy<WhiteboardRepository> whiteboardRepository;

    @Inject
    public UserRepositoryImpl(Lazy<WhiteboardRepository> whiteboardRepository) {
        this.whiteboardRepository = whiteboardRepository;
    }

    @Override
    public void checkRegistration(String uid, LoadManager<Boolean> manager) {
        HFirebase.DB.child(Constants.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()){
                    manager.onResult(new LoadData<>(Result.SUCCESS, false));
                }else{
                    manager.onResult(new LoadData<>(Result.SUCCESS, true));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                manager.onResult(new LoadData<>(Result.FAILURE, false));
            }
        });
    }

    @Override
    public void register(User user, LoadManager<String> manager) {
        HFirebase.AUTH.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        HFirebase.DB.child(Constants.USERS).child(NNull.str(task.getResult().getUser().getUid())).setValue(user.getAsHashMap())
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()){
                                        manager.onResult(new LoadData<>(Result.SUCCESS, "User successfully registered"));
                                    }else{
                                        manager.onResult(new LoadData<>(Result.FAILURE, "Registration failed"));
                                    }
                                });
                    }
                    else
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                });
    }

    @Override
    public void getWhiteboards(String uid, LoadManager<List<Whiteboard>> manager) {
        if (uid==null){
            manager.onResult(new LoadData<>(Result.FAILURE, null));
            return;
        }
        HFirebase.DB.child(Constants.USERS).child(uid).child(Constants.WHITEBOARDS).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        // ids of all user's whiteboards are saved in the string and divided by ','
                        String whiteboardsStr = Optional.ofNullable(task.getResult().getValue()).map(String::valueOf).orElse("");
                        if (whiteboardsStr.isEmpty()){
                            // if user doesn't have any whiteboards
                            manager.onResult(new LoadData<>(Result.SUCCESS, new ArrayList<>()));
                            return;
                        }
                        List<String> whiteboardsIds = Arrays.asList(whiteboardsStr.split(","));
                        whiteboardRepository.get().loadWhiteboards(whiteboardsIds, data -> {
                            if (data.getResultCode() == Result.SUCCESS)
                                manager.onResult(new LoadData<>(Result.SUCCESS, data.getData()));
                        });
                    }else{
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                    }
                });
    }

    @Override
    public void loadUser(String userId, LoadManager<User> manager) {
        if (userId == null){
            manager.onResult(new LoadData<>(Result.FAILURE, null));
            return;
        }

        HFirebase.DB.child(Constants.USERS).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = Optional.ofNullable(snapshot.child(Constants.NAME).getValue()).map(String::valueOf).orElse("");
                String tag = Optional.ofNullable(snapshot.child(Constants.TAG).getValue()).map(String::valueOf).orElse("");
                String email = Optional.ofNullable(snapshot.child(Constants.EMAIL).getValue()).map(String::valueOf).orElse("");
                String password = Optional.ofNullable(snapshot.child(Constants.PASSWORD).getValue()).map(String::valueOf).orElse("");
                String profileImageUrl = Optional.ofNullable(snapshot.child(Constants.PROFILE_IMAGE_URL).getValue()).map(String::valueOf).orElse("");

                User user = User.builder()
                        .name(name)
                        .tag(tag)
                        .email(email)
                        .password(password)
                        .profileImageUrl(profileImageUrl)
                        .build();
                manager.onResult(new LoadData<>(Result.SUCCESS, user));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                manager.onResult(new LoadData<>(Result.FAILURE, null));
            }
        });
    }

    @Override
    public void loadAllUsers(LoadManager<List<User>> manager) {
        HFirebase.DB.child(Constants.USERS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot parentSnapshot) {
                List<User> users = new ArrayList<>();

                for (DataSnapshot snapshot : parentSnapshot.getChildren()) {
                    //skipping the current user
                    if (Objects.equals(snapshot.getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        continue;

                    String name = Optional.ofNullable(snapshot.child(Constants.NAME).getValue()).map(String::valueOf).orElse("");
                    String tag = Optional.ofNullable(snapshot.child(Constants.TAG).getValue()).map(String::valueOf).orElse("");
                    String email = Optional.ofNullable(snapshot.child(Constants.EMAIL).getValue()).map(String::valueOf).orElse("");
                    String profileImageUrl = Optional.ofNullable(snapshot.child(Constants.PROFILE_IMAGE_URL).getValue()).map(String::valueOf).orElse("");

                    User user = User.builder()
                            .uid(snapshot.getKey())
                            .name(name)
                            .tag(tag)
                            .email(email)
                            .profileImageUrl(profileImageUrl)
                            .build();
                    users.add(user);
                }

                manager.onResult(new LoadData<>(Result.SUCCESS, users));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                manager.onResult(new LoadData<>(Result.FAILURE, new ArrayList<>()));
            }
        });
    }

    @Override
    public void addWhiteboardId(String userId, String whiteboardId, LoadManager<String> manager) {
        HFirebase.DB.child(Constants.USERS).child(userId).child(Constants.WHITEBOARDS)
                .get().addOnCompleteListener(task -> {
                    // getting ids of user's whiteboards
                    if (!task.isSuccessful() || task.getResult().getValue() == null) {
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                        return;
                    }
                    String whiteboardsIds = Optional.ofNullable(task.getResult().getValue()).map(String::valueOf).orElse("");
                    //adding whiteboard id to user's whiteboards
                    if (whiteboardsIds.isEmpty()) whiteboardsIds = whiteboardId;
                    else whiteboardsIds += "," + whiteboardId;

                    HFirebase.DB.child(Constants.USERS).child(userId).child(Constants.WHITEBOARDS).setValue(whiteboardsIds)
                            .addOnCompleteListener(pushTask -> {
                                if (pushTask.isSuccessful()) {
                                    manager.onResult(new LoadData<>(Result.SUCCESS, null));
                                    return;
                                }
                                manager.onResult(new LoadData<>(Result.FAILURE, null));
                            });
                });
    }
}
