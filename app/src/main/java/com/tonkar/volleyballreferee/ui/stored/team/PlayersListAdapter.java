package com.tonkar.volleyballreferee.ui.stored.team;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.api.ApiPlayer;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.ArrayList;
import java.util.List;

public class PlayersListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView playerNumber;
        TextView playerName;
    }

    private final LayoutInflater  mLayoutInflater;
    private final Context         mContext;
    private final IBaseTeam       mTeamService;
    private final TeamType        mTeamType;
    private final List<ApiPlayer> mPlayers;

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
            playerItem = mLayoutInflater.inflate(R.layout.full_player_item, null);
            viewHolder = new ViewHolder();
            viewHolder.playerNumber = playerItem.findViewById(R.id.player_number);
            viewHolder.playerName = playerItem.findViewById(R.id.player_name);
            playerItem.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) playerItem.getTag();
        }

        ApiPlayer player = mPlayers.get(index);
        viewHolder.playerNumber.setText(UiUtils.formatNumberFromLocale(player.getNum()));

        if (mTeamService.isLibero(mTeamType, player.getNum())) {
            UiUtils.colorTeamText(mContext, mTeamService.getLiberoColor(mTeamType), viewHolder.playerNumber);
        } else {
            UiUtils.colorTeamText(mContext, mTeamService.getTeamColor(mTeamType), viewHolder.playerNumber);
        }

        if (mTeamService.isCaptain(mTeamType, player.getNum())) {
            viewHolder.playerNumber.setPaintFlags(viewHolder.playerNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        } else {
            viewHolder.playerNumber.setPaintFlags(viewHolder.playerNumber.getPaintFlags() & (~ Paint.UNDERLINE_TEXT_FLAG));
        }

        viewHolder.playerName.setText(player.getName());

        return playerItem;
    }
}
