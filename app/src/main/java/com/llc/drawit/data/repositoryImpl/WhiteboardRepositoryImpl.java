package com.llc.drawit.data.repositoryImpl;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.repository.UserRepository;
import com.llc.drawit.domain.repository.WhiteboardRepository;
import com.llc.drawit.domain.util.drawing.CPoint;
import com.llc.drawit.domain.util.drawing.ColorMapper;
import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.domain.util.database.HFirebase;
import com.llc.drawit.domain.util.database.LoadData;
import com.llc.drawit.domain.util.callbacks.LoadManager;
import com.llc.drawit.domain.util.database.Result;
import com.llc.drawit.domain.util.drawing.Stroke;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.inject.Inject;

import dagger.Lazy;

public class WhiteboardRepositoryImpl implements WhiteboardRepository {

    private ValueEventListener valueEventListener; // listener on changes in realtime database
    private final Lazy<UserRepository> userRepository; // repository for user management in the database
    private final HashMap<String, String> loadedPaths = new HashMap<>(); //save ids of already loaded drawings so we won't need to load them again

    @Inject // dependency injection of UserRepository
    public WhiteboardRepositoryImpl(Lazy<UserRepository> userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void loadWhiteboard(String whiteboardId, LoadManager<Whiteboard> manager) {
        if (whiteboardId == null) {
            manager.onResult(new LoadData<>(Result.FAILURE, null));
            return;
        }

        // getting whiteboard data by id
        HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = Objects.requireNonNull(snapshot.child(Constants.NAME).getValue()).toString();
                        String id = snapshot.getKey();
                        String members = Objects.requireNonNull(snapshot.child(Constants.MEMBERS).getValue()).toString();

                        Whiteboard whiteboard = Whiteboard.builder()
                                .id(id)
                                .name(name)
                                .members(members)
                                .build();
                        manager.onResult(new LoadData<>(Result.SUCCESS, whiteboard));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        manager.onResult(new LoadData<>(Result.FAILURE, null));
                    }
                });
    }

    @Override
    public void loadWhiteboards(List<String> whiteboardsIds, LoadManager<List<Whiteboard>> manager) {
        List<Whiteboard> whiteboards = new ArrayList<>();
        HFirebase.DB.child(Constants.WHITEBOARDS).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                whiteboardsIds.forEach(whiteboardId -> {
                    DataSnapshot whiteboardSnapshot = snapshot.child(whiteboardId);
                    String id = whiteboardSnapshot.getKey();
                    String name = Objects.requireNonNull(whiteboardSnapshot.child(Constants.NAME).getValue()).toString();
                    String members = Objects.requireNonNull(whiteboardSnapshot.child(Constants.MEMBERS).getValue()).toString();

                    Whiteboard whiteboard = new Whiteboard(id, name, members);
                    whiteboards.add(whiteboard);
                });
                manager.onResult(new LoadData<>(Result.SUCCESS, whiteboards));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                manager.onResult(new LoadData<>(Result.FAILURE, null));
            }
        });
    }

    @Override
    public void loadMembers(String members, LoadManager<List<User>> manager) {
        if (members==null){
            manager.onResult(new LoadData<>(Result.FAILURE, null));
            return;
        }

        // if there is only 1 user then he is a whiteboard creator a we don't need to load his avatar
        if (members.equals(Objects.requireNonNull(HFirebase.AUTH.getCurrentUser()).getUid()) || members.isEmpty()) {
            manager.onResult(new LoadData<>(Result.SUCCESS, new ArrayList<>()));
            return;
        }

        String[] membersIds = members.split(",");
        List<User> users = new ArrayList<>();
        for (int i=0; i< membersIds.length; ++i){
            int finalI = i;
            if (membersIds[i].equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) continue;
            userRepository.get().loadUser(membersIds[i], data -> {
                if (data.getResultCode() == Result.SUCCESS){
                    users.add(data.getData());

                    if (finalI == membersIds.length-1)
                        manager.onResult(new LoadData<>(Result.SUCCESS, users));
                }
            });
        }
    }

    @Override
    public void loadDrawing(String drawingPath, String text /* If we need to display text */, int color, long timestamp, LoadManager<Pair<Stroke, ArrayList<CPoint>>> manager) {
        //get all the points of this drawing in a string to speed up loading and updating
        String[] pointsStrArr = drawingPath.split("\\|"); //split points by the '|' character

        Pair<Stroke, ArrayList<CPoint>> drawing;

        // if the text is not empty, we need to load and draw the text, otherwise it's a drawing
        if (text.isEmpty())
            drawing = new Pair<>(new Stroke(color, 5, timestamp), new ArrayList<>());
        else
            drawing = new Pair<>(new Stroke(text, timestamp), new ArrayList<>());

        for (String pointStr : pointsStrArr) {
            if (pointStr.isEmpty()) continue;
            //coordinates of the point are separated by a space
            float x = Float.parseFloat(pointStr.split(" ")[0]);
            float y = Float.parseFloat(pointStr.split(" ")[1]);

            drawing.second.add(new CPoint(x, y));
        }
        manager.onResult(new LoadData<>(Result.SUCCESS, drawing));
    }

    @Override
    public void addMember(String whiteboardId, String userId, LoadManager<Object> manager) {
        if (whiteboardId == null || whiteboardId.isEmpty() || userId == null || userId.isEmpty()){
            return;
        }

        //check that user is not a member in this whiteboard
        loadWhiteboard(whiteboardId, res -> {
            if (res.getResultCode() == Result.FAILURE) {
                manager.onResult(new LoadData<>(Result.FAILURE, null));
                return;
            }
            String members = res.getData().members();
            if (Arrays.asList(members.split(",")).contains(userId)) {
                manager.onResult(new LoadData<>(Result.FAILURE, null));
                return;
            }
            if (members.isEmpty()) members = userId;
            else members += ","+userId;

            //add user to members list
            HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId).child(Constants.MEMBERS)
                    .setValue(members).addOnCompleteListener(task -> {
                        if (task.isSuccessful())
                            manager.onResult(new LoadData<>(Result.SUCCESS, null));
                        else
                            manager.onResult(new LoadData<>(Result.FAILURE, null));
                    });
        });
    }

    @Override
    public void loadDrawings(String whiteboardId, MutableLiveData<LinkedHashMap<Stroke, ArrayList<CPoint>>> drawingsLd, LoadManager<LinkedHashMap<Stroke, ArrayList<CPoint>>> manager) {
        if (whiteboardId==null || whiteboardId.isEmpty()){
            return;
        }

        //listener for changes in the database
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (!snapshot.exists()){
                    manager.onResult(new LoadData<>(Result.FAILURE, null));
                    return;
                }

                LinkedHashMap<Stroke, ArrayList<CPoint>> drawings = new LinkedHashMap<>();
                for (DataSnapshot drawingSnapshot : snapshot.getChildren()) {
                    if (loadedPaths.containsKey(drawingSnapshot.getKey()))
                        continue;

                    //get all the points of this drawing in a string to speed up loading and updating
                    String colorStr = Optional.ofNullable(drawingSnapshot.child(Constants.COLOR).getValue()).map(String::valueOf).orElse("red");
                    int color = ColorMapper.stringToColor(colorStr);

                    String text = Optional.ofNullable(drawingSnapshot.child(Constants.TEXT).getValue()).map(String::valueOf).orElse("");
                    long timestamp = Long.parseLong(Optional.ofNullable(drawingSnapshot.child(Constants.TIMESTAMP).getValue()).map(String::valueOf).orElse("0"));



                    //get all the points of this drawing in a string to speed up loading and updating
                    String drawingPath = Optional.ofNullable(drawingSnapshot.child(Constants.POINTS).getValue()).map(String::valueOf).orElse("");
                    loadDrawing(drawingPath, text, color, timestamp, drawing -> {
                        if (drawing == null) return;
                        drawings.put(drawing.getData().first, drawing.getData().second);
                        loadedPaths.put(drawingSnapshot.getKey(), "");
                    });
                }
                LinkedHashMap<Stroke, ArrayList<CPoint>> currentDrawings = drawingsLd.getValue();
                if (currentDrawings != null) {
                    drawings.putAll(currentDrawings);
                }
                currentDrawings = sortHashmap(drawings);
                drawingsLd.postValue(currentDrawings);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){}
        };

        HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId).child(Constants.POINTS)
                .addValueEventListener(valueEventListener);
    }

    //sort drawings by time
    private LinkedHashMap<Stroke, ArrayList<CPoint>> sortHashmap(LinkedHashMap<Stroke, ArrayList<CPoint>> map) {
        List<Map.Entry<Stroke, ArrayList<CPoint>>> list = new ArrayList<>(map.entrySet());

        // Sort the list based on the timestamp of each Stroke
        list.sort(Comparator.comparingLong(o -> o.getKey().timestamp));

        // Convert the sorted list back to a map
        LinkedHashMap<Stroke, ArrayList<CPoint>> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<Stroke, ArrayList<CPoint>> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    @Override
    public void stopDrawingsListener(String whiteboardId) {
        loadedPaths.clear(); // clearing the list of already loaded drawings

        // removing onValueChangedListener
        HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId).child(Constants.POINTS)
                .removeEventListener(valueEventListener);
    }

    @Override
    public void addUpdDrawing(String whiteboardId, Pair<Stroke, ArrayList<CPoint>> drawing) {
        if (whiteboardId==null || whiteboardId.isEmpty() || drawing.first == null || drawing.second == null)
            return;

        // if no id of particular line on the screen then this line is new
        String key = HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId).child(Constants.POINTS).push().getKey();
        if (key == null) return;

        HashMap<String, String> drawingInfo = getStringStringHashMap(drawing);
        HFirebase.DB.child(Constants.WHITEBOARDS).child(whiteboardId).child(Constants.POINTS).child(key).setValue(drawingInfo);
    }

    /**
     * Function to convert drawing into map struct to be save in the database
     * @param drawing the full drawing
     * @return HashMap of string-string
     */
    private HashMap<String, String> getStringStringHashMap(Pair<Stroke, ArrayList<CPoint>> drawing) {
        StringBuilder path = new StringBuilder();
        for (int i = 0; i< drawing.second.size(); ++i) {
            CPoint point = drawing.second.get(i);
            if (path.length() == 0)
                path = new StringBuilder(point.toString());
            else
                path.append("|").append(point.toString());
        }
        HashMap<String, String> drawingInfo = new HashMap<>();
        drawingInfo.put(Constants.POINTS, path.toString());
        drawingInfo.put(Constants.COLOR, ColorMapper.colorToString(drawing.first.color));
        drawingInfo.put(Constants.TEXT, ((drawing.first.text==null) ? "" : drawing.first.text));
        drawingInfo.put(Constants.TIMESTAMP, String.valueOf(drawing.first.timestamp));
        return drawingInfo;
    }
}
