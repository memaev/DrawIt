package com.llc.drawit.domain.util.drawing;

import android.graphics.Color;

import com.llc.drawit.domain.util.Constants;

/**
 * Class for mapping color to string and back
 */
public class ColorMapper {
    public static int stringToColor(String color) {
        switch (color) {
            case Constants.BLACK: {
                return Color.BLACK;
            }
            case Constants.BLUE: {
                return Color.BLUE;
            }
            case Constants.GREEN: {
                return Color.GREEN;
            }
            case Constants.WHITE: {
                return Color.WHITE;
            }
            default: {
                return Color.RED;
            }
        }
    }
    public static String colorToString(int color) {
        switch (color) {
            case Color.BLACK: {
                return Constants.BLACK;
            }
            case Color.BLUE: {
                return Constants.BLUE;
            }
            case Color.GREEN: {
                return Constants.GREEN;
            }
            case Color.WHITE: {
                return Constants.WHITE;
            }
            default: {
                return Constants.RED;
            }
        }
    }

    public static String colorToStringRus(int color) {
        switch (color) {
            case Color.BLACK: {
                return Constants.BLACK_RUS;
            }
            case Color.BLUE: {
                return Constants.BLUE_RUS;
            }
            case Color.GREEN: {
                return Constants.GREEN_RUS;
            }
            default: {
                return Constants.RED_RUS;
            }
        }
    }
}
