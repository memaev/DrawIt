package com.llc.drawit.domain.util.drawing;

import android.graphics.Color;

import com.llc.drawit.R;
import com.llc.drawit.domain.util.Constants;

/**
 * Class for mapping color to string and back
 */
public class ColorMapper {
    public static int stringToColor(String color) {
        return switch (color) {
            case Constants.BLACK -> Color.BLACK;
            case Constants.BLUE -> Color.BLUE;
            case Constants.GREEN -> Color.GREEN;
            case Constants.WHITE -> Color.WHITE;
            default -> Color.RED;
        };
    }
    public static String colorToString(int color) {
        return switch (color) {
            case Color.BLACK -> Constants.BLACK;
            case Color.BLUE -> Constants.BLUE;
            case Color.GREEN -> Constants.GREEN;
            case Color.WHITE -> Constants.WHITE;
            default -> Constants.RED;
        };
    }

    public static int colorToStringResourceId(int color) {
        return switch (color) {
            case Color.BLACK -> R.string.black;
            case Color.BLUE -> R.string.blue;
            case Color.GREEN -> R.string.green;
            default -> R.string.red;
        };
    }
}
