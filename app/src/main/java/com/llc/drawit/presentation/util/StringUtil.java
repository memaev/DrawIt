package com.llc.drawit.presentation.util;

import lombok.experimental.UtilityClass;

@UtilityClass
public class StringUtil {
    public String getInitials(String name) {
        String[] initials = name.split(" ");
        return String.valueOf(initials[0].charAt(0)) + (initials.length > 1 ? initials[1].charAt(0) : ' ');
    }
}
