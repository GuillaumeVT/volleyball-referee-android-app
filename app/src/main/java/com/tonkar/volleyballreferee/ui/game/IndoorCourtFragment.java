package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.team.IndoorPlayerSelectionDialog;

import java.util.Map;
import java.util.Set;

public class IndoorCourtFragment extends CourtFragment {

    private IndoorTeamService mIndoorTeamService;
    private LayoutInflater    mLayoutInflater;

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
        mView = inflater.inflate(R.layout.fragment_indoor_court, container, false);

        initView();

        mIndoorTeamService = (IndoorTeamService) mTeamService;
        mLayoutInflater = inflater;

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

        for (Map.Entry<PositionType, Button> entry : mLeftTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Set<Integer> possibleReplacements = mIndoorTeamService.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                    if (possibleReplacements.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                        showPlayerSelectionDialog(mTeamOnLeftSide, positionType, possibleReplacements);
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

        for (Map.Entry<PositionType, Button> entry : mRightTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Set<Integer> possibleReplacements = mIndoorTeamService.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                    if (possibleReplacements.size() > 0) {
                        Log.i("VBR-Court", String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                        showPlayerSelectionDialog(mTeamOnRightSide, positionType, possibleReplacements);
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
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        super.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);

        onTeamRotated(mTeamOnLeftSide);
        onTeamRotated(mTeamOnRightSide);
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (PositionType.BENCH.equals(positionType)) {
            onTeamRotated(teamType);
        } else {
            final Map<PositionType, Button> teamPositions;

            if (mTeamOnLeftSide.equals(teamType)) {
                teamPositions = mLeftTeamPositions;
            } else {
                teamPositions = mRightTeamPositions;
            }

            Button button = teamPositions.get(positionType);
            button.setText(String.valueOf(number));
            UiUtils.styleIndoorTeamButton(mView.getContext(), mIndoorTeamService, teamType, number, button, true);

            checkCaptain(teamType, number);
        }

        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup();
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        final Map<PositionType, Button> teamPositions;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamPositions = mLeftTeamPositions;
        } else {
            teamPositions = mRightTeamPositions;
        }

        for (final Button button : teamPositions.values()) {
            button.setText("!");
            UiUtils.styleBaseTeamButton(mView.getContext(), mIndoorTeamService, teamType, button);
        }

        final Set<Integer> players = mTeamService.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mTeamService.getPlayerPosition(teamType, number);
            Button button = teamPositions.get(positionType);
            button.setText(String.valueOf(number));
            UiUtils.styleIndoorTeamButton(mView.getContext(), mIndoorTeamService, teamType, number, button, true);
        }

        confirmStartingLineup();
        checkCaptain(teamType, -1);
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
                    checkCaptain(TeamType.HOME, -1);
                    checkCaptain(TeamType.GUEST, -1);
                }

                @Override
                public void onNeutralButtonClicked() {}
            });
            alertDialogFragment.show(getActivity().getFragmentManager(), "confirm_lineup");
        }
    }

    private void showPlayerSelectionDialog(final TeamType teamType, final PositionType positionType, Set<Integer> possibleReplacements) {
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getResources().getString(R.string.select_player_title), mIndoorTeamService,
                teamType, possibleReplacements) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i("VBR-Court", String.format("Substitute %s team player at %s position by #%d player", teamType.toString(), positionType.toString(), selectedNumber));
                mIndoorTeamService.substitutePlayer(teamType, selectedNumber, positionType, ActionOriginType.USER);
            }
        };
        playerSelectionDialog.show();
    }

    private void checkCaptain(TeamType teamType, int number) {
        if (mIndoorTeamService.isStartingLineupConfirmed()) {
            if (mIndoorTeamService.isCaptain(teamType, number)) {
                // the captain is back on court, refresh the team
                onTeamRotated(teamType);
            } else if (!mIndoorTeamService.hasActingCaptainOnCourt(teamType)) {
                // there is no captain on court, request one
                showCaptainSelectionDialog(teamType);
            }
        }
    }

    private void showCaptainSelectionDialog(final TeamType teamType) {
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getResources().getString(R.string.select_captain), mIndoorTeamService,
                teamType, mIndoorTeamService.getPossibleActingCaptains(teamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i("VBR-Court", String.format("Change %s team acting captain by #%d player", teamType.toString(), selectedNumber));
                mIndoorTeamService.setActingCaptain(teamType, selectedNumber);
                // refresh the team with the new captain
                onTeamRotated(teamType);
            }
        };
        playerSelectionDialog.show();
    }
}
