package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutService;

public class TimeoutsFragment extends Fragment implements NamedGameFragment, TimeoutListener, TeamListener {

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
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.timeouts_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Timeouts", "Create Timeouts fragment");
        View view = inflater.inflate(R.layout.fragment_timeouts, container, false);

        mTimeoutService = ServicesProvider.getInstance().getTimeoutService();
        mTimeoutService.addTimeoutListener(this);

        mTeamService = ServicesProvider.getInstance().getTeamService();
        mTeamService.addTeamListener(this);

        GridView leftTeamTimeoutsGrid = view.findViewById(R.id.left_team_timeouts_list);
        mLeftTeamTimeoutsListAdapter = new TimeoutsListAdapter(getActivity(), inflater, mTimeoutService, mTeamService, mTeamService.getTeamOnLeftSide());
        leftTeamTimeoutsGrid.setAdapter(mLeftTeamTimeoutsListAdapter);

        GridView rightTeamTimeoutsGrid = view.findViewById(R.id.right_team_timeouts_list);
        mRightTeamTimeoutsListAdapter = new TimeoutsListAdapter(getActivity(), inflater, mTimeoutService, mTeamService, mTeamService.getTeamOnRightSide());
        rightTeamTimeoutsGrid.setAdapter(mRightTeamTimeoutsListAdapter);

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
