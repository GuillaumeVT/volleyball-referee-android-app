package com.tonkar.volleyballreferee.ui.data.game;

import android.content.Context;
import android.view.LayoutInflater;

import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public class LineupSnowAdapter extends LineupAdapter {

    LineupSnowAdapter(LayoutInflater layoutInflater, Context context, IBaseTeam teamService, TeamType teamType, int setIndex) {
        super(layoutInflater, context, teamService, teamType, setIndex);
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    protected boolean isVisible(PositionType positionType) {
        boolean visible;

        switch (positionType) {
            case POSITION_1:
            case POSITION_2:
            case POSITION_3:
                visible = true;
                break;
            default:
                visible = false;
        }

        return visible;
    }

    @Override
    protected PositionType viewIndexToPosition(int index) {
        PositionType positionType = null;

        switch (index) {
            case 0:
                positionType = PositionType.POSITION_1;
                break;
            case 1:
                positionType = PositionType.POSITION_2;
                break;
            case 2:
                positionType = PositionType.POSITION_3;
                break;
        }

        return positionType;
    }

}