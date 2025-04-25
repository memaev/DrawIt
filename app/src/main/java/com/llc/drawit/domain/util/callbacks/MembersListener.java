package com.llc.drawit.domain.util.callbacks;

import com.llc.drawit.domain.entities.User;

/**
 * Callback for members management in BottomSheet
 */
public interface MembersListener {
    default void onMemberAdded(User user) {}
    default void onMemberSelected(User user) {}
}
