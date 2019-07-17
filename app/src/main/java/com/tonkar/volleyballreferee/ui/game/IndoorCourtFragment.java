package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.ActionOriginType;
import com.tonkar.volleyballreferee.engine.game.IGame;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.team.IIndoorTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.team.IndoorPlayerSelectionDialog;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Map;
import java.util.Set;

public class IndoorCourtFragment extends CourtFragment {

    protected IIndoorTeam    mIndoorTeam;
    protected LayoutInflater mLayoutInflater;
    private   boolean        mOneStartingLineupDialog;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create indoor court view");
        mView = inflater.inflate(R.layout.fragment_indoor_court, container, false);

        mOneStartingLineupDialog = false;

        initView();

        if (mIndoorTeam != null) {
            mLayoutInflater = inflater;

            addButtonOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_position_1));
            addButtonOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_position_2));
            addButtonOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_position_3));
            addButtonOnLeftSide(PositionType.POSITION_4, mView.findViewById(R.id.left_team_position_4));
            addButtonOnLeftSide(PositionType.POSITION_5, mView.findViewById(R.id.left_team_position_5));
            addButtonOnLeftSide(PositionType.POSITION_6, mView.findViewById(R.id.left_team_position_6));

            addButtonOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_position_1));
            addButtonOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_position_2));
            addButtonOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_position_3));
            addButtonOnRightSide(PositionType.POSITION_4, mView.findViewById(R.id.right_team_position_4));
            addButtonOnRightSide(PositionType.POSITION_5, mView.findViewById(R.id.right_team_position_5));
            addButtonOnRightSide(PositionType.POSITION_6, mView.findViewById(R.id.right_team_position_6));

            addSanctionImageOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_sanction_1));
            addSanctionImageOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_sanction_2));
            addSanctionImageOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_sanction_3));
            addSanctionImageOnLeftSide(PositionType.POSITION_4, mView.findViewById(R.id.left_team_sanction_4));
            addSanctionImageOnLeftSide(PositionType.POSITION_5, mView.findViewById(R.id.left_team_sanction_5));
            addSanctionImageOnLeftSide(PositionType.POSITION_6, mView.findViewById(R.id.left_team_sanction_6));

            addSanctionImageOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_sanction_1));
            addSanctionImageOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_sanction_2));
            addSanctionImageOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_sanction_3));
            addSanctionImageOnRightSide(PositionType.POSITION_4, mView.findViewById(R.id.right_team_sanction_4));
            addSanctionImageOnRightSide(PositionType.POSITION_5, mView.findViewById(R.id.right_team_sanction_5));
            addSanctionImageOnRightSide(PositionType.POSITION_6, mView.findViewById(R.id.right_team_sanction_6));

            initLeftTeamListeners();
            initRightTeamListeners();

            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);
        }

        return mView;
    }

    protected void initLeftTeamListeners() {
        for (Map.Entry<PositionType, MaterialButton> entry : mLeftTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(view -> {
                final Set<Integer> possibleSubstitutions = mIndoorTeam.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                    showPlayerSelectionDialog(mTeamOnLeftSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mIndoorTeam.isStartingLineupConfirmed()) {
                    int number = mIndoorTeam.getPlayerAtPosition(mTeamOnLeftSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mIndoorTeam.substitutePlayer(mTeamOnLeftSide, number, PositionType.BENCH, ActionOriginType.USER);
                    }
                }
                return true;
            });
        }
    }

    protected void initRightTeamListeners() {
        for (Map.Entry<PositionType, MaterialButton> entry : mRightTeamPositions.entrySet()) {
            final PositionType positionType = entry.getKey();
            entry.getValue().setOnClickListener(view -> {
                final Set<Integer> possibleSubstitutions = mIndoorTeam.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                    showPlayerSelectionDialog(mTeamOnRightSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mIndoorTeam.isStartingLineupConfirmed()) {
                    int number = mIndoorTeam.getPlayerAtPosition(mTeamOnRightSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mIndoorTeam.substitutePlayer(mTeamOnRightSide, number, PositionType.BENCH, ActionOriginType.USER);
                    }
                }
                return true;
            });
        }
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        super.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);

        update(mTeamOnLeftSide);
        update(mTeamOnRightSide);
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (PositionType.BENCH.equals(positionType)) {
            update(teamType);
        } else {
            updatePosition(teamType, number, getTeamPositions(teamType).get(positionType));
            updateSanction(teamType, number, getTeamSanctionImages(teamType).get(positionType));

            checkCaptain(teamType, number);
        }

        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup();

            if (mIndoorTeam.isStartingLineupConfirmed() && !mIndoorTeam.hasRemainingSubstitutions(teamType) && !mIndoorTeam.isLibero(teamType, number)) {
                UiUtils.showNotification(getContext(), String.format(getString(R.string.all_substitutions_used), mIndoorTeam.getTeamName(teamType)));
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {
        rotateAnimation(teamType, clockwise);
    }

    protected void rotateAnimation(TeamType teamType, boolean clockwise) {
        View layoutPosition1 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_1) : mView.findViewById(R.id.right_team_layout_1);
        View layoutPosition2 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_2) : mView.findViewById(R.id.right_team_layout_2);
        View layoutPosition3 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_3) : mView.findViewById(R.id.right_team_layout_3);
        View layoutPosition4 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_4) : mView.findViewById(R.id.right_team_layout_4);
        View layoutPosition5 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_5) : mView.findViewById(R.id.right_team_layout_5);
        View layoutPosition6 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_6) : mView.findViewById(R.id.right_team_layout_6);

        if (clockwise) {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition6.getX()).y(layoutPosition6.getY()).setDuration(500L).start();
            layoutPosition6.animate().setStartDelay(0L).x(layoutPosition5.getX()).y(layoutPosition5.getY()).setDuration(500L).start();
            layoutPosition5.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition1.getX()).y(layoutPosition1.getY()).setDuration(500L).withEndAction(() ->
                getFragmentManager()
                        .beginTransaction()
                        .detach(IndoorCourtFragment.this)
                        .attach(IndoorCourtFragment.this)
                        .commit()
            ).start();
        } else {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition5.getX()).y(layoutPosition5.getY()).setDuration(500L).start();
            layoutPosition5.animate().setStartDelay(0L).x(layoutPosition6.getX()).y(layoutPosition6.getY()).setDuration(500L).start();
            layoutPosition6.animate().setStartDelay(0L).x(layoutPosition1.getX()).y(layoutPosition1.getY()).setDuration(500L).withEndAction(() ->
                getFragmentManager()
                        .beginTransaction()
                        .detach(IndoorCourtFragment.this)
                        .attach(IndoorCourtFragment.this)
                        .commit()
            ).start();
        }
    }

    private void update(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
        final Map<PositionType, ImageView> teamSanctionImages = getTeamSanctionImages(teamType);

        for (final Map.Entry<PositionType, MaterialButton> teamPosition : teamPositions.entrySet()) {
            teamPosition.getValue().setText(UiUtils.getPositionTitle(mView.getContext(), teamPosition.getKey()));
            UiUtils.styleBaseTeamButton(mView.getContext(), mIndoorTeam, teamType, teamPosition.getValue());
        }

        for (final ImageView imageView : teamSanctionImages.values()) {
            imageView.setVisibility(View.INVISIBLE);
        }

        final Set<Integer> players = mIndoorTeam.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mIndoorTeam.getPlayerPosition(teamType, number);
            updatePosition(teamType, number, teamPositions.get(positionType));
            updateSanction(teamType, number, teamSanctionImages.get(positionType));
        }

        confirmStartingLineup();
        checkCaptain(teamType, -1);
        checkExplusions(TeamType.HOME);
        checkExplusions(TeamType.GUEST);
    }

    private void confirmStartingLineup() {
        if (!mIndoorTeam.isStartingLineupConfirmed()
                && mIndoorTeam.getPlayersOnCourt(TeamType.HOME).size() == mIndoorTeam.getExpectedNumberOfPlayersOnCourt()
                && mIndoorTeam.getPlayersOnCourt(TeamType.GUEST).size() == mIndoorTeam.getExpectedNumberOfPlayersOnCourt()) {
            AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("confirm_lineup");
            if (alertDialogFragment == null) {
                if (!mOneStartingLineupDialog) {
                    alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.confirm_lineup_title), getString(R.string.confirm_lineup_question),
                            getString(android.R.string.no), getString(android.R.string.yes));
                    setStartingLineupDialogListener(alertDialogFragment);
                    alertDialogFragment.show(getActivity().getSupportFragmentManager(), "confirm_lineup");
                    mOneStartingLineupDialog = true;
                }
            } else {
                mOneStartingLineupDialog = true;
                setStartingLineupDialogListener(alertDialogFragment);
            }
        }
    }

    private void setStartingLineupDialogListener(final AlertDialogFragment alertDialogFragment) {
        alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
            @Override
            public void onNegativeButtonClicked() {
                mOneStartingLineupDialog = false;
            }

            @Override
            public void onPositiveButtonClicked() {
                mIndoorTeam.confirmStartingLineup();
                checkCaptain(TeamType.HOME, -1);
                checkCaptain(TeamType.GUEST, -1);
                mOneStartingLineupDialog = false;
            }

            @Override
            public void onNeutralButtonClicked() {}
        });
    }

    protected void showPlayerSelectionDialog(final TeamType teamType, final PositionType positionType, Set<Integer> possibleReplacements) {
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getString(R.string.select_player_title) + " (" + UiUtils.getPositionTitle(getActivity(), positionType) + ")",
                mIndoorTeam, mGame, teamType, possibleReplacements) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position by #%d player", teamType.toString(), positionType.toString(), selectedNumber));
                mIndoorTeam.substitutePlayer(teamType, selectedNumber, positionType, ActionOriginType.USER);
            }
        };
        playerSelectionDialog.show();
    }

    private void checkCaptain(TeamType teamType, int number) {
        if (mIndoorTeam.isStartingLineupConfirmed()) {
            if (mIndoorTeam.isCaptain(teamType, number)) {
                // the captain is back on court, refresh the team
                update(teamType);
            } else if (!mIndoorTeam.hasActingCaptainOnCourt(teamType)) {
                // there is no captain on court, request one
                showCaptainSelectionDialog(teamType);
            }
        }
    }

    private void showCaptainSelectionDialog(final TeamType teamType) {
        IndoorPlayerSelectionDialog playerSelectionDialog = new IndoorPlayerSelectionDialog(mLayoutInflater, mView.getContext(), getString(R.string.select_captain),
                mIndoorTeam, mGame, teamType, mIndoorTeam.getPossibleActingCaptains(teamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Change %s team acting captain by #%d player", teamType.toString(), selectedNumber));
                mIndoorTeam.setActingCaptain(teamType, selectedNumber);
                // refresh the team with the new captain
                update(teamType);
            }
        };
        playerSelectionDialog.show();
    }

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (number > 0) {
            PositionType positionType = mIndoorTeam.getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                updateSanction(teamType, number, getTeamSanctionImages(teamType).get(positionType));

                if (SanctionType.RED_EXPULSION.equals(sanctionType) || SanctionType.RED_DISQUALIFICATION.equals(sanctionType)) {
                    showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
                }
            }
        }
    }

    private void checkExplusions(TeamType teamType) {
        final Set<Integer> players = mIndoorTeam.getPlayersOnCourt(teamType);
        final Set<Integer> excludedNumbers = mGame.getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for (Integer number : players) {
            if (excludedNumbers.contains(number)) {
                final PositionType positionType = mIndoorTeam.getPlayerPosition(teamType, number);
                showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
            }
        }
    }

    private void showPlayerSelectionDialogAfterExpulsion(TeamType teamType, int number, PositionType positionType) {
        final Set<Integer> possibleSubstitutions = mIndoorTeam.getPossibleSubstitutions(teamType, positionType);
        final Set<Integer> filteredSubstitutions = mIndoorTeam.filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(teamType, number, possibleSubstitutions);

        if (filteredSubstitutions.size() > 0) {
            final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
            MaterialButton button = teamPositions.get(positionType);
            UiUtils.animate(getContext(), button);
            Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position after red card", teamType.toString(), positionType.toString()));
            showPlayerSelectionDialog(teamType, positionType, filteredSubstitutions);
        } else {
            UiUtils.makeText(getActivity(), String.format(getString(R.string.set_lost_incomplete), mIndoorTeam.getTeamName(teamType)), Toast.LENGTH_LONG).show();
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

    private Map<PositionType, ImageView> getTeamSanctionImages(TeamType teamType) {
        final Map<PositionType, ImageView> teamSanctionImages;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamSanctionImages = mLeftTeamSanctionImages;
        } else {
            teamSanctionImages = mRightTeamSanctionImages;
        }

        return teamSanctionImages;
    }

    protected void updatePosition(TeamType teamType, int number, MaterialButton positionButton) {
        positionButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleIndoorTeamButton(mView.getContext(), mIndoorTeam, teamType, number, positionButton);
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
        mIndoorTeam = (IIndoorTeam) game;
    }
}
