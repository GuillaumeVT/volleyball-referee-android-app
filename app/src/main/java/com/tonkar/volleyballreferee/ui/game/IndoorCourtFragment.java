package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.team.IndoorPlayerSelectionDialog;

import java.util.Map;
import java.util.Set;

public class IndoorCourtFragment extends CourtFragment {

    protected IndoorTeamService mIndoorTeamService;
    protected LayoutInflater    mLayoutInflater;

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
        Log.i(Tags.GAME_UI, "Create indoor court view");
        mView = inflater.inflate(R.layout.fragment_indoor_court, container, false);

        initView();

        mIndoorTeamService = (IndoorTeamService) mTeamService;
        mLayoutInflater = inflater;

        addButtonOnLeftSide(PositionType.POSITION_1, (MaterialButton) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (MaterialButton) mView.findViewById(R.id.left_team_position_2));
        addButtonOnLeftSide(PositionType.POSITION_3, (MaterialButton) mView.findViewById(R.id.left_team_position_3));
        addButtonOnLeftSide(PositionType.POSITION_4, (MaterialButton) mView.findViewById(R.id.left_team_position_4));
        addButtonOnLeftSide(PositionType.POSITION_5, (MaterialButton) mView.findViewById(R.id.left_team_position_5));
        addButtonOnLeftSide(PositionType.POSITION_6, (MaterialButton) mView.findViewById(R.id.left_team_position_6));

        addButtonOnRightSide(PositionType.POSITION_1, (MaterialButton) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (MaterialButton) mView.findViewById(R.id.right_team_position_2));
        addButtonOnRightSide(PositionType.POSITION_3, (MaterialButton) mView.findViewById(R.id.right_team_position_3));
        addButtonOnRightSide(PositionType.POSITION_4, (MaterialButton) mView.findViewById(R.id.right_team_position_4));
        addButtonOnRightSide(PositionType.POSITION_5, (MaterialButton) mView.findViewById(R.id.right_team_position_5));
        addButtonOnRightSide(PositionType.POSITION_6, (MaterialButton) mView.findViewById(R.id.right_team_position_6));

        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

        initLeftTeamListeners();
        initRightTeamListeners();

        if (savedInstanceState != null) {
            restoreStartingLineupDialog();
        }

