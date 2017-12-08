package com.tonkar.volleyballreferee.ui.history;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayersListAdapter extends BaseAdapter {

    private final LayoutInflater        mLayoutInflater;
    private final Context               mContext;
    private final BaseIndoorTeamService mIndoorTeamService;
    private final TeamType              mTeamType;
    private final List<Integer>         mPlayers;

    PlayersListAdapter(LayoutInflater layoutInflater, Context context, BaseIndoorTeamService indoorTeamService, TeamType teamType) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mIndoorTeamService = indoorTeamService;
        mTeamType = teamType;
        mPlayers = new ArrayList<>(mIndoorTeamService.getPlayers(mTeamType));
    }

    @Override
    public int getCount() {
        return mPlayers.size();
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int index, View view, ViewGroup viewGroup) {
        Button playerButton;

        if (view == null) {
            playerButton = (Button) mLayoutInflater.inflate(R.layout.player_item, null);
            UiUtils.addMarginLegacyButton(playerButton);
        } else {
            playerButton = (Button) view;
        }

        int number = mPlayers.get(index);
        playerButton.setText(String.valueOf(number));

        if (mIndoorTeamService.isLibero(mTeamType, number)) {
            UiUtils.colorTeamButton(mContext, mIndoorTeamService.getLiberoColor(mTeamType), playerButton);
        } else {
            UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), playerButton);
        }

        if (mIndoorTeamService.isCaptain(mTeamType, number)) {
            playerButton.setPaintFlags(playerButton.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            playerButton.setPaintFlags(playerButton.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
        }

        return playerButton;
    }
}
