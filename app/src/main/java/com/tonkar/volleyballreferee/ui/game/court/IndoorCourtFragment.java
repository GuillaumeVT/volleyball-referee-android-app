package com.tonkar.volleyballreferee.ui.game.court;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.team.PlayerSelectionDialog;
import com.tonkar.volleyballreferee.ui.util.*;

import java.util.*;

public class IndoorCourtFragment extends CourtFragment {

    protected IClassicTeam   mClassicTeam;
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

        if (mClassicTeam != null) {
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

            addSubstitutionOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_substitution_1));
            addSubstitutionOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_substitution_2));
            addSubstitutionOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_substitution_3));
            addSubstitutionOnLeftSide(PositionType.POSITION_4, mView.findViewById(R.id.left_team_substitution_4));
            addSubstitutionOnLeftSide(PositionType.POSITION_5, mView.findViewById(R.id.left_team_substitution_5));
            addSubstitutionOnLeftSide(PositionType.POSITION_6, mView.findViewById(R.id.left_team_substitution_6));

            addSubstitutionOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_substitution_1));
            addSubstitutionOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_substitution_2));
            addSubstitutionOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_substitution_3));
            addSubstitutionOnRightSide(PositionType.POSITION_4, mView.findViewById(R.id.right_team_substitution_4));
            addSubstitutionOnRightSide(PositionType.POSITION_5, mView.findViewById(R.id.right_team_substitution_5));
            addSubstitutionOnRightSide(PositionType.POSITION_6, mView.findViewById(R.id.right_team_substitution_6));

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
                final Set<Integer> possibleSubstitutions = mClassicTeam.getPossibleSubstitutions(mTeamOnLeftSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnLeftSide, positionType));
                    showPlayerSelectionDialog(mTeamOnLeftSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mClassicTeam.isStartingLineupConfirmed(mTeamOnLeftSide)) {
                    int number = mClassicTeam.getPlayerAtPosition(mTeamOnLeftSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mClassicTeam.substitutePlayer(mTeamOnLeftSide, number, PositionType.BENCH, ActionOriginType.USER);
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
                final Set<Integer> possibleSubstitutions = mClassicTeam.getPossibleSubstitutions(mTeamOnRightSide, positionType);
                if (possibleSubstitutions.size() > 0) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position", mTeamOnRightSide, positionType));
                    showPlayerSelectionDialog(mTeamOnRightSide, positionType, possibleSubstitutions);
                } else {
                    UiUtils.makeText(getContext(), getString(R.string.no_substitution_message), Toast.LENGTH_LONG).show();
                }
            });

            entry.getValue().setOnLongClickListener(view -> {
                if (!mClassicTeam.isStartingLineupConfirmed(mTeamOnRightSide)) {
                    int number = mClassicTeam.getPlayerAtPosition(mTeamOnRightSide, positionType);
                    if (number > 0) {
                        UiUtils.animateBounce(getContext(), view);
                        mClassicTeam.substitutePlayer(mTeamOnRightSide, number, PositionType.BENCH, ActionOriginType.USER);
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

        if (mGame.areNotificationsEnabled() && ActionOriginType.APPLICATION.equals(actionOriginType)) {
            UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content), getString(R.string.switch_sides));
            UiUtils.playNotificationSound(getContext());
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        update(teamType);

        if (!PositionType.BENCH.equals(positionType)) {
            MaterialButton positionButton = getTeamPositions(teamType).get(positionType);
            UiUtils.animateBounce(mView.getContext(), positionButton);
        }

        if (ActionOriginType.USER.equals(actionOriginType)) {
            confirmStartingLineup(teamType);

            if (mClassicTeam.isStartingLineupConfirmed(teamType) && !mClassicTeam.isLibero(teamType, number)) {
                int remainingSubstitutions = mClassicTeam.countRemainingSubstitutions(teamType);

                if (remainingSubstitutions == 0) {
                    UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content),
                                             String.format(getString(R.string.all_substitutions_used), mClassicTeam.getTeamName(teamType)));
                } else if (remainingSubstitutions == 1) {
                    UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content),
                                             String.format(getString(R.string.one_remaining_substitution),
                                                           mClassicTeam.getTeamName(teamType)));
                }
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {
        if (mClassicTeam.isStartingLineupConfirmed(teamType)) {
            rotateAnimation(teamType, clockwise);
        } else {
            update(teamType);
        }
    }

    protected void rotateAnimation(TeamType teamType, boolean clockwise) {
        View layoutPosition1 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_1) : mView.findViewById(
                R.id.right_team_layout_1);
        View layoutPosition2 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_2) : mView.findViewById(
                R.id.right_team_layout_2);
        View layoutPosition3 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_3) : mView.findViewById(
                R.id.right_team_layout_3);
        View layoutPosition4 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_4) : mView.findViewById(
                R.id.right_team_layout_4);
        View layoutPosition5 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_5) : mView.findViewById(
                R.id.right_team_layout_5);
        View layoutPosition6 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_6) : mView.findViewById(
                R.id.right_team_layout_6);

        if (clockwise) {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition6.getX()).y(layoutPosition6.getY()).setDuration(500L).start();
            layoutPosition6.animate().setStartDelay(0L).x(layoutPosition5.getX()).y(layoutPosition5.getY()).setDuration(500L).start();
            layoutPosition5.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2
                    .animate()
                    .setStartDelay(0L)
                    .x(layoutPosition1.getX())
                    .y(layoutPosition1.getY())
                    .setDuration(500L)
                    .withEndAction(this::detachThenAttach)
                    .start();
        } else {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition5.getX()).y(layoutPosition5.getY()).setDuration(500L).start();
            layoutPosition5.animate().setStartDelay(0L).x(layoutPosition6.getX()).y(layoutPosition6.getY()).setDuration(500L).start();
            layoutPosition6
                    .animate()
                    .setStartDelay(0L)
                    .x(layoutPosition1.getX())
                    .y(layoutPosition1.getY())
                    .setDuration(500L)
                    .withEndAction(this::detachThenAttach)
                    .start();
        }
    }

    private void detachThenAttach() {
        if (this.isAdded()) {
            getParentFragmentManager().beginTransaction().detach(this).commit();
            getParentFragmentManager().beginTransaction().attach(this).commit();
        }
    }

    protected void update(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
        final Map<PositionType, ImageView> teamSanctionImages = getTeamSanctionImages(teamType);
        final Map<PositionType, TextView> teamSubstitutions = getTeamSubstitutions(teamType);

        for (final Map.Entry<PositionType, MaterialButton> teamPosition : teamPositions.entrySet()) {
            teamPosition.getValue().setText(UiUtils.getPositionTitle(mView.getContext(), teamPosition.getKey()));
            UiUtils.styleBaseTeamButton(mView.getContext(), mClassicTeam, teamType, teamPosition.getValue());
        }

        for (final ImageView imageView : teamSanctionImages.values()) {
            imageView.setVisibility(View.INVISIBLE);
        }

        for (final View view : teamSubstitutions.values()) {
            view.setVisibility(View.INVISIBLE);
        }

        final Set<Integer> players = mClassicTeam.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mClassicTeam.getPlayerPosition(teamType, number);
            MaterialButton positionButton = getTeamPositions(teamType).get(positionType);
            if (positionButton != null) {
                updatePosition(teamType, number, positionButton);
                if (mClassicTeam.isStartingLineupConfirmed(teamType)) {
                    updateSanction(teamType, number, teamSanctionImages.get(positionType));
                    updateSubstitution(teamType, number, teamSubstitutions.get(positionType));
                }
            }
        }

        updateService();
        confirmStartingLineup(teamType);
        checkCaptain(teamType);
        checkEvictions(teamType);
    }

    private void confirmStartingLineup(TeamType teamType) {
        if (!mClassicTeam.isStartingLineupConfirmed(teamType) && mClassicTeam
                .getPlayersOnCourt(teamType)
                .size() == mClassicTeam.getExpectedNumberOfPlayersOnCourt()) {
            AlertDialogFragment alertDialogFragment = (AlertDialogFragment) requireActivity()
                    .getSupportFragmentManager()
                    .findFragmentByTag("confirm_lineup");
            if (alertDialogFragment == null) {
                if (!mOneStartingLineupDialog) {
                    alertDialogFragment = AlertDialogFragment.newInstance(getString(R.string.confirm_lineup_title),
                                                                          String.format(Locale.getDefault(),
                                                                                        getString(R.string.confirm_team_lineup_question),
                                                                                        mClassicTeam.getTeamName(teamType)),
                                                                          getString(android.R.string.no), getString(android.R.string.yes));
                    setStartingLineupDialogListener(alertDialogFragment, teamType);
                    alertDialogFragment.show(requireActivity().getSupportFragmentManager(), "confirm_lineup");
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
                mClassicTeam.confirmStartingLineup(teamType);
                checkCaptain(teamType);
                mOneStartingLineupDialog = false;
            }

            @Override
            public void onNeutralButtonClicked() {}
        });
    }

    private void showPlayerSelectionDialog(final TeamType teamType, final PositionType positionType, Set<Integer> possibleReplacements) {
        PlayerSelectionDialog playerSelectionDialog = new PlayerSelectionDialog(mLayoutInflater, mView.getContext(), getString(
                R.string.select_player_title) + " (" + UiUtils.getPositionTitle(requireActivity(), positionType) + ")", mClassicTeam, mGame,
                                                                                teamType, possibleReplacements) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI,
                      String.format("Substitute %s team player at %s position by #%d player", teamType, positionType, selectedNumber));
                mClassicTeam.substitutePlayer(teamType, selectedNumber, positionType, ActionOriginType.USER);
            }
        };
        playerSelectionDialog.show();
    }

    private void checkCaptain(TeamType teamType) {
        if (mClassicTeam.isStartingLineupConfirmed(teamType) && !mClassicTeam.hasGameCaptainOnCourt(teamType)) {
            // there is no captain on court, request one
            showCaptainSelectionDialog(teamType);
        }
    }

    private void showCaptainSelectionDialog(final TeamType teamType) {
        PlayerSelectionDialog playerSelectionDialog = new PlayerSelectionDialog(mLayoutInflater, mView.getContext(),
                                                                                getString(R.string.select_captain), mClassicTeam, mGame,
                                                                                teamType,
                                                                                mClassicTeam.getPossibleSecondaryCaptains(teamType)) {
            @Override
            public void onPlayerSelected(int selectedNumber) {
                Log.i(Tags.GAME_UI, String.format("Change %s team acting captain by #%d player", teamType, selectedNumber));
                mClassicTeam.setGameCaptain(teamType, selectedNumber);
                // refresh the team with the new captain
                update(teamType);
            }
        };
        playerSelectionDialog.show();
    }

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (SanctionDto.isPlayer(number)) {
            PositionType positionType = mClassicTeam.getPlayerPosition(teamType, number);

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

    private void checkEvictions(TeamType teamType) {
        if (mClassicTeam.isStartingLineupConfirmed(teamType)) {
            final Set<Integer> players = mClassicTeam.getPlayersOnCourt(teamType);
            final Set<Integer> evictedPlayers = mGame.getEvictedPlayersForCurrentSet(teamType, true, true);

            for (Integer player : players) {
                if (evictedPlayers.contains(player)) {
                    final PositionType positionType = mClassicTeam.getPlayerPosition(teamType, player);
                    showPlayerSelectionDialogAfterExpulsion(teamType, player, positionType);
                }
            }
        }
    }

    private void showPlayerSelectionDialogAfterExpulsion(TeamType teamType, int number, PositionType positionType) {
        final Set<Integer> possibleSubstitutions = mClassicTeam.getPossibleSubstitutions(teamType, positionType);
        final Set<Integer> filteredSubstitutions = mClassicTeam.filterSubstitutionsWithEvictedPlayersForCurrentSet(teamType, number,
                                                                                                                   possibleSubstitutions);

        if (filteredSubstitutions.size() > 0) {
            final Map<PositionType, MaterialButton> teamPositions = getTeamPositions(teamType);
            MaterialButton button = teamPositions.get(positionType);
            UiUtils.animate(getContext(), button);
            Log.i(Tags.GAME_UI, String.format("Substitute %s team player at %s position after red card", teamType, positionType));
            showPlayerSelectionDialog(teamType, positionType, filteredSubstitutions);
        } else {
            UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content),
                                     String.format(getString(R.string.set_lost_incomplete), mClassicTeam.getTeamName(teamType)));
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

    private Map<PositionType, TextView> getTeamSubstitutions(TeamType teamType) {
        final Map<PositionType, TextView> teamSubstitutions;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamSubstitutions = mLeftTeamSubstitutions;
        } else {
            teamSubstitutions = mRightTeamSubstitutions;
        }

        return teamSubstitutions;
    }

    private void updatePosition(TeamType teamType, int number, MaterialButton positionButton) {
        if (number >= 0) {
            positionButton.setText(UiUtils.formatNumberFromLocale(number));
        }
        UiUtils.styleIndoorTeamButton(mView.getContext(), mClassicTeam, teamType, number, positionButton);
    }

    protected void updateService() {
        updateService(mTeamOnLeftSide);
        updateService(mTeamOnRightSide);
    }

    private void updateService(TeamType teamType) {
        MaterialButton positionButton = getTeamPositions(teamType).get(PositionType.POSITION_1);
        if (mGame.getServingTeam().equals(teamType) && positionButton != null) {
            positionButton.setIconResource(R.drawable.ic_service);
            UiUtils.styleIndoorTeamButton(mView.getContext(), mClassicTeam, teamType,
                                          mClassicTeam.getPlayerAtPosition(teamType, PositionType.POSITION_1), positionButton);
        } else {
            positionButton.setIcon(null);
        }
    }

    private void updateSubstitution(TeamType teamType, int number, TextView substitutionView) {
        substitutionView.setVisibility(View.GONE);

        if (Rules.FIVB_LIMITATION == mGame.getRules().getSubstitutionsLimitation() && mClassicTeam.isStartingLineupConfirmed(teamType)) {
            boolean isLibero = mClassicTeam.isLibero(teamType, number);

            if (mClassicTeam.hasRemainingSubstitutions(teamType) || isLibero) {
                if (isLibero) {
                    int middleBlockerNumber = mClassicTeam.getWaitingMiddleBlocker(teamType);
                    substitutionView.setText(UiUtils.formatNumberFromLocale(middleBlockerNumber));
                    UiUtils.styleTeamText(getContext(), mClassicTeam, teamType, middleBlockerNumber, substitutionView);
                    substitutionView.setVisibility(View.VISIBLE);
                } else {
                    for (SubstitutionDto substitution : mClassicTeam.getSubstitutions(teamType)) {
                        if (number == substitution.getPlayerIn()) {
                            substitutionView.setText(UiUtils.formatNumberFromLocale(substitution.getPlayerOut()));
                            UiUtils.styleTeamText(getContext(), mClassicTeam, teamType, substitution.getPlayerOut(), substitutionView);
                            substitutionView.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
        mClassicTeam = (IClassicTeam) game;
    }

    @Override
    public void onServiceSwapped(TeamType teamType, boolean isStart) {
        if (isStart) {
            updateService();

            updatePosition(teamType, mClassicTeam.getPlayerAtPosition(teamType, PositionType.POSITION_1),
                           getTeamPositions(teamType).get(PositionType.POSITION_1));

            TeamType otherTeamType = teamType.other();
            updatePosition(otherTeamType, mClassicTeam.getPlayerAtPosition(otherTeamType, PositionType.POSITION_1),
                           getTeamPositions(otherTeamType).get(PositionType.POSITION_1));
        }
    }
}
