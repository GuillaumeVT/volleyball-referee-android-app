package com.tonkar.volleyballreferee.ui.game.court;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ImageView;

import androidx.annotation.NonNull;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

public class SnowCourtFragment extends IndoorCourtFragment {

    private ImageView mLeftServiceImage1;
    private ImageView mLeftServiceImage2;
    private ImageView mLeftServiceImage3;
    private ImageView mRightServiceImage1;
    private ImageView mRightServiceImage2;
    private ImageView mRightServiceImage3;

    public SnowCourtFragment() {
        super();
    }

    public static SnowCourtFragment newInstance() {
        SnowCourtFragment fragment = new SnowCourtFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create snow court view");
        mView = inflater.inflate(R.layout.fragment_snow_court, container, false);

        initView();

        if (mClassicTeam != null && mGame != null) {
            mGame.addScoreListener(this);
            mLayoutInflater = inflater;

            addButtonOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_position_1));
            addButtonOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_position_2));
            addButtonOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_position_3));

            addButtonOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_position_1));
            addButtonOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_position_2));
            addButtonOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_position_3));

            addSanctionImageOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_sanction_1));
            addSanctionImageOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_sanction_2));
            addSanctionImageOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_sanction_3));

            addSanctionImageOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_sanction_1));
            addSanctionImageOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_sanction_2));
            addSanctionImageOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_sanction_3));

            addSubstitutionOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_substitution_1));
            addSubstitutionOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_substitution_2));
            addSubstitutionOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_substitution_3));

            addSubstitutionOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_substitution_1));
            addSubstitutionOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_substitution_2));
            addSubstitutionOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_substitution_3));

            initLeftTeamListeners();
            initRightTeamListeners();

            mLeftServiceImage1 = mView.findViewById(R.id.left_team_service_1);
            mLeftServiceImage2 = mView.findViewById(R.id.left_team_service_2);
            mLeftServiceImage3 = mView.findViewById(R.id.left_team_service_3);
            mRightServiceImage1 = mView.findViewById(R.id.right_team_service_1);
            mRightServiceImage2 = mView.findViewById(R.id.right_team_service_2);
            mRightServiceImage3 = mView.findViewById(R.id.right_team_service_3);

            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);
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
    protected void rotateAnimation(TeamType teamType, boolean clockwise) {
        View layoutPosition1 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_1) : mView.findViewById(
                R.id.right_team_layout_1);
        View layoutPosition2 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_2) : mView.findViewById(
                R.id.right_team_layout_2);
        View layoutPosition3 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_3) : mView.findViewById(
                R.id.right_team_layout_3);

        if (clockwise) {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
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
            layoutPosition3
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

    @Override
    protected void update(TeamType teamType) {
        super.update(teamType);
        updateService();
    }

    @Override
    protected void updateService() {
        TeamType servingTeam = mGame.getServingTeam();
        if (servingTeam.equals(mTeamOnLeftSide)) {
            mRightServiceImage1.setVisibility(View.INVISIBLE);
            mRightServiceImage2.setVisibility(View.INVISIBLE);
            mRightServiceImage3.setVisibility(View.INVISIBLE);
            mLeftServiceImage1.setVisibility(View.VISIBLE);
            mLeftServiceImage2.setVisibility(View.VISIBLE);
            mLeftServiceImage3.setVisibility(View.VISIBLE);
        } else {
            mLeftServiceImage1.setVisibility(View.INVISIBLE);
            mLeftServiceImage2.setVisibility(View.INVISIBLE);
            mLeftServiceImage3.setVisibility(View.INVISIBLE);
            mRightServiceImage1.setVisibility(View.VISIBLE);
            mRightServiceImage2.setVisibility(View.VISIBLE);
            mRightServiceImage3.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onServiceSwapped(TeamType teamType, boolean isStart) {
        updateService();
    }
}
