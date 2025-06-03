package com.llc.drawit.presentation.viewModel;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.repository.WhiteboardRepository;
import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.database.Result;
import com.llc.drawit.domain.util.drawing.Stroke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

/** ViewModel for the whiteboard, here we load members, whiteboard name, drawings on the board (and their real-time updates) **/

@HiltViewModel
public class WhiteboardActivityViewModel extends ViewModel {

    private final MutableLiveData<Whiteboard> _currWhiteboard = new MutableLiveData<>();
    public LiveData<Whiteboard> currWhiteboard = _currWhiteboard;

    private final MutableLiveData<List<User>> _members = new MutableLiveData<>(new ArrayList<>());
    public LiveData<List<User>> members = _members;

    private final MutableLiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> _drawings = new MutableLiveData<>(new LinkedHashMap<>());
    public LiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> drawings = _drawings;

    private final WhiteboardRepository whiteboardRepository;

    @Inject
    public WhiteboardActivityViewModel (WhiteboardRepository whiteboardRepository){
        this.whiteboardRepository = whiteboardRepository;
    }

    public void loadWhiteboard(String whiteboardId) {
        whiteboardRepository.loadWhiteboard(whiteboardId, data -> {
            if (data.getResultCode() == Result.FAILURE)
                return;

            _currWhiteboard.postValue(data.getData());
            whiteboardRepository.loadMembers(
                    data.getData().members(),
                    result -> {
                        if (result.getResultCode() == Result.FAILURE)
                            return;
                        this._members.postValue(result.getData());
                    }
            );
            whiteboardRepository.loadDrawings(
                    whiteboardId, _drawings,
                    result -> {
                        if (result.getResultCode() == Result.FAILURE) {
                            this._drawings.postValue(new LinkedHashMap<>());
                            return;
                        }

                        LinkedHashMap<Stroke, ArrayList<CPoint>> drawings = Optional.ofNullable(result.getData()).orElseGet(LinkedHashMap::new);
                        this._drawings.postValue(drawings);
                    }
            );
        });
    }


    public void addDrawing(Pair<Stroke, ArrayList<CPoint>> drawing) {
        Optional.ofNullable(_currWhiteboard.getValue()).ifPresent(
                whiteboard -> whiteboardRepository.addUpdDrawing(whiteboard.id(), drawing)
        );
    }

    public void stopListening(){
        Optional.ofNullable(_currWhiteboard.getValue()).ifPresent(
                whiteboard -> whiteboardRepository.stopDrawingsListener(whiteboard.id())
        );
    }
}
