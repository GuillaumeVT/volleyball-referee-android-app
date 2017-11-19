package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.List;
import java.util.Map;

public class IndoorCourtFragment extends CourtFragment {

    private IndoorTeamService mIndoorTeamService;

    public IndoorCourtFragment() {
        super();
    }

    public static IndoorCourtFragment newInstance() {
        IndoorCourtFragment fragment = new IndoorCourtFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Court", "Create indoor court view");
        initView();

        mIndoorTeamService = (IndoorTeamService) mTeamService;

        mView = inflater.inflate(R.layout.fragment_indoor_court, container, false);

        addButtonOnLeftSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.left_team_position_2));
        addButtonOnLeftSide(PositionType.POSITION_3, (Button) mView.findViewById(R.id.left_team_position_3));
        addButtonOnLeftSide(PositionType.POSITION_4, (Button) mView.findViewById(R.id.left_team_position_4));
        addButtonOnLeftSide(PositionType.POSITION_5, (Button) mView.findViewById(R.id.left_team_position_5));
        addButtonOnLeftSide(PositionType.POSITION_6, (Button) mView.findViewById(R.id.left_team_position_6));

        addButtonOnRightSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.right_team_position_2));
        addButtonOnRightSide(PositionType.POSITION_3, (Button) mView.findViewById(R.id.right_team_position_3));
        addButtonOnRightSide(PositionType.POSITION_4, (Button) mView.findViewById(R.id.right_team_position_4));
        addButtonOnRightSide(PositionType.POSITION_5, (Button) mView.findViewById(R.id.right_team_position_5));
        addButtonOnRightSide(PositionType.POSITION_6, (Button) mView.findViewById(R.id.right_team_position_6));

        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

        for (Map.Entry<PositionType,Button> entry : mLeftTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Integer> possibleReplacements = mIndoorTeamService.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                    if (possibleReplacements.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                        final PlayerAdapter playerAdapter = new PlayerAdapter(getContext(), mTeamOnLeftSide, possibleReplacements, positionType);
                        showPlayerSelectionDialog(playerAdapter);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                    }
                }
            });

            entry.getValue().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!mIndoorTeamService.isStartingLineupConfirmed()) {
                        int number = mIndoorTeamService.getPlayerAtPosition(mTeamOnLeftSide, positionType);
                        if (number > 0) {
                            mIndoorTeamService.substitutePlayer(mTeamOnLeftSide, number, PositionType.BENCH, ActionOriginType.USER);
                        }
                    }
                    return true;
                }
            });
        }

        for (Map.Entry<PositionType,Button> entry : mRightTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final List<Integer> possibleReplacements = mIndoorTeamService.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                    if (possibleReplacements.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                        final PlayerAdapter playerAdapter = new PlayerAdapter(getContext(), mTeamOnRightSide, possibleReplacements, positionType);
                        showPlayerSelectionDialog(playerAdapter);
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                    }
                }
            });

            entry.getValue().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!mIndoorTeamService.isStartingLineupConfirmed()) {
                        int number = mIndoorTeamService.getPlayerAtPosition(mTeamOnRightSide, positionType);
                        if (number > 0) {
                            mIndoorTeamService.substitutePlayer(mTeamOnRightSide, number, PositionType.BENCH, ActionOriginType.USER);
                        }
                    }
                    return true;
                }
            });
        }

        if (savedInstanceState != null) {
            AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getFragmentManager().findFragmentByTag("confirm_lineup");
            if (alertDialogFragment != null) {
                alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                    @Override
                    public void onNegativeButtonClicked() {
                    }

                    @Override
                    public void onPositiveButtonClicked() {
                        mIndoorTeamService.confirmStartingLineup();
                    }

                    @Override
                    public void onNeutralButtonClicked() {
                    }
                });
            }
        }

        return mView;
    }

    @Override
    protected void applyColor(TeamType teamType, int number, Button button) {
        if (mIndoorTeamService.isLibero(teamType, number)) {
            UiUtils.colorTeamButton(mView.getContext(), mIndoorTeamService.getLiberoColor(teamType), button);
        } else {
            super.applyColor(teamType, button);
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        super.onTeamRotated(teamType);
        confirmStartingLineup();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        super.onPlayerChanged(teamType, number, positionType, actionOriginType);
        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup();
        }
    }

    private void confirmStartingLineup() {
        if (!mIndoorTeamService.isStartingLineupConfirmed()
                && mIndoorTeamService.getPlayersOnCourt(TeamType.HOME).size() == 6 && mIndoorTeamService.getPlayersOnCourt(TeamType.GUEST).size() == 6) {
            AlertDialogFragment alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.confirm_lineup_title), getResources().getString(R.string.confirm_lineup_question),
                    getResources().getString(android.R.string.no), getResources().getString(android.R.string.yes));
            alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                @Override
                public void onNegativeButtonClicked() {}

                @Override
                public void onPositiveButtonClicked() {
                    mIndoorTeamService.confirmStartingLineup();
                }

                @Override
                public void onNeutralButtonClicked() {}
            });
            alertDialogFragment.show(getActivity().getFragmentManager(), "confirm_lineup");
        }
    }

    private void showPlayerSelectionDialog(final PlayerAdapter playerAdapter) {
        final GridView gridView = new GridView(getContext());
        gridView.setNumColumns(GridView.AUTO_FIT);
        gridView.setGravity(Gravity.CENTER);
        gridView.setPadding(8, 8, 8, 8);
        gridView.setAdapter(playerAdapter);

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.select_player_title)).setView(gridView);
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        final AlertDialog alertDialog = builder.create();

        playerAdapter.setDialog(alertDialog);

        alertDialog.show();
    }

    private class PlayerAdapter extends BaseAdapter {

        private final Context       mContext;
        private final TeamType      mTeamType;
        private final List<Integer> mPossibleReplacements;
        private final PositionType  mPositionType;
        private       AlertDialog   mDialog;

        private PlayerAdapter(Context context, final TeamType teamType, final List<Integer> possibleReplacements, final PositionType positionType) {
            mContext = context;
            mTeamType = teamType;
            mPossibleReplacements = possibleReplacements;
            mPositionType = positionType;
        }

        @Override
        public int getCount() {
            return mPossibleReplacements.size();
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
        public View getView(int position, View convertView, ViewGroup parent) {
            final int playerShirtNumber = mPossibleReplacements.get(position);
            Button button;

            if (convertView == null) {
                button = new Button(mContext);
            } else {
                button = (Button) convertView;
            }

            button.setText(String.valueOf(playerShirtNumber));
            applyColor(mTeamType, playerShirtNumber, button);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final int number = Integer.parseInt(((Button) view).getText().toString());
                    Log.i("VBR-Court", String.format("Substitute %s team player at %s position by #%d player", mTeamType.toString(), mPositionType.toString(), number));
                    mIndoorTeamService.substitutePlayer(mTeamType, number, mPositionType, ActionOriginType.USER);

                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                }
            });

            return button;
        }

        void setDialog(AlertDialog dialog) {
            mDialog = dialog;
        }
    }

}
