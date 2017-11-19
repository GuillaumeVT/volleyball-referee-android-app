package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.support.v4.content.ContextCompat;

import com.tonkar.volleyballreferee.R;

import java.util.Random;

class ShirtColors {

    private static final int[] SHIRT_COLOR_IDS = {
            R.color.colorShirt1, R.color.colorShirt2, R.color.colorShirt3, R.color.colorShirt4,
            R.color.colorShirt5, R.color.colorShirt6, R.color.colorShirt7, R.color.colorShirt8,
            R.color.colorShirt9, R.color.colorShirt10, R.color.colorShirt11, R.color.colorShirt12
    };

    static int[] getShirtColorIds() {
        return SHIRT_COLOR_IDS;
    }

    private static final Random RANDOM = new Random();

    static int getRandomShirtColor(Context context) {
        int colorId = SHIRT_COLOR_IDS[RANDOM.nextInt(SHIRT_COLOR_IDS.length - 1)];
        return ContextCompat.getColor(context, colorId);
    }
}
