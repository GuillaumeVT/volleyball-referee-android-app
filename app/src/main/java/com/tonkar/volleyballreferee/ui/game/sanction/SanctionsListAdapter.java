package com.tonkar.volleyballreferee.ui.game.sanction;

import android.content.Context;
import android.view.*;
import android.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.api.model.SanctionDto;
import com.tonkar.volleyballreferee.engine.game.sanction.IBaseSanction;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;

public class SanctionsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView  setText;
        TextView  scoreText;
        TextView  playerText;
        ImageView cardTypeImage;
    }

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final IBaseSanction  mSanctionService;
    private final IBaseTeam      mTeamService;
    private       TeamType       mTeamType;
    private final int            mSetIndex;

    SanctionsListAdapter(Context context,
                         LayoutInflater layoutInflater,
                         IBaseSanction sanctionService,
                         IBaseTeam teamService,
                         TeamType teamType) {
        this(context, layoutInflater, sanctionService, teamService, teamType, -1);
    }

    public SanctionsListAdapter(Context context,
                                LayoutInflater layoutInflater,
                                IBaseSanction sanctionService,
                                IBaseTeam teamService,
                                TeamType teamType,
                                int setIndex) {
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
            count = mSanctionService.getAllSanctions(mTeamType).size();
        } else {
            count = mSanctionService.getAllSanctions(mTeamType, mSetIndex).size();
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

        SanctionDto sanction;

        if (mSetIndex < 0) {
            sanction = mSanctionService.getAllSanctions(mTeamType).get(index);
        } else {
            sanction = mSanctionService.getAllSanctions(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamType)) {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", sanction.getHomePoints(), sanction.getGuestPoints()));
        } else {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", sanction.getGuestPoints(), sanction.getHomePoints()));
        }
        if (mSetIndex < 0) {
            viewHolder.setText.setText(String.format(mContext.getString(R.string.set_number), (sanction.getSet() + 1)));
        } else {
            viewHolder.setText.setVisibility(View.GONE);
        }

        UiUtils.setSanctionImage(viewHolder.cardTypeImage, sanction.getCard());

        if (sanction.isTeam()) {
            viewHolder.playerText.setVisibility(View.INVISIBLE);
        } else {
            if (sanction.isPlayer()) {
                viewHolder.playerText.setText(UiUtils.formatNumberFromLocale(sanction.getNum()));
            } else if (sanction.isCoach()) {
                viewHolder.playerText.setText(mContext.getString(R.string.coach_abbreviation));
            }

            viewHolder.playerText.setVisibility(View.VISIBLE);
            UiUtils.styleTeamText(mContext, mTeamService, mTeamType, sanction.getNum(), viewHolder.playerText);
        }

        return sanctionView;
    }
}
