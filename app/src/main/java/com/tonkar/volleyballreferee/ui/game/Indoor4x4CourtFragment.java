package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class Indoor4x4CourtFragment extends IndoorCourtFragment {

    public Indoor4x4CourtFragment() {
        super();
    }

    public static Indoor4x4CourtFragment newInstance() {
        Indoor4x4CourtFragment fragment = new Indoor4x4CourtFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create indoor court view");
        mView = inflater.inflate(R.layout.fragment_indoor_4x4_court, container, false);

        initView();

        if (mIndoorTeamService != null) {
            mLayoutInflater = inflater;

            addButtonOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_position_1));
            addButtonOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_position_2));
            addButtonOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_position_3));
            addButtonOnLeftSide(PositionType.POSITION_4, mView.findViewById(R.id.left_team_position_4));

            addButtonOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_position_1));
            addButtonOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_position_2));
            addButtonOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_position_3));
            addButtonOnRightSide(PositionType.POSITION_4, mView.findViewById(R.id.right_team_position_4));

            addSanctionImageOnLeftSide(PositionType.POSITION_1, mView.findViewById(R.id.left_team_sanction_1));
            addSanctionImageOnLeftSide(PositionType.POSITION_2, mView.findViewById(R.id.left_team_sanction_2));
            addSanctionImageOnLeftSide(PositionType.POSITION_3, mView.findViewById(R.id.left_team_sanction_3));
            addSanctionImageOnLeftSide(PositionType.POSITION_4, mView.findViewById(R.id.left_team_sanction_4));

            addSanctionImageOnRightSide(PositionType.POSITION_1, mView.findViewById(R.id.right_team_sanction_1));
            addSanctionImageOnRightSide(PositionType.POSITION_2, mView.findViewById(R.id.right_team_sanction_2));
            addSanctionImageOnRightSide(PositionType.POSITION_3, mView.findViewById(R.id.right_team_sanction_3));
            addSanctionImageOnRightSide(PositionType.POSITION_4, mView.findViewById(R.id.right_team_sanction_4));

            initLeftTeamListeners();
            initRightTeamListeners();

            onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);
        }

        return mView;
    }

    @Override
    protected void rotateAnimation(TeamType teamType, boolean clockwise) {
        View layoutPosition1 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_1) : mView.findViewById(R.id.right_team_layout_1);
        View layoutPosition2 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_2) : mView.findViewById(R.id.right_team_layout_2);
        View layoutPosition3 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_3) : mView.findViewById(R.id.right_team_layout_3);
        View layoutPosition4 = mTeamOnLeftSide.equals(teamType) ? mView.findViewById(R.id.left_team_layout_4) : mView.findViewById(R.id.right_team_layout_4);

        if (clockwise) {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition1.getX()).y(layoutPosition1.getY()).setDuration(500L).withEndAction(() -> {
                getFragmentManager()
                        .beginTransaction()
                        .detach(Indoor4x4CourtFragment.this)
                        .attach(Indoor4x4CourtFragment.this)
                        .commit();
            }).start();
        } else {
            layoutPosition1.animate().setStartDelay(0L).x(layoutPosition2.getX()).y(layoutPosition2.getY()).setDuration(500L).start();
            layoutPosition2.animate().setStartDelay(0L).x(layoutPosition3.getX()).y(layoutPosition3.getY()).setDuration(500L).start();
            layoutPosition3.animate().setStartDelay(0L).x(layoutPosition4.getX()).y(layoutPosition4.getY()).setDuration(500L).start();
            layoutPosition4.animate().setStartDelay(0L).x(layoutPosition1.getX()).y(layoutPosition1.getY()).setDuration(500L).withEndAction(() -> {
                getFragmentManager()
                        .beginTransaction()
                        .detach(Indoor4x4CourtFragment.this)
                        .attach(Indoor4x4CourtFragment.this)
                        .commit();
            }).start();
        }
    }
}
