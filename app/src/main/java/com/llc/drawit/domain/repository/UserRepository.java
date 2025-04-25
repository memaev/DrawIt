package com.llc.drawit.domain.repository;

import com.llc.drawit.domain.entities.User;
import com.llc.drawit.domain.entities.Whiteboard;
import com.llc.drawit.domain.util.database.LoadManager;

import java.util.List;

public interface UserRepository {
    void checkRegistration(String uid, LoadManager<Boolean> manager);
    void register(User user, LoadManager<String> manager);
    void getWhiteboards(String uid, LoadManager<List<Whiteboard>> manager);
    void loadUser(String userId, LoadManager<User> manager);
    void loadAllUsers(LoadManager<List<User>> manager);
    void addWhiteboardId(String userId, String whiteboardId, LoadManager<String> manager);
}
