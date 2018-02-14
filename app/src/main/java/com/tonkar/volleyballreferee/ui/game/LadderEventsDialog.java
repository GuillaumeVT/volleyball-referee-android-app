package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCard;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;

public class LadderEventsDialog {

    private       AlertDialog    mAlertDialog;
    private final LayoutInflater mLayoutInflater;
    private final Context        mContext;
    private final TeamType       mTeamType;
    private final BaseTeamService mBaseTeamService;

    LadderEventsDialog(LayoutInflater layoutInflater, final Context context, final TeamType teamType, final LadderItem ladderItem, final BaseTeamService baseTeamService) {
        mLayoutInflater = layoutInflater;
        mContext = context;
        mTeamType = teamType;
        mBaseTeamService = baseTeamService;

        View view = layoutInflater.inflate(R.layout.ladder_events_dialog, null);

        ListView timeoutList = view.findViewById(R.id.timeout_list);
        TimeoutsListAdapter timeoutsListAdapter = new TimeoutsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomeTimeouts().size() > 0 : ladderItem.getGuestTimeouts().size() > 0);
        timeoutList.setAdapter(timeoutsListAdapter);

        ListView substitutionList = view.findViewById(R.id.substitution_list);
        SubstitutionsListAdapter substitutionsListAdapter = new SubstitutionsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomeSubstitutions() : ladderItem.getGuestSubstitutions());
        substitutionList.setAdapter(substitutionsListAdapter);

        ListView cardList = view.findViewById(R.id.card_list);
        PenaltyCardsListAdapter penaltyCardsListAdapter = new PenaltyCardsListAdapter(TeamType.HOME.equals(mTeamType) ? ladderItem.getHomePenaltyCards() : ladderItem.getGuestPenaltyCards());
        cardList.setAdapter(penaltyCardsListAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setView(view);

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();
        }
    }

    private class TimeoutsListAdapter extends BaseAdapter {

        private boolean mHasTimeout;

        private TimeoutsListAdapter(boolean hasTimeout) {
            mHasTimeout = hasTimeout;
        }

        @Override
        public int getCount() {
            return mHasTimeout ? 1 : 0;
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
            ImageView imageView = (ImageView) view;

            if (imageView == null) {
                imageView = (ImageView) mLayoutInflater.inflate(R.layout.ladder_event_item, null);
            }

            imageView.setImageResource(R.drawable.ic_timeout);
            imageView.getDrawable().setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimaryText), PorterDuff.Mode.SRC_IN));

            return imageView;
        }
    }

    private class SubstitutionsListAdapter extends BaseAdapter {

        private List<Substitution> mSubstitutions;

        private SubstitutionsListAdapter(List<Substitution> substitutions) {
            mSubstitutions = substitutions;
        }

        @Override
        public int getCount() {
            return mSubstitutions.size();
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

            if (substitutionView == null) {
                substitutionView = mLayoutInflater.inflate(R.layout.substitution_list_item, null);
            }

            TextView scoreText = substitutionView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerInText = substitutionView.findViewById(R.id.player_in_text);
            TextView playerOutText = substitutionView.findViewById(R.id.player_out_text);

            Substitution substitution = mSubstitutions.get(index);
            playerInText.setText(String.valueOf(substitution.getPlayerIn()));
            playerOutText.setText(String.valueOf(substitution.getPlayerOut()));

            UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerInText);
            UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerOutText);

            return substitutionView;
        }
    }

    private class PenaltyCardsListAdapter extends BaseAdapter {

        private List<PenaltyCard> mPenaltyCards;

        private PenaltyCardsListAdapter(List<PenaltyCard> penaltyCards) {
            mPenaltyCards = penaltyCards;
        }

        @Override
        public int getCount() {
            return mPenaltyCards.size();
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

            if (penaltyCardView == null) {
                penaltyCardView = mLayoutInflater.inflate(R.layout.penalty_card_list_item, null);
            }

            TextView setText = penaltyCardView.findViewById(R.id.set_text);
            setText.setVisibility(View.GONE);
            TextView scoreText = penaltyCardView.findViewById(R.id.score_text);
            scoreText.setVisibility(View.GONE);
            TextView playerText = penaltyCardView.findViewById(R.id.player_text);
            ImageView cardTypeImage = penaltyCardView.findViewById(R.id.penalty_card_image);

            PenaltyCard penaltyCard = mPenaltyCards.get(index);

            switch (penaltyCard.getPenaltyCardType()) {
                case YELLOW:
                    cardTypeImage.setImageResource(R.drawable.yellow_card);
                    break;
                case RED:
                    cardTypeImage.setImageResource(R.drawable.red_card);
                    break;
                case RED_EXPULSION:
                    cardTypeImage.setImageResource(R.drawable.expulsion_card);
                    break;
                case RED_DISQUALIFICATION:
                    cardTypeImage.setImageResource(R.drawable.disqualification_card);
                    break;
            }

            if (penaltyCard.getPlayer() < 0) {
                playerText.setVisibility(View.GONE);
            } else {
                if (penaltyCard.getPlayer() > 0) {
                    playerText.setText(String.valueOf(penaltyCard.getPlayer()));
                } else {
                    playerText.setText(mContext.getResources().getString(R.string.coach_abbreviation));
                }

                if (mBaseTeamService instanceof BaseIndoorTeamService) {
                    UiUtils.styleBaseIndoorTeamText(mContext, (BaseIndoorTeamService) mBaseTeamService, mTeamType, penaltyCard.getPlayer(), playerText);
                } else {
                    UiUtils.styleBaseTeamText(mContext, mBaseTeamService, mTeamType, playerText);
                }
            }

            return penaltyCardView;
        }
    }
}
