package com.llc.drawit.domain.entities;

import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.presentation.util.NNull;

import java.util.HashMap;

public class User {
    private String uid;
    private String name;
    private String tag;
    private String email;
    private String password;
    private String profileImageUrl;
    private String whiteboards;

    public User(String uid, String name, String tag, String email, String password, String profileImageUrl, String whiteboards) {
        this.uid = uid;
        this.name = name;
        this.tag = tag;
        this.email = email;
        this.password = password;
        this.profileImageUrl = profileImageUrl;
        this.whiteboards = whiteboards;
    }

    public User(String uid, String name, String tag, String email, String profileImageUrl) {
        this.uid = uid;
        this.name = name;
        this.tag = tag;
        this.email = email;
        this.profileImageUrl = profileImageUrl;
        this.whiteboards = "";
    }

    public User(String name, String tag, String email, String password){
        this.name = name;
        this.tag = tag;
        this.email = email;
        this.password = password;
        this.profileImageUrl = "";
        this.whiteboards = "";
    }

    public HashMap<String, String> getAsHashMap(){
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.NAME, NNull.str(name));
        user.put(Constants.TAG, NNull.str(tag));
        user.put(Constants.EMAIL, NNull.str(email));
        user.put(Constants.PROFILE_IMAGE_URL, (profileImageUrl==null?"":profileImageUrl));
        user.put(Constants.WHITEBOARDS, (whiteboards==null?"":whiteboards));
        return user;
    }

    public User(){}

    public String getUid() {
        return uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() { return email; }

    public String getPassword() { return password; }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
