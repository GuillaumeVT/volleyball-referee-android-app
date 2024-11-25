package com.tonkar.volleyballreferee.ui.game.court;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.api.model.SanctionDto;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Map;

public class BeachCourtFragment extends CourtFragment implements ScoreListener {

    private IBeachTeam     mBeachTeam;
    private MaterialButton mLeftServiceButton1;
    private MaterialButton mLeftServiceButton2;
    private MaterialButton mRightServiceButton1;
    private MaterialButton mRightServiceButton2;

    public BeachCourtFragment() {
        super();
    }

    public static BeachCourtFragment newInstance() {
        BeachCourtFragment fragment = new BeachCourtFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create beach court view");
        mView = inflater.inflate(R.layout.fragment_beach_court, container, false);

        initView();

        if (mBeachTeam != null && mGame != null) {
            mGame.addScoreListener(this);

            addButtonOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_position_1));
            addButtonOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_position_2));

            addButtonOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_position_1));
            addButtonOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_position_2));

            addSanctionImageOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_sanction_1));
            addSanctionImageOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_sanction_2));

            addSanctionImageOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_sanction_1));
            addSanctionImageOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_sanction_2));

            mLeftServiceButton1 = mView.findViewById(R.id.left_team_service_1);
            mLeftServiceButton2 = mView.findViewById(R.id.left_team_service_2);
            mRightServiceButton1 = mView.findViewById(R.id.right_team_service_1);
            mRightServiceButton2 = mView.findViewById(R.id.right_team_service_2);

            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

            mLeftServiceButton1.setOnClickListener(view -> swapService(mTeamOnLeftSide, view));
            mLeftServiceButton2.setOnClickListener(view -> swapService(mTeamOnLeftSide, view));
            mRightServiceButton1.setOnClickListener(view -> swapService(mTeamOnRightSide, view));
            mRightServiceButton2.setOnClickListener(view -> swapService(mTeamOnRightSide, view));
        }

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGame != null) {
            mGame.removeScoreListener(this);
        }
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        super.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        updateAll();

        if (mGame.areNotificationsEnabled() && ActionOriginType.APPLICATION.equals(actionOriginType)) {
            UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content), getString(R.string.switch_sides));
            UiUtils.playNotificationSound(getContext());
        }
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {
        updateAll();
    }

    private void updateAll() {
        update(mTeamOnLeftSide);
        update(mTeamOnRightSide);
    }

    private void update(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions;
        final Map<PositionType, ImageView> teamSanctionImages;
        MaterialButton serviceButton1;
        MaterialButton serviceButton2;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamPositions = mLeftTeamPositions;
            serviceButton1 = mLeftServiceButton1;
            serviceButton2 = mLeftServiceButton2;
            teamSanctionImages = mLeftTeamSanctionImages;
        } else {
            teamPositions = mRightTeamPositions;
            serviceButton1 = mRightServiceButton1;
            serviceButton2 = mRightServiceButton2;
            teamSanctionImages = mRightTeamSanctionImages;
        }

        int number = 1;
        PositionType positionType = mBeachTeam.getPlayerPosition(teamType, number);
        MaterialButton positionButton = teamPositions.get(PositionType.POSITION_1);
        positionButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(mView.getContext(), mBeachTeam, teamType, number, positionButton);
        updateService(teamType, positionType, serviceButton1);
        updateSanction(teamType, number, teamSanctionImages.get(PositionType.POSITION_1));

        number = 2;
        positionType = mBeachTeam.getPlayerPosition(teamType, number);
        positionButton = teamPositions.get(PositionType.POSITION_2);
        positionButton.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(mView.getContext(), mBeachTeam, teamType, number, positionButton);
        updateService(teamType, positionType, serviceButton2);
        updateSanction(teamType, number, teamSanctionImages.get(PositionType.POSITION_2));
    }

    private void updateService(TeamType teamType, PositionType positionType, MaterialButton imageButton) {
        if (mGame.getServingTeam().equals(teamType) && PositionType.POSITION_1.equals(positionType)) {
            imageButton.setVisibility(View.VISIBLE);
        } else {
            imageButton.setVisibility(View.INVISIBLE);
        }
    }

    private void swapService(TeamType teamType, View view) {
        if (mGame.getServingTeam().equals(teamType)) {
            Log.i(Tags.GAME_UI, String.format("Swap %s team player", teamType));
            mBeachTeam.swapPlayers(teamType);
        }
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType, boolean isStart) {
        updateAll();
    }

    @Override
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(TeamType winner) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (SanctionDto.isPlayer(number) && sanctionType.isMisconductExpulsionCard()) {
            // The team is excluded for this set, the other team wins
            UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content),
                                     String.format(getString(R.string.set_lost_incomplete), mBeachTeam.getTeamName(teamType)));
        } else if (SanctionDto.isPlayer(number) && sanctionType.isMisconductDisqualificationCard()) {
            // The team is excluded for this match, the other team wins
            UiUtils.showNotification(requireActivity().findViewById(R.id.activity_game_content),
                                     String.format(getString(R.string.match_lost_incomplete), mBeachTeam.getTeamName(teamType)));
        }

        update(teamType);
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {
        update(teamType);
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
        mBeachTeam = (IBeachTeam) game;
    }
}
