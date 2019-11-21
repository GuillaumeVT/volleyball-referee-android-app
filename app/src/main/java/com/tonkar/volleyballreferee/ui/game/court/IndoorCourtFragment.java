package com.tonkar.volleyballreferee.ui.game.court;

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
import com.tonkar.volleyballreferee.engine.stored.api.ApiSanction;
import com.tonkar.volleyballreferee.engine.team.IClassicTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.team.PlayerSelectionDialog;
import com.tonkar.volleyballreferee.ui.util.AlertDialogFragment;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IndoorCourtFragment extends CourtFragment {

    protected IClassicTeam   mClassTeam;
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

        if (mClassTeam != null) {
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
                final Set<Integer> possibleSubstitutions = mClassTeam.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnLeftSide.toString(), positionType.toString()));
                    showPlayerSelectionDialog(mTeamOnLeftSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mClassTeam.isStartingLineupConfirmed(mTeamOnLeftSide)) {
                    int number = mClassTeam.getPlayerAtPosition(mTeamOnLeftSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mClassTeam.substitutePlayer(mTeamOnLeftSide, number, PositionType.BENCH, ActionOriginType.USER);
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
                final Set<Integer> possibleSubstitutions = mClassTeam.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnRightSide.toString(), positionType.toString()));
                    showPlayerSelectionDialog(mTeamOnRightSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mClassTeam.isStartingLineupConfirmed(mTeamOnRightSide)) {
                    int number = mClassTeam.getPlayerAtPosition(mTeamOnRightSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mClassTeam.substitutePlayer(mTeamOnRightSide, number, PositionType.BENCH, ActionOriginType.USER);
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
            MaterialButton positionButton = getTeamPositions(teamType).get(positionType);
            updatePosition(teamType, number, positionButton);
            updateSanction(teamType, number, getTeamSanctionImages(teamType).get(positionType));
            UiUtils.animateBounce(mView.getContext(), positionButton);

            checkCaptain(teamType, number);
        }

        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup(teamType);

            if (mClassTeam.isStartingLineupConfirmed(teamType) && !mClassTeam.hasRemainingSubstitutions(teamType) && !mClassTeam.isLibero(teamType, number)) {
                UiUtils.showNotification(getContext(), String.format(getString(R.string.all_substitutions_used), mClassTeam.getTeamName(teamType)));
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {
        if (mClassTeam.isStartingLineupConfirmed(teamType)) {
            rotateAnimation(teamType, clockwise);
        } else {
            update(teamType);
        }
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
                    getFragmentManager().beginTransaction().detach(IndoorCourtFragment.this).attach(IndoorCourtFragment.this).commit()
            ).start();
        } else {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition5.getX()).y(layoutPosition5.getY()).setDuration(500L).start();
            layoutPosition5.animate().setStartDelay(0L).x(layoutPosition6.getX()).y(layoutPosition6.getY()).setDuration(500L).start();
            layoutPosition6.animate().setStartDelay(0L).x(layoutPosition1.getX()).y(layoutPosition1.getY()).setDuration(500L).withEndAction(() ->
                    getFragmentManager().beginTransaction().detach(IndoorCourtFragment.this).attach(IndoorCourtFragment.this).commit()
            ).start();
        }
    }

    protected void update(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
        final Map<PositionType, ImageView> teamSanctionImages = getTeamSanctionImages(teamType);

        for (final Map.Entry<PositionType, MaterialButton> teamPosition : teamPositions.entrySet()) {
            teamPosition.getValue().setText(UiUtils.getPositionTitle(mView.getContext(), teamPosition.getKey()));
            UiUtils.styleBaseTeamButton(mView.getContext(), mClassTeam, teamType, teamPosition.getValue());
        }

        for (final ImageView imageView : teamSanctionImages.values()) {
            imageView.setVisibility(View.INVISIBLE);
        }

        final Set<Integer> players = mClassTeam.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mClassTeam.getPlayerPosition(teamType, number);
            updatePosition(teamType, number, teamPositions.get(positionType));
            updateSanction(teamType, number, teamSanctionImages.get(positionType));
        }

        confirmStartingLineup(teamType);
        checkCaptain(teamType, -1);
        checkExplusions(TeamType.HOME);
        checkExplusions(TeamType.GUEST);
    }

    private void confirmStartingLineup(TeamType teamType) {
        if (!mClassTeam.isStartingLineupConfirmed(teamType) && mClassTeam.getPlayersOnCourt(teamType).size() == mClassTeam.getExpectedNumberOfPlayersOnCourt()) {
            AlertDialogFragment alertDialogFragment = (AlertDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("confirm_lineup");
            if (alertDialogFragment == null) {
                if (!mOneStartingLineupDialog) {
                    alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.confirm_lineup_title), String.format(Locale.getDefault(), getString(R.string.confirm_team_lineup_question), mClassTeam.getTeamName(teamType)),
                            getString(android.R.string.no), getString(android.R.string.yes));
                    setStartingLineupDialogListener(alertDialogFragment, teamType);
                    alertDialogFragment.show(getActivity().getSupportFragmentManager(), "confirm_lineup");
                    mOneStartingLineupDialog = true;
                }
            } else {
                mOneStartingLineupDialog = true;
                setStartingLineupDialogListener(alertDialogFragment, teamType);
            }
        }
    }

    private void setStartingLineupDialogListener(final AlertDialogFragment alertDialogFragment, TeamType teamType) {
        alertDialogFragment.setAlertDialogListener(new AlertDialogFragment.AlertDialogListener() {
            @Override
            public void onNegativeButtonClicked() {
                mOneStartingLineupDialog = false;
            }

            @Override
            public void onPositiveButtonClicked() {
                mClassTeam.confirmStartingLineup(teamType);
                checkCaptain(teamType, -1);
                mOneStartingLineupDialog = false;
            }

            @Override
            public void onNeutralButtonClicked() {}
        });
    }

    private void showPlayerSelectionDialog(final TeamType teamType, final PositionType positionType, Set<Integer> possibleReplacements) {
        PlayerSelectionDialog playerSelectionDialog = new PlayerSelectionDialog(mLayoutInflater, mView.getContext(), getString(R.string.select_player_title) + " (" + UiUtils.getPositionTitle(getActivity(), positionType) + ")",
                mClassTeam, mGame, teamType, possibleReplacements) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position by #%d player", teamType.toString(), positionType.toString(), selectedNumber));
                mClassTeam.substitutePlayer(teamType, selectedNumber, positionType, ActionOriginType.USER);
            }
        };
        playerSelectionDialog.show();
    }

    private void checkCaptain(TeamType teamType, int number) {
        if (mClassTeam.isStartingLineupConfirmed(teamType)) {
            if (mClassTeam.isCaptain(teamType, number)) {
                // the captain is back on court, refresh the team
                update(teamType);
            } else if (!mClassTeam.hasActingCaptainOnCourt(teamType)) {
                // there is no captain on court, request one
                showCaptainSelectionDialog(teamType);
            }
        }
    }

    private void showCaptainSelectionDialog(final TeamType teamType) {
        PlayerSelectionDialog playerSelectionDialog = new PlayerSelectionDialog(mLayoutInflater, mView.getContext(), getString(R.string.select_captain),
                mClassTeam, mGame, teamType, mClassTeam.getPossibleActingCaptains(teamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Change %s team acting captain by #%d player", teamType.toString(), selectedNumber));
                mClassTeam.setActingCaptain(teamType, selectedNumber);
                // refresh the team with the new captain
                update(teamType);
            }
        };
        playerSelectionDialog.show();
    }

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (ApiSanction.isPlayer(number)) {
            PositionType positionType = mClassTeam.getPlayerPosition(teamType, number);

            if (!PositionType.BENCH.equals(positionType)) {
                updateSanction(teamType, number, getTeamSanctionImages(teamType).get(positionType));

                if (sanctionType.isMisconductExpulsionCard() || sanctionType.isMisconductDisqualificationCard()) {
                    showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
                }
            }
        }
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {
        update(teamType);
    }

    private void checkExplusions(TeamType teamType) {
        final Set<Integer> players = mClassTeam.getPlayersOnCourt(teamType);
        final Set<Integer> excludedNumbers = mGame.getExpulsedOrDisqualifiedPlayersForCurrentSet(teamType);

        for (Integer number : players) {
            if (excludedNumbers.contains(number)) {
                final PositionType positionType = mClassTeam.getPlayerPosition(teamType, number);
                showPlayerSelectionDialogAfterExpulsion(teamType, number, positionType);
            }
        }
    }

    private void showPlayerSelectionDialogAfterExpulsion(TeamType teamType, int number, PositionType positionType) {
        final Set<Integer> possibleSubstitutions = mClassTeam.getPossibleSubstitutions(teamType, positionType);
        final Set<Integer> filteredSubstitutions = mClassTeam.filterSubstitutionsWithExpulsedOrDisqualifiedPlayersForCurrentSet(teamType, number, possibleSubstitutions);

        if (filteredSubstitutions.size() > 0) {
            final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
            MaterialButton button = teamPositions.get(positionType);
            UiUtils.animate(getContext(), button);
            Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position after red card", teamType.toString(), positionType.toString()));
            showPlayerSelectionDialog(teamType, positionType, filteredSubstitutions);
        } else {
            UiUtils.makeText(getActivity(), String.format(getString(R.string.set_lost_incomplete), mClassTeam.getTeamName(teamType)), Toast.LENGTH_LONG).show();
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

    private void updatePosition(TeamType teamType, int number, MaterialButton positionButton) {
        positionButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleIndoorTeamButton(mView.getContext(), mClassTeam, teamType, number, positionButton);
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
        mClassTeam = (IClassicTeam) game;
    }
}
