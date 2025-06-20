package com.llc.drawit.presentation.util.color;

import android.util.Pair;

import com.llc.drawit.R;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class DefaultProfileImageColorsUtil {
    private static final LinkedHashMap<Integer, Integer> colors = new LinkedHashMap<>(Map.of(
            R.color.blue, R.color.white,
            R.color.red, R.color.white,
            R.color.white, R.color.black,
            R.color.purple, R.color.white
    ));

    /**
     * @return random pair of colors (backgroundColor:textColor)
     */
    public static Pair<Integer, Integer> getRandomColorsPair() {
        int randomIndex = (int) (Math.random() * colors.size());
        Integer randomKey = colors.keySet().stream().collect(Collectors.toList()).get(randomIndex);
        return Pair.create(randomKey, colors.get(randomKey));
    }
}
