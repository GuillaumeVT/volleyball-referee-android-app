package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.team.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.card.PenaltyCardType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.team.PlayerToggleButton;

import java.util.ArrayList;
import java.util.List;

public abstract class PenaltyCardSelectionDialog {

    private AlertDialog         mAlertDialog;
    private GameType            mGameType;
    private IndoorPlayerAdapter mPlayerAdapter;

    PenaltyCardSelectionDialog(LayoutInflater layoutInflater, final Context context, String title, final GameService gameService, final TeamType teamType) {
        mGameType = gameService.getGameType();

        View penaltyCardsView = layoutInflater.inflate(R.layout.penalty_card_selection_dialog, null);

        final Spinner penaltyCardSpinner = penaltyCardsView.findViewById(R.id.penalty_card_spinner);
        final PenaltyCardTypeAdapter penaltyCardTypeAdapter = new PenaltyCardTypeAdapter(layoutInflater, context);
        penaltyCardSpinner.setAdapter(penaltyCardTypeAdapter);

        final GridView penaltyCardGrid = penaltyCardsView.findViewById(R.id.penalty_card_grid);

        if (GameType.INDOOR.equals(mGameType)) {
            mPlayerAdapter = new IndoorPlayerAdapter(context, gameService, teamType);
            penaltyCardGrid.setAdapter(mPlayerAdapter);
        } else {
            penaltyCardGrid.setVisibility(View.GONE);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
        builder.setTitle(title).setView(penaltyCardsView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (GameType.INDOOR.equals(mGameType)) {
                    onPenaltyCard(teamType, penaltyCardTypeAdapter.getItem(penaltyCardSpinner.getSelectedItemPosition()), mPlayerAdapter.getSelectedPlayer());
                } else {
                    onPenaltyCard(teamType, penaltyCardTypeAdapter.getItem(penaltyCardSpinner.getSelectedItemPosition()), -1);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });

        mAlertDialog = builder.create();
    }

    public void show() {
        if (mAlertDialog != null) {
            mAlertDialog.show();

            if (GameType.INDOOR.equals(mGameType)) {
                mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
            }
        }
    }

    public abstract void onPenaltyCard(TeamType teamType, PenaltyCardType penaltyCardType, int player);

    private class PenaltyCardTypeAdapter extends BaseAdapter {

        private final LayoutInflater        mLayoutInflater;
        private final Context               mContext;
        private final List<PenaltyCardType> mPenaltyCardTypes;

        PenaltyCardTypeAdapter(LayoutInflater layoutInflater, Context context) {
            mLayoutInflater = layoutInflater;
            mContext = context;
            mPenaltyCardTypes = new ArrayList<>();
            mPenaltyCardTypes.add(PenaltyCardType.YELLOW);
            mPenaltyCardTypes.add(PenaltyCardType.RED);
            mPenaltyCardTypes.add(PenaltyCardType.RED_EXPULSION);
            mPenaltyCardTypes.add(PenaltyCardType.RED_DISQUALIFICATION);
        }

        @Override
        public int getCount() {
            return mPenaltyCardTypes.size();
        }

        @Override
        public PenaltyCardType getItem(int i) {
            return mPenaltyCardTypes.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int index, View view, ViewGroup viewGroup) {
            View itemView;

            if (view == null) {
                itemView = mLayoutInflater.inflate(R.layout.penalty_card_selection_item, null);
            } else {
                itemView = view;
            }

            ImageView penaltyCardImage = itemView.findViewById(R.id.penalty_card_image);
            TextView penaltyCardText = itemView.findViewById(R.id.penalty_card_text);

            switch (mPenaltyCardTypes.get(index)) {
                case YELLOW:
                    penaltyCardText.setText(mContext.getResources().getString(R.string.yellow_card));
                    penaltyCardImage.setImageResource(R.drawable.yellow_card);
                    break;
                case RED:
                    penaltyCardText.setText(mContext.getResources().getString(R.string.red_card));
                    penaltyCardImage.setImageResource(R.drawable.red_card);
                    break;
                case RED_EXPULSION:
                    penaltyCardText.setText(mContext.getResources().getString(R.string.red_card_expulsion));
                    penaltyCardImage.setImageResource(R.drawable.expulsion_card);
                    break;
                case RED_DISQUALIFICATION:
                    penaltyCardText.setText(mContext.getResources().getString(R.string.red_card_disqualification));
                    penaltyCardImage.setImageResource(R.drawable.disqualification_card);
                    break;
            }

            return itemView;
        }
    }

    private class IndoorPlayerAdapter extends BaseAdapter {

        private final Context               mContext;
        private final BaseIndoorTeamService mTeamService;
        private final TeamType              mTeamType;
        private final List<Integer>         mPlayers;
        private       int                   mSelectedPlayer;

        private IndoorPlayerAdapter(Context context, BaseTeamService teamService, TeamType teamType) {
            mContext = context;
            mTeamService = (BaseIndoorTeamService) teamService;
            mTeamType = teamType;
            mPlayers = new ArrayList<>(mTeamService.getPlayers(mTeamType));
            mSelectedPlayer = -1;
        }

        @Override
        public int getCount() {
            // Coach + players
            return 1 + mPlayers.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, final ViewGroup parent) {
            PlayerToggleButton button;

            if (view == null) {
                button = new PlayerToggleButton(mContext);
            } else {
                button = (PlayerToggleButton) view;
            }

            final int player;

            if (mPlayers.size() > position) {
                player = mPlayers.get(position);
                button.setText(String.valueOf(player));
                if (mTeamService.isLibero(mTeamType, player)) {
                    button.setColor(mContext, mTeamService.getLiberoColor(mTeamType));
                } else {
                    button.setColor(mContext, mTeamService.getTeamColor(mTeamType));
                }
            } else {
                player = 0;
                button.setText(mContext.getResources().getString(R.string.coach_abbreviation));
                button.setColor(mContext, mTeamService.getTeamColor(mTeamType));
            }


            button.setOnCheckedChangeListener(new PlayerToggleButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(PlayerToggleButton button, boolean isChecked) {
                    UiUtils.animate(mContext, button);
                    if (isChecked) {
                        for (int index = 0; index < parent.getChildCount(); index++) {
                            PlayerToggleButton child = (PlayerToggleButton) parent.getChildAt(index);
                            if (child != button && child.isChecked()) {
                                child.setChecked(false);
                                mSelectedPlayer = -1;
                            }
                        }
                        mSelectedPlayer = player;
                        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    } else {
                        mSelectedPlayer = -1;
                        mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                }
            });

            return button;
        }

        public int getSelectedPlayer() {
            return mSelectedPlayer;
        }
    }

}
