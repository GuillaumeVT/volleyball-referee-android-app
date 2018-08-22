package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutService;

import androidx.fragment.app.Fragment;

public class TimeoutsFragment extends Fragment implements TimeoutListener, TeamListener {

    private TimeoutService      mTimeoutService;
    private TeamService         mTeamService;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.TIMEOUT, "Create Timeouts fragment");
        View view = inflater.inflate(R.layout.fragment_timeouts, container, false);

        mTimeoutService = ServicesProvider.getInstance().getTimeoutService();
        mTimeoutService.addTimeoutListener(this);

        mTeamService = ServicesProvider.getInstance().getTeamService();
        mTeamService.addTeamListener(this);

        ListView leftTeamTimeoutsList = view.findViewById(R.id.left_team_timeouts_list);
        mLeftTeamTimeoutsListAdapter = new TimeoutsListAdapter(getActivity(), inflater, mTimeoutService, mTeamService, mTeamService.getTeamOnLeftSide());
        leftTeamTimeoutsList.setAdapter(mLeftTeamTimeoutsListAdapter);

        ListView rightTeamTimeoutsList = view.findViewById(R.id.right_team_timeouts_list);
        mRightTeamTimeoutsListAdapter = new TimeoutsListAdapter(getActivity(), inflater, mTimeoutService, mTeamService, mTeamService.getTeamOnRightSide());
        rightTeamTimeoutsList.setAdapter(mRightTeamTimeoutsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTimeoutService.removeTimeoutListener(this);
        mTeamService.removeTeamListener(this);
    }

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
    public void onTeamRotated(TeamType teamType) {}

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
}
