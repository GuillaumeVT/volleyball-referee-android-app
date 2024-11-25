package com.tonkar.volleyballreferee.ui.data.team;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.PlayerDto;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public class PlayersListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView playerNumber;
        TextView playerName;
    }

    private final LayoutInflater  mLayoutInflater;
    private final Context         mContext;
    private final IBaseTeam       mTeamService;
    private final TeamType        mTeamType;
    private final List<PlayerDto> mPlayers;

    PlayersListAdapter(LayoutInflater layoutInflater, Context context, IBaseTeam teamService, TeamType teamType) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mTeamService = teamService;
        mTeamType = teamType;
        mPlayers = new ArrayList<>(mTeamService.getPlayers(mTeamType));
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
    public View getView(int index, View view, ViewGroup parent) {
        View playerItem = view;
        ViewHolder viewHolder;

        if (view == null) {
            playerItem = mLayoutInflater.inflate(R.layout.full_player_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.playerNumber = playerItem.findViewById(R.id.player_number);
            viewHolder.playerName = playerItem.findViewById(R.id.player_name);
            playerItem.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) playerItem.getTag();
        }

        PlayerDto player = mPlayers.get(index);
        viewHolder.playerNumber.setText(UiUtils.formatNumberFromLocale(player.getNum()));
        UiUtils.styleTeamText(mContext, mTeamService, mTeamType, player.getNum(), viewHolder.playerNumber);

        viewHolder.playerName.setText(player.getName());

        return playerItem;
    }
}
