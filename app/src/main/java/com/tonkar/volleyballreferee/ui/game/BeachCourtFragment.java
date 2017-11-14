package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.BeachTeamService;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import java.util.Map;

public class BeachCourtFragment extends CourtFragment {

    private BeachTeamService mBeachTeamService;

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
        Log.i("VBR-Court", "Create beach court view");

        mBeachTeamService = (BeachTeamService) mTeamService;

        mView = inflater.inflate(R.layout.fragment_beach_court, container, false);

        addButtonOnLeftSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.left_team_position_2));

        addButtonOnRightSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.right_team_position_2));

        initView();

        return mView;
    }

    @Override
    protected void initView() {
        super.initView();

        for (Map.Entry<PositionType,Button> entry : mLeftTeamPositions.entrySet()) {
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("VBR-Court", String.format("Swap %s team player", mTeamOnLeftSide.toString()));
                    mBeachTeamService.swapPlayers(mTeamOnLeftSide);
                }
            });
        }

        for (Map.Entry<PositionType,Button> entry : mRightTeamPositions.entrySet()) {
            entry.getValue().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("VBR-Court", String.format("Swap %s team player", mTeamOnRightSide.toString()));
                    mBeachTeamService.swapPlayers(mTeamOnRightSide);
                }
            });
        }
    }

    protected void applyColor(TeamType teamType, int number, Button button) {
        applyColor(teamType, button);
    }

}