        return mView;
    }

    protected void initLeftTeamListeners() {
        for (Map.Entry<PositionType, MaterialButton> entry : mLeftTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Set<Integer> possibleSubstitutions = mIndoorTeamService.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                    if (possibleSubstitutions.size() > 0) {
                        UiUtils.animate(getContext(), view);
                        Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                        showPlayerSelectionDialog(mTeamOnLeftSide, positionType, possibleSubstitutions);
                    } else {
                        UiUtils.makeText(getContext(), getResources().getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                    }
                }
            });

            entry.getValue().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!mIndoorTeamService.isStartingLineupConfirmed()) {
                        int number = mIndoorTeamService.getPlayerAtPosition(mTeamOnLeftSide, positionType);
                        if (number > 0) {
                            UiUtils.animateBounce(getContext(), view);
                            mIndoorTeamService.substitutePlayer(mTeamOnLeftSide, number, PositionType.BENCH, ActionOriginType.USER);
                        }
                    }
                    return true;
                }
            });
        }
    }

    protected void initRightTeamListeners() {
        for (Map.Entry<PositionType, MaterialButton> entry : mRightTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    final Set<Integer> possibleSubstitutions = mIndoorTeamService.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                    if (possibleSubstitutions.size() > 0) {
                        UiUtils.animate(getContext(), view);
                        Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                        showPlayerSelectionDialog(mTeamOnRightSide, positionType, possibleSubstitutions);
                    } else {
                        UiUtils.makeText(getContext(), getResources().getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                    }
                }
            });

            entry.getValue().setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    if (!mIndoorTeamService.isStartingLineupConfirmed()) {
                        int number = mIndoorTeamService.getPlayerAtPosition(mTeamOnRightSide, positionType);
                        if (number > 0) {
                            UiUtils.animateBounce(getContext(), view);
                            mIndoorTeamService.substitutePlayer(mTeamOnRightSide, number, PositionType.BENCH, ActionOriginType.USER);
                        }
                    }
                    return true;
                }
            });
        }
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
            final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
            MaterialButton button = teamPositions.get(positionType);
            button.setText(UiUtils.formatNumberFromLocal(number));
            UiUtils.styleIndoorTeamButton(mView.getContext(), mIndoorTeamService, teamType, number, button);

            checkCaptain(teamType, number);
        }

        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup();

            if (!mIndoorTeamService.hasRemainingSubstitutions(teamType) && !mIndoorTeamService.isLibero(teamType, number)) {
                UiUtils.showNotification(getContext(), String.format(getResources().getString(R.string.all_substitutions_used), mIndoorTeamService.getTeamName(teamType)));
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);

        for (final MaterialButton button : teamPositions.values()) {
            button.setText("!");
            UiUtils.styleBaseTeamButton(mView.getContext(), mIndoorTeamService, teamType, button);
        }

        final Set<Integer> players = mTeamService.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mTeamService.getPlayerPosition(teamType, number);
            final MaterialButton button = teamPositions.get(positionType);
            button.setText(UiUtils.formatNumberFromLocal(number));
            UiUtils.styleIndoorTeamButton(mView.getContext(), mIndoorTeamService, teamType, number, button);
        }

        confirmStartingLineup();
        checkCaptain(teamType, -1);
        checkExplusions(TeamType.HOME);
        checkExplusions(TeamType.GUEST);
    }

    private void confirmStartingLineup() {
        if (!mIndoorTeamService.isStartingLineupConfirmed()
                && mIndoorTeamService.getPlayersOnCourt(TeamType.HOME).size() == mIndoorTeamService.getExpectedNumberOfPlayersOnCourt()
                && mIndoorTeamService.getPlayersOnCourt(TeamType.GUEST).size() == mIndoorTeamService.getExpectedNumberOfPlayersOnCourt()) {
            AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("confirm_lineup");
            if (alertDialogFragment == null) {
                alertDialogFragment = AlertDialogFragment.newInstance(getResources().getString(R.string.confirm_lineup_title), getResources().getString(R.string.confirm_lineup_question),
                        getResources().getString(android.R.string.no), getResources().getString(android.R.string.yes));
                alertDialogFragment.show(getActivity().getSupportFragmentManager(), "confirm_lineup");
            }
            setStartingLineupDialogListener(alertDialogFragment);
        }
    }

    protected void restoreStartingLineupDialog() {
        AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("confirm_lineup");
        setStartingLineupDialogListener(alertDialogFragment);
    }

    private void setStartingLineupDialogListener(final AlertDialogFragment alertDialogFragment) {
        if (alertDialogFragment != null) {
            alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
                @Override
                public void onNegativeButtonClicked() {
                }

                @Override
                public void onPositiveButtonClicked() {
                    mIndoorTeamService.confirmStartingLineup();
                    checkCaptain(TeamType.HOME, -1);
                    checkCaptain(TeamType.GUEST, -1);
                }

                @Override
                public void onNeutralButtonClicked() {
                }
            });
        }
    }

    protected void showPlayerSelectionDialog(final TeamType teamType, final PositionType positionType, Set<Integer> possibleReplacements) {
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getResources().getString(R.string.select_player_title) + " (" + UiUtils.getPositionTitle(getActivity(), positionType) + ")",
                mIndoorTeamService, mSanctionService, teamType, possibleReplacements) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position by #%d player", teamType.toString(), positionType.toString(), selectedNumber));
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
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getResources().getString(R.string.select_captain),
                mIndoorTeamService, mSanctionService, teamType, mIndoorTeamService.getPossibleActingCaptains(teamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Change %s team acting captain by #%d player", teamType.toString(), selectedNumber));
                mIndoorTeamService.setActingCaptain(teamType, selectedNumber);
                // refresh the team with the new captain
                onTeamRotated(teamType);
            }
        };
        playerSelectionDialog.show();
    }

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (number > 0 && (SanctionType.RED_EXPULSION.equals(sanctionType) || SanctionType.RED_DISQUALIFICATION.equals(sanctionType))) {
            PositionType positionType = mIndoorTeamService.getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
            }
        }
    }

    private void checkExplusions(TeamType teamType) {
        final Set<Integer> players = mTeamService.getPlayersOnCourt(teamType);
        final Set<Integer> excludedNumbers = mSanctionService.getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for (Integer number : players) {
            if (excludedNumbers.contains(number)) {
                final PositionType positionType = mTeamService.getPlayerPosition(teamType, number);
                showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
            }
        }
    }

    private void showPlayerSelectionDialogAfterExpulsion(TeamType teamType, int number, PositionType positionType) {
        final Set<Integer> possibleSubstitutions = mIndoorTeamService.getPossibleSubstitutions(teamType, positionType);
        final Set<Integer> filteredSubstitutions = mIndoorTeamService.filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(teamType, number, possibleSubstitutions);

        if (filteredSubstitutions.size() > 0) {
            final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
            MaterialButton button = teamPositions.get(positionType);
            UiUtils.animate(getContext(), button);
            Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position after red card", teamType.toString(), positionType.toString()));
            showPlayerSelectionDialog(teamType, positionType, filteredSubstitutions);
        } else {
            UiUtils.makeText(getActivity(), String.format(getResources().getString(R.string.set_lost_incomplete), mTeamService.getTeamName(teamType)), Toast.LENGTH_LONG).show();
        }
    }

    private Map<PositionType, MaterialButton> getTeamPositions(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamPositions = mLeftTeamPositions;
        } else {
            teamPositions = mRightTeamPositions;
        }

        return teamPositions;
    }

}
