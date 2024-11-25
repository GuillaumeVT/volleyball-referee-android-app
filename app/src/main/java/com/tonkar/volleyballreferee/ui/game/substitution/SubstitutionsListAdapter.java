package com.tonkar.volleyballreferee.ui.game.substitution;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.SubstitutionDto;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class SubstitutionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView scoreText;
        TextView playerInText;
        TextView playerOutText;
    }

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final IBaseTeam      mTeamService;
    private       TeamType       mTeamType;
    private final int            mSetIndex;

    SubstitutionsListAdapter(Context context, LayoutInflater layoutInflater, IBaseTeam teamService, TeamType teamType) {
        this(context, layoutInflater, teamService, teamType, -1);
    }

    public SubstitutionsListAdapter(Context context,
                                    LayoutInflater layoutInflater,
                                    IBaseTeam teamService,
                                    TeamType teamType,
                                    int setIndex) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mTeamService = teamService;
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

        if (mSetIndex < 0) {
            count = mTeamService.getSubstitutions(mTeamType).size();
        } else {
            count = mTeamService.getSubstitutions(mTeamType, mSetIndex).size();
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
    public View getView(int index, View view, ViewGroup parent) {
        View substitutionView = view;
        ViewHolder viewHolder;

        if (substitutionView == null) {
            substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.scoreText = substitutionView.findViewById(R.id.score_text);
            viewHolder.playerInText = substitutionView.findViewById(R.id.player_in_text);
            viewHolder.playerOutText = substitutionView.findViewById(R.id.player_out_text);
            substitutionView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) substitutionView.getTag();
        }

        SubstitutionDto substitution;

        if (mSetIndex < 0) {
            substitution = mTeamService.getSubstitutions(mTeamType).get(index);
        } else {
            substitution = mTeamService.getSubstitutions(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamType)) {
            viewHolder.scoreText.setText(
                    String.format(Locale.getDefault(), "%d-%d", substitution.getHomePoints(), substitution.getGuestPoints()));
        } else {
            viewHolder.scoreText.setText(
                    String.format(Locale.getDefault(), "%d-%d", substitution.getGuestPoints(), substitution.getHomePoints()));
        }
        viewHolder.playerInText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerIn()));
        viewHolder.playerOutText.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerOut()));

        UiUtils.styleTeamText(mContext, mTeamService, mTeamType, substitution.getPlayerIn(), viewHolder.playerInText);
        UiUtils.styleTeamText(mContext, mTeamService, mTeamType, substitution.getPlayerOut(), viewHolder.playerOutText);

        return substitutionView;
    }
}
