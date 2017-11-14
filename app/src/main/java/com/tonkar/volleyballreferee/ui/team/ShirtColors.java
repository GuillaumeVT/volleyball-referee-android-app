package com.tonkar.volleyballreferee.ui.team;

import com.tonkar.volleyballreferee.R;

import java.util.Random;

class ShirtColors {

    private static final int[] SHIRT_COLORS = {
            R.color.colorShirt1, R.color.colorShirt2, R.color.colorShirt3, R.color.colorShirt4,
            R.color.colorShirt5, R.color.colorShirt6, R.color.colorShirt7, R.color.colorShirt8,
            R.color.colorShirt9, R.color.colorShirt10, R.color.colorShirt11, R.color.colorShirt12
    };

    static int[] getShirtColors() {
        return SHIRT_COLORS;
    }

    private static final Random RANDOM = new Random();

    static int getRandomShirtColor() {
        return SHIRT_COLORS[RANDOM.nextInt(SHIRT_COLORS.length - 1)];
    }
}
