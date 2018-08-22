package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.IndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

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

        mIndoorTeamService = (IndoorTeamService) mTeamService;
        mLayoutInflater = inflater;

        addButtonOnLeftSide(PositionType.POSITION_1, (MaterialButton) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (MaterialButton) mView.findViewById(R.id.left_team_position_2));
        addButtonOnLeftSide(PositionType.POSITION_3, (MaterialButton) mView.findViewById(R.id.left_team_position_3));
        addButtonOnLeftSide(PositionType.POSITION_4, (MaterialButton) mView.findViewById(R.id.left_team_position_4));

        addButtonOnRightSide(PositionType.POSITION_1, (MaterialButton) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (MaterialButton) mView.findViewById(R.id.right_team_position_2));
        addButtonOnRightSide(PositionType.POSITION_3, (MaterialButton) mView.findViewById(R.id.right_team_position_3));
        addButtonOnRightSide(PositionType.POSITION_4, (MaterialButton) mView.findViewById(R.id.right_team_position_4));

        onTeamsSwapped(mTeamOnLeftSide, mTeamOnRightSide, null);

        initLeftTeamListeners();
        initRightTeamListeners();

        if (savedInstanceState != null) {
            restoreStartingLineupDialog();
        }

        return mView;
    }
}
