package com.llc.drawit.data.repositoryImpl;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.repository.WhiteboardRepository;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.database.LoadManager;
import com.llc.drawit.domain.util.database.Result;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.llc.drawit.presentation.util.NNull;
import com.llc.drawit.presentation.util.loading.LoadingAlertDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import dagger.Lazy;

public class UserRepositoryImpl implements UserRepository{

    //в этом database репозитории нам также понадобится доступ к загрузке даннных для доски (функции для этого находятся в whiteboardRepository)
    private Lazy<WhiteboardRepository> whiteboardRepository;

    @Inject //аннотация для автоматического импорта аргументов
    public UserRepositoryImpl(Lazy<WhiteboardRepository> whiteboardRepository) {
        this.whiteboardRepository = whiteboardRepository;
    }

    @Override
    public void checkRegistration(String uid, LoadManager<Boolean> manager) {
        HFirebase.DB.child(Constants.USERS).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                //проверяем есть ли пользователь в базе данных
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
                        //id-шники досок пользователя хранятся в виде строки через запятую
                        String whiteboardsStr = (task.getResult().getValue()!=null ? task.getResult().getValue() : "").toString();
                        if (whiteboardsStr.isEmpty()){
                            //если у пользователя нет досок
                            manager.onResult(new LoadData<>(Result.SUCCESS, new ArrayList<>()));
                            return;
                        }
                        String[] whiteboards = whiteboardsStr.split(",");
                        List<Whiteboard> whiteboardList = new ArrayList<>();

                        for (int i=0; i<whiteboards.length; ++i){
                            int finalI = i;
                            //загружаем каждую доску
                            whiteboardRepository.get().loadWhiteboard(whiteboards[i], data -> {
                                if (data.getResultCode() == Result.SUCCESS)
                                    whiteboardList.add(data.getData());

                                if (finalI == (whiteboards.length-1))
                                    manager.onResult(new LoadData<>(Result.SUCCESS, whiteboardList));
                            });
                        }

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
                //получаем данные пользователя
                String name = NNull.str(snapshot.child(Constants.NAME).getValue());
                String tag = NNull.str(snapshot.child(Constants.TAG).getValue());
                String email = NNull.str(snapshot.child(Constants.EMAIL).getValue());
                String password = NNull.str(snapshot.child(Constants.PASSWORD).getValue());
                String profileImageUrl = NNull.str(snapshot.child(Constants.PROFILE_IMAGE_URL).getValue());

                User user = new User(name, tag, email, password, profileImageUrl);
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
                    //пропускаем текущего пользователя
                    if (Objects.equals(snapshot.getKey(), FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        continue;
                    String name = NNull.str(snapshot.child(Constants.NAME).getValue());
                    String tag = NNull.str(snapshot.child(Constants.TAG).getValue());
                    String email = NNull.str(snapshot.child(Constants.EMAIL).getValue());
                    String password = NNull.str(snapshot.child(Constants.PROFILE_IMAGE_URL).getValue());

                    User user = new User(snapshot.getKey(), name, tag, email, password);
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
                    // получаем id досок пользователя
                    if (!task.isSuccessful() || task.getResult().getValue() == null) { //если не удалось получить id досок
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                        return;
                    }
                    String whiteboardsIds = ((task.getResult().getValue()==null)?"":(task.getResult().getValue())).toString();
                    //добавляем id доски к пользователю
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
