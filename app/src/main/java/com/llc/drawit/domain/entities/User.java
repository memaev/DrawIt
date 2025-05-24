package com.llc.drawit.domain.entities;

import com.llc.drawit.domain.util.Constants;
import com.llc.drawit.presentation.util.NNull;

import java.util.HashMap;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class User {
    private String uid;
    private String name;
    private String tag;
    private String email;
    private String password;
    private String profileImageUrl;
    private String whiteboards;

    public HashMap<String, String> getAsHashMap(){
        HashMap<String, String> user = new HashMap<>();
        user.put(Constants.NAME, NNull.str(name));
        user.put(Constants.TAG, NNull.str(tag));
        user.put(Constants.EMAIL, NNull.str(email));
        user.put(Constants.PROFILE_IMAGE_URL, (profileImageUrl==null?"":profileImageUrl));
        user.put(Constants.WHITEBOARDS, (whiteboards==null?"":whiteboards));
        return user;
    }
}
