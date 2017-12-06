package com.tonkar.volleyballreferee.business.game;

import android.content.SharedPreferences;
import android.util.Log;

import com.tonkar.volleyballreferee.rules.Rules;

public class GameFactory {

    public static IndoorGame createIndoorGame() {
        Log.i("VBR-Core", "Create indoor game with official rules");
        Rules.OFFICIAL_INDOOR_RULES.printRules();
        return new IndoorGame(Rules.OFFICIAL_INDOOR_RULES);
    }

    public static IndoorGame createIndoorGame(final SharedPreferences sharedPreferences) {
        Log.i("VBR-Core", "Create indoor game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_INDOOR_RULES);
        rules.printRules();
        return new IndoorGame(rules);
    }

    public static BeachGame createBeachGame() {
        Log.i("VBR-Core", "Create beach game with official rules");
        Rules.OFFICIAL_BEACH_RULES.printRules();
        return new BeachGame(Rules.OFFICIAL_BEACH_RULES);
    }

    public static BeachGame createBeachGame(final SharedPreferences sharedPreferences) {
        Log.i("VBR-Core", "Create beach game with user rules");
        Rules rules = Rules.createRulesFromPref(sharedPreferences, Rules.OFFICIAL_BEACH_RULES);
        rules.printRules();
        return new BeachGame(rules);
    }

}
