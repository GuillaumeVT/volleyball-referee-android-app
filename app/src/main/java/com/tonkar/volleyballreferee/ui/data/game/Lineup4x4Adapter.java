package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Context;
import android.view.LayoutInflater;

import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public class Lineup4x4Adapter extends LineupAdapter {

    Lineup4x4Adapter(LayoutInflater layoutInflater, Context context, IBaseTeam teamService, TeamType teamType, int setIndex) {
        super(layoutInflater, context, teamService, teamType, setIndex);
    }

    @Override
    public int getCount() {
        return 5;
    }

    @Override
    protected boolean isVisible(PositionType positionType) {
        return switch (positionType) {
            case POSITION_1, POSITION_2, POSITION_3, POSITION_4 -> true;
            default -> false;
        };
    }

    @Override
    protected PositionType viewIndexToPosition(int index) {
        return switch (index) {
            case 0 -> PositionType.POSITION_4;
            case 1 -> PositionType.POSITION_3;
            case 2 -> PositionType.POSITION_2;
            case 3 -> PositionType.POSITION_5;
            case 4 -> PositionType.POSITION_1;
            default -> null;
        };
    }

}