package com.llc.drawit.domain.repository;

import android.util.Pair;

import androidx.lifecycle.MutableLiveData;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.database.LoadManager;
import com.llc.drawit.domain.util.drawing.Stroke;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public interface WhiteboardRepository {
    void loadWhiteboard(String whiteboardId, LoadManager<Whiteboard> manager);
    void loadMembers(String members, LoadManager<List<User>> manager);
    void loadDrawings(String whiteboardId, MutableLiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> drawings, LoadManager<LinkedHashMap<Stroke, ArrayList<CPoint>>> manager);
    void stopDrawingsListener(String whiteboardId);
    void addUpdDrawing(String whiteboardId, Pair<Stroke, ArrayList<CPoint>> drawing);
    void loadDrawing(String drawingPath, String text, int color, long timestamp, LoadManager<Pair<Stroke, ArrayList<CPoint>>> manager);
    void addMember(String whiteboardId, String userId, LoadManager<Object> manager);
}
