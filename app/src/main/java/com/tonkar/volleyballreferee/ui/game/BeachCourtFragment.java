package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.PositionType;

public class BeachCourtFragment extends CourtFragment {

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

        mView = inflater.inflate(R.layout.fragment_beach_court, container, false);

        addButtonOnLeftSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.left_team_position_1));
        addButtonOnLeftSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.left_team_position_2));

        addButtonOnRightSide(PositionType.POSITION_1, (Button) mView.findViewById(R.id.right_team_position_1));
        addButtonOnRightSide(PositionType.POSITION_2, (Button) mView.findViewById(R.id.right_team_position_2));

        initView();

        return mView;
    }

}
