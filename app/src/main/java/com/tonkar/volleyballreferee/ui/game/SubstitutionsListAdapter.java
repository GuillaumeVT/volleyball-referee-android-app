package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;

public class SubstitutionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        Button playerInButton;
        Button playerOutButton;
    }

    private final Context               mContext;
    private final LayoutInflater        mLayoutInflater;
    private final BaseIndoorTeamService mIndoorTeamService;
    private       TeamType              mTeamType;
    private final int                   mSetIndex;

    SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, BaseIndoorTeamService indoorTeamService, TeamType teamType) {
        this(context, layoutInflater, indoorTeamService, teamType, -1);
    }

    public SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, BaseIndoorTeamService indoorTeamService, TeamType teamType, int setIndex) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mIndoorTeamService = indoorTeamService;
        mTeamType = teamType;
        mSetIndex = setIndex;
    }

    public void setTeamType(TeamType teamType) {
        mTeamType = teamType;
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    @Override
    public int getCount() {
        int count;

        if (mSetIndex > 0) {
            count = mIndoorTeamService.getSubstitutions(mTeamType, mSetIndex).size();
        } else {
            count = mIndoorTeamService.getSubstitutions(mTeamType).size();
        }

        return count;
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

        List<Substitution> substitutions;

        if (mSetIndex > 0) {
            substitutions = mIndoorTeamService.getSubstitutions(mTeamType, mSetIndex);
        } else {
            substitutions = mIndoorTeamService.getSubstitutions(mTeamType);
        }

        viewHolder.playerInButton.setText(String.valueOf(substitutions.get(index).getPlayerIn()));
        viewHolder.playerOutButton.setText(String.valueOf(substitutions.get(index).getPlayerOut()));

        UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), viewHolder.playerInButton);
        UiUtils.colorTeamButton(mContext, mIndoorTeamService.getTeamColor(mTeamType), viewHolder.playerOutButton);

        return substitutionView;
    }
}
