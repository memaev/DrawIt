package com.llc.drawit.presentation.viewModel;

import androidx.lifecycle.ViewModel;

import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.database.LoadManager;
import com.llc.drawit.domain.util.database.Result;

import java.util.HashMap;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class NewWhiteboardBsViewModel extends ViewModel {

    private UserRepository userRepository;

    @Inject
    public NewWhiteboardBsViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void createWhiteboard(String name, LoadManager<String> manager) {
        if (HFirebase.AUTH.getCurrentUser() == null)
            return;

        // сначала мы должны получить доски пользователя, чтобы проверить, что у него не больше 5 досок
        HFirebase.DB.child(Constants.USERS).child(HFirebase.AUTH.getCurrentUser().getUid()).child(Constants.WHITEBOARDS)
                .get().addOnCompleteListener(task1 -> {
                    if (!task1.isSuccessful()) {
                        manager.onResult(new LoadData<>(Result.FAILURE, "Ошибка загрузки данных"));
                        return;
                    }
                    if (task1.getResult().getValue() != null && task1.getResult().getValue().toString().split(",").length >= 5){
                        manager.onResult(new LoadData<>(Result.FAILURE, "Вы достигли лимита по количеству досок - 5"));
                        return;
                    }

                    //если все хорошо, то продолжаем создание доски
                    HashMap<String, String> whiteboardInfo = new HashMap<>();
                    whiteboardInfo.put(Constants.NAME, name);
                    whiteboardInfo.put(Constants.MEMBERS, HFirebase.AUTH.getCurrentUser().getUid());
                    String id = HFirebase.DB.child(Constants.WHITEBOARDS).push().getKey();
                    if (id==null) {
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                        return;
                    }

                    HFirebase.DB.child(Constants.WHITEBOARDS).child(id).setValue(whiteboardInfo)
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    userRepository.addWhiteboardId(HFirebase.AUTH.getCurrentUser().getUid(), id, result -> {
                                        if (result.getResultCode() == Result.FAILURE){
                                            manager.onResult(new LoadData<>(Result.FAILURE, null));
                                            return;
                                        }
                                        manager.onResult(new LoadData<>(Result.SUCCESS, id));
                                    });
                                }else{
                                    manager.onResult(new LoadData<>(Result.FAILURE, null));
                                }
                            });
                });

    }
}
