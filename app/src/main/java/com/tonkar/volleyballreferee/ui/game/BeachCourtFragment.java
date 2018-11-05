package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.BeachTeamService;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.score.ScoreService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.Map;

public class BeachCourtFragment extends CourtFragment implements ScoreListener {

    private BeachTeamService mBeachTeamService;
    private ScoreService     mScoreService;
    private ImageView        mLeftServiceImage1;
    private ImageView        mLeftServiceImage2;
    private ImageView        mRightServiceImage1;
    private ImageView        mRightServiceImage2;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create beach court view");
        mView = inflater.inflate(R.layout.fragment_beach_court, container, false);

        initView();

        mBeachTeamService = (BeachTeamService) mTeamService;
        mScoreService = ServicesProvider.getInstance().getScoreService();
        mScoreService.addScoreListener(this);

        addButtonOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_position_2));

        addButtonOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_position_2));

        addSanctionImageOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_sanction_1));
        addSanctionImageOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_sanction_2));

        addSanctionImageOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_sanction_1));
        addSanctionImageOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_sanction_2));

        mLeftServiceImage1 = mView.findViewById(R.id.left_team_service_1);
        mLeftServiceImage2 = mView.findViewById(R.id.left_team_service_2);
        mRightServiceImage1 = mView.findViewById(R.id.right_team_service_1);
        mRightServiceImage2 = mView.findViewById(R.id.right_team_service_2);

        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

        for (Map.Entry<PositionType, MaterialButton> entry : mLeftTeamPositions.entrySet()) {
            entry.getValue().setOnClickListener(view -> {
                if (mScoreService.getServingTeam().equals(mTeamOnLeftSide)) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Swap %s team player", mTeamOnLeftSide.toString()));
                    mBeachTeamService.swapPlayers(mTeamOnLeftSide);
                }
            });
        }

        for (Map.Entry<PositionType, MaterialButton> entry : mRightTeamPositions.entrySet()) {
            entry.getValue().setOnClickListener(view -> {
                if (mScoreService.getServingTeam().equals(mTeamOnRightSide)) {
                    UiUtils.animate(getContext(), view);
                    Log.i(Tags.GAME_UI, String.format("Swap %s team player", mTeamOnRightSide.toString()));
                    mBeachTeamService.swapPlayers(mTeamOnRightSide);
                }
            });
        }

        return mView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScoreService.removeScoreListener(this);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        super.onTeamsSwapped(leftTeamType, rightTeamType, actionOriginType);
        updateAll();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType) {
        updateAll();
    }

    private void updateAll() {
        update(mTeamOnLeftSide);
        update(mTeamOnRightSide);
    }

    private void update(TeamType teamType) {
        final Map<PositionType, MaterialButton> teamPositions;
        final Map<PositionType, ImageView> teamSanctionImages;
        ImageView serviceImage1;
        ImageView serviceImage2;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamPositions = mLeftTeamPositions;
            serviceImage1 = mLeftServiceImage1;
            serviceImage2 = mLeftServiceImage2;
            teamSanctionImages = mLeftTeamSanctionImages;
        } else {
            teamPositions = mRightTeamPositions;
            serviceImage1 = mRightServiceImage1;
            serviceImage2 = mRightServiceImage2;
            teamSanctionImages = mRightTeamSanctionImages;
        }

        int number = 1;
        PositionType positionType = mTeamService.getPlayerPosition(teamType, number);
        MaterialButton button = teamPositions.get(PositionType.POSITION_1);
        button.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(mView.getContext(), mBeachTeamService, teamType, number, button);
        updateService(teamType, positionType, serviceImage1);
        updateSanction(teamType, number, teamSanctionImages.get(PositionType.POSITION_1));

        number = 2;
        positionType = mTeamService.getPlayerPosition(teamType, number);
        button = teamPositions.get(PositionType.POSITION_2);
        button.setText(UiUtils.formatNumberFromLocale(number));
        UiUtils.styleTeamButton(mView.getContext(), mBeachTeamService, teamType, number, button);
        updateService(teamType, positionType, serviceImage2);
        updateSanction(teamType, number, teamSanctionImages.get(PositionType.POSITION_2));
    }

    private void updateService(TeamType teamType, PositionType positionType, ImageView imageView) {
        if (mScoreService.getServingTeam().equals(teamType) && PositionType.POSITION_1.equals(positionType)) {
            imageView.setVisibility(View.VISIBLE);
        } else {
            imageView.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {
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
        if (SanctionType.RED_EXPULSION.equals(sanctionType)) {
            // The team is excluded for this set, the other team wins
            UiUtils.makeText(getActivity(), String.format(getResources().getString(R.string.set_lost_incomplete), mTeamService.getTeamName(teamType)), Toast.LENGTH_LONG).show();
        } else if (SanctionType.RED_DISQUALIFICATION.equals(sanctionType)) {
            // The team is excluded for this match, the other team wins
            UiUtils.makeText(getActivity(), String.format(getResources().getString(R.string.match_lost_incomplete), mTeamService.getTeamName(teamType)), Toast.LENGTH_LONG).show();
        }

        update(teamType);
    }
}
