package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.sanction.BaseSanctionService;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class SanctionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView  setText;
        TextView  scoreText;
        TextView  playerText;
        ImageView cardTypeImage;
    }

    private final Context             mContext;
    private final LayoutInflater      mLayoutInflater;
    private final BaseSanctionService mSanctionService;
    private final BaseTeamService     mTeamService;
    private       TeamType            mTeamType;
    private final int                 mSetIndex;

    SanctionsListAdapter(Context context, LayoutInflater layoutInflater, BaseSanctionService sanctionService, BaseTeamService teamService, TeamType teamType) {
        this(context, layoutInflater, sanctionService, teamService, teamType, -1);
    }

    public SanctionsListAdapter(Context context, LayoutInflater layoutInflater, BaseSanctionService sanctionService, BaseTeamService teamService, TeamType teamType, int setIndex) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mSanctionService = sanctionService;
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
            count = mSanctionService.getGivenSanctions(mTeamType).size();
        } else {
            count = mSanctionService.getGivenSanctions(mTeamType, mSetIndex).size();
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
        View sanctionView = view;
        ViewHolder viewHolder;

        if (sanctionView == null) {
            sanctionView = mLayoutInflater.inflate(R.layout.sanction_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.setText = sanctionView.findViewById(R.id.set_text);
            viewHolder.scoreText = sanctionView.findViewById(R.id.score_text);
            viewHolder.playerText = sanctionView.findViewById(R.id.player_text);
            viewHolder.cardTypeImage = sanctionView.findViewById(R.id.sanction_type_image);
            sanctionView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) sanctionView.getTag();
        }

        Sanction sanction;

        if (mSetIndex < 0) {
            sanction = mSanctionService.getGivenSanctions(mTeamType).get(index);
        } else {
            sanction = mSanctionService.getGivenSanctions(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamType)) {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", sanction.getHomeTeamPoints(), sanction.getGuestTeamPoints()));
        } else {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", sanction.getGuestTeamPoints(), sanction.getHomeTeamPoints()));
        }
        if (mSetIndex < 0) {
            viewHolder.setText.setText(String.format(mContext.getResources().getString(R.string.set_number), (sanction.getSetIndex()+1)));
        } else {
            viewHolder.setText.setVisibility(View.GONE);
        }

        UiUtils.setSanctionImage(viewHolder.cardTypeImage, sanction.getSanctionType());

        if (sanction.getPlayer() < 0) {
            viewHolder.playerText.setVisibility(View.INVISIBLE);
        } else {
            if (sanction.getPlayer() > 0) {
                viewHolder.playerText.setText(UiUtils.formatNumberFromLocale(sanction.getPlayer()));
            } else {
                viewHolder.playerText.setText(mContext.getResources().getString(R.string.coach_abbreviation));
            }

            UiUtils.styleTeamText(mContext, mTeamService, mTeamType, sanction.getPlayer(), viewHolder.playerText);
        }

        return sanctionView;
    }
}
