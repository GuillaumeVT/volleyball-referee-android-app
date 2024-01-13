package com.tonkar.volleyballreferee.ui.game.timeout;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

public class TimeoutsFragment extends Fragment implements TimeoutListener, TeamListener, GameServiceHandler {

    private IGame               mGame;
    private TimeoutsListAdapter mLeftTeamTimeoutsListAdapter;
    private TimeoutsListAdapter mRightTeamTimeoutsListAdapter;

    public TimeoutsFragment() {}

    public static TimeoutsFragment newInstance() {
        TimeoutsFragment fragment = new TimeoutsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.TIMEOUT, "Create Timeouts fragment");
        View view = inflater.inflate(R.layout.fragment_timeouts, container, false);

        if (mGame != null) {
            mGame.addTimeoutListener(this);
            mGame.addTeamListener(this);

            ListView leftTeamTimeoutsList = view.findViewById(R.id.left_team_timeouts_list);
            mLeftTeamTimeoutsListAdapter = new TimeoutsListAdapter(requireActivity(), inflater, mGame, mGame, mGame.getTeamOnLeftSide());
            leftTeamTimeoutsList.setAdapter(mLeftTeamTimeoutsListAdapter);

            ListView rightTeamTimeoutsList = view.findViewById(R.id.right_team_timeouts_list);
            mRightTeamTimeoutsListAdapter = new TimeoutsListAdapter(requireActivity(), inflater, mGame, mGame, mGame.getTeamOnRightSide());
            rightTeamTimeoutsList.setAdapter(mRightTeamTimeoutsListAdapter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGame != null) {
            mGame.removeTimeoutListener(this);
            mGame.removeTeamListener(this);
        }
    }

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamTimeoutsListAdapter.setTeamType(leftTeamType);
        mRightTeamTimeoutsListAdapter.setTeamType(rightTeamType);
        mLeftTeamTimeoutsListAdapter.notifyDataSetChanged();
        mRightTeamTimeoutsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {}

    @Override
    public void onTimeout(TeamType teamType, int duration) {
        mLeftTeamTimeoutsListAdapter.notifyDataSetChanged();
        mRightTeamTimeoutsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTechnicalTimeout(int duration) {

    }

    @Override
    public void onGameInterval(int duration) {
        mLeftTeamTimeoutsListAdapter.notifyDataSetChanged();
        mRightTeamTimeoutsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
