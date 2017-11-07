package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.PositionType;

public class IndoorCourtFragment extends CourtFragment {

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
        Log.i("VBR-Court", "Create indoor court view");

        mView = inflater.inflate(R.layout.fragment_indoor_court, container, false);

        addButtonOnLeftSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.left_team_position_2));
        addButtonOnLeftSide(PositionType.POSITION_3, (Button) mView.findViewById(R.id.left_team_position_3));
        addButtonOnLeftSide(PositionType.POSITION_4, (Button) mView.findViewById(R.id.left_team_position_4));
        addButtonOnLeftSide(PositionType.POSITION_5, (Button) mView.findViewById(R.id.left_team_position_5));
        addButtonOnLeftSide(PositionType.POSITION_6, (Button) mView.findViewById(R.id.left_team_position_6));

        addButtonOnRightSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.right_team_position_2));
        addButtonOnRightSide(PositionType.POSITION_3, (Button) mView.findViewById(R.id.right_team_position_3));
        addButtonOnRightSide(PositionType.POSITION_4, (Button) mView.findViewById(R.id.right_team_position_4));
        addButtonOnRightSide(PositionType.POSITION_5, (Button) mView.findViewById(R.id.right_team_position_5));
        addButtonOnRightSide(PositionType.POSITION_6, (Button) mView.findViewById(R.id.right_team_position_6));

        initView();

        return mView;
    }

}
