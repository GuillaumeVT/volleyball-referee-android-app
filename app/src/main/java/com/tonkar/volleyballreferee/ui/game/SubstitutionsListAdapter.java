package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.Substitution;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Locale;

public class SubstitutionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView scoreText;
        TextView playerInText;
        TextView playerOutText;
    }

    private final Context               mContext;
    private final LayoutInflater        mLayoutInflater;
    private final BaseIndoorTeamService mIndoorTeamService;
    private       TeamType              mTeamType;
    private       TeamType              mTeamOnLeftSide;
    private final int                   mSetIndex;

    SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, BaseIndoorTeamService indoorTeamService, TeamType teamType, TeamType teamOnLeftSide) {
        this(context, layoutInflater, indoorTeamService, teamType, teamOnLeftSide, -1);
    }

    public SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, BaseIndoorTeamService indoorTeamService, TeamType teamType, TeamType teamOnLeftSide, int setIndex) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mIndoorTeamService = indoorTeamService;
        mTeamType = teamType;
        mTeamOnLeftSide = teamOnLeftSide;
        mSetIndex = setIndex;
    }

    public void setTeamType(TeamType teamType) {
        mTeamType = teamType;
    }

    public TeamType getTeamType() {
        return mTeamType;
    }

    public void setTeamOnLeftSide(TeamType teamType) {
        mTeamOnLeftSide = teamType;
    }

    @Override
    public int getCount() {
        int count;

        if (mSetIndex < 0) {
            count = mIndoorTeamService.getSubstitutions(mTeamType).size();
        } else {
            count = mIndoorTeamService.getSubstitutions(mTeamType, mSetIndex).size();
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
            viewHolder.scoreText = substitutionView.findViewById(R.id.score_text);
            viewHolder.playerInText = substitutionView.findViewById(R.id.player_in_text);
            viewHolder.playerOutText = substitutionView.findViewById(R.id.player_out_text);
            substitutionView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) substitutionView.getTag();
        }

        Substitution substitution;

        if (mSetIndex < 0) {
            substitution = mIndoorTeamService.getSubstitutions(mTeamType).get(index);
        } else {
            substitution = mIndoorTeamService.getSubstitutions(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamOnLeftSide)) {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", substitution.getHomeTeamPoints(), substitution.getGuestTeamPoints()));
        } else {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", substitution.getGuestTeamPoints(), substitution.getHomeTeamPoints()));
        }
        viewHolder.playerInText.setText(String.valueOf(substitution.getPlayerIn()));
        viewHolder.playerOutText.setText(String.valueOf(substitution.getPlayerOut()));

        UiUtils.styleBaseIndoorTeamText(mContext, mIndoorTeamService, mTeamType, substitution.getPlayerIn(), viewHolder.playerInText);
        UiUtils.styleBaseIndoorTeamText(mContext, mIndoorTeamService, mTeamType, substitution.getPlayerOut(), viewHolder.playerOutText);

        return substitutionView;
    }
}
