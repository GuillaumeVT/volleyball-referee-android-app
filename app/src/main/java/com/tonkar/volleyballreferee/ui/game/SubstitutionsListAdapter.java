package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.AbstractMap;
import java.util.List;

public class SubstitutionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        Button playerInButton;
        Button playerOutButton;
    }

    private final Context           mContext;
    private final LayoutInflater    mLayoutInflater;
    private final IndoorTeamService mIndoorTeamService;
    private       TeamType          mTeamType;

    SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, IndoorTeamService indoorTeamService, TeamType teamType) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mIndoorTeamService = indoorTeamService;
        mTeamType = teamType;
    }

    public void setTeamType(TeamType teamType) {
        mTeamType = teamType;
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    @Override
    public int getCount() {
        return mIndoorTeamService.getNumberOfSubstitutions(mTeamType);
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
        View substitutionView = view;
        ViewHolder viewHolder;

        if (substitutionView == null) {
            substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.playerInButton = substitutionView.findViewById(R.id.player_in_button);
            viewHolder.playerOutButton = substitutionView.findViewById(R.id.player_out_button);
            substitutionView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) substitutionView.getTag();
        }

        List<AbstractMap.SimpleEntry<Integer, Integer>> substitutions = mIndoorTeamService.getSubstitutions(mTeamType);

        viewHolder.playerInButton.setText(String.valueOf(substitutions.get(index).getKey()));
        viewHolder.playerOutButton.setText(String.valueOf(substitutions.get(index).getValue()));

        UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), viewHolder.playerInButton);
        UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), viewHolder.playerOutButton);

        return substitutionView;
    }
}
