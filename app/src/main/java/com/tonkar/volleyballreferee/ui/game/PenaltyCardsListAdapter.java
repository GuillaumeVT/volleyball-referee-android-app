package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.card.BasePenaltyCardService;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCard;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.Locale;

public class PenaltyCardsListAdapter extends BaseAdapter {

    static class ViewHolder {
        TextView  setText;
        TextView  scoreText;
        TextView  playerText;
        ImageView cardTypeImage;
    }

    private final Context                mContext;
    private final LayoutInflater         mLayoutInflater;
    private final BasePenaltyCardService mPenaltyCardService;
    private final BaseTeamService        mTeamService;
    private       TeamType               mTeamType;
    private final int                    mSetIndex;

    PenaltyCardsListAdapter(Context context, LayoutInflater layoutInflater, BasePenaltyCardService penaltyCardService, BaseTeamService teamService, TeamType teamType) {
        this(context, layoutInflater, penaltyCardService, teamService, teamType, -1);
    }

    public PenaltyCardsListAdapter(Context context, LayoutInflater layoutInflater, BasePenaltyCardService penaltyCardService, BaseTeamService teamService, TeamType teamType, int setIndex) {
        mContext = context;
        mLayoutInflater = layoutInflater;
        mPenaltyCardService = penaltyCardService;
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
            count = mPenaltyCardService.getGivenPenaltyCards(mTeamType).size();
        } else {
            count = mPenaltyCardService.getGivenPenaltyCards(mTeamType, mSetIndex).size();
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
        View penaltyCardView = view;
        ViewHolder viewHolder;

        if (penaltyCardView == null) {
            penaltyCardView = mLayoutInflater.inflate(R.layout.penalty_card_list_item, null);
            viewHolder = new ViewHolder();
            viewHolder.setText = penaltyCardView.findViewById(R.id.set_text);
            viewHolder.scoreText = penaltyCardView.findViewById(R.id.score_text);
            viewHolder.playerText = penaltyCardView.findViewById(R.id.player_text);
            viewHolder.cardTypeImage = penaltyCardView.findViewById(R.id.penalty_card_image);
            penaltyCardView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) penaltyCardView.getTag();
        }

        PenaltyCard penaltyCard;

        if (mSetIndex < 0) {
            penaltyCard = mPenaltyCardService.getGivenPenaltyCards(mTeamType).get(index);
        } else {
            penaltyCard = mPenaltyCardService.getGivenPenaltyCards(mTeamType, mSetIndex).get(index);
        }

        if (TeamType.HOME.equals(mTeamType)) {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", penaltyCard.getHomeTeamPoints(), penaltyCard.getGuestTeamPoints()));
        } else {
            viewHolder.scoreText.setText(String.format(Locale.getDefault(), "%d-%d", penaltyCard.getGuestTeamPoints(), penaltyCard.getHomeTeamPoints()));
        }
        if (mSetIndex < 0) {
            viewHolder.setText.setText(String.format(mContext.getResources().getString(R.string.set_number), (penaltyCard.getSetIndex()+1)));
        } else {
            viewHolder.setText.setVisibility(View.GONE);
        }

        switch (penaltyCard.getPenaltyCardType()) {
            case YELLOW:
                viewHolder.cardTypeImage.setImageResource(R.drawable.yellow_card);
                break;
            case RED:
                viewHolder.cardTypeImage.setImageResource(R.drawable.red_card);
                break;
            case RED_EXPULSION:
                viewHolder.cardTypeImage.setImageResource(R.drawable.expulsion_card);
                break;
            case RED_DISQUALIFICATION:
                viewHolder.cardTypeImage.setImageResource(R.drawable.disqualification_card);
                break;
        }

        if (penaltyCard.getPlayer() < 0) {
            viewHolder.playerText.setVisibility(View.GONE);
        } else {
            if (penaltyCard.getPlayer() > 0) {
                viewHolder.playerText.setText(String.valueOf(penaltyCard.getPlayer()));
            } else {
                viewHolder.playerText.setText(mContext.getResources().getString(R.string.coach_abbreviation));
            }

            if (mTeamService instanceof IndoorTeamService) {
                UiUtils.styleBaseIndoorTeamText(mContext, (IndoorTeamService) mTeamService, mTeamType, penaltyCard.getPlayer(), viewHolder.playerText);
            } else {
                UiUtils.styleBaseTeamText(mContext, mTeamService, mTeamType, viewHolder.playerText);
            }
        }

        return penaltyCardView;
    }
}
