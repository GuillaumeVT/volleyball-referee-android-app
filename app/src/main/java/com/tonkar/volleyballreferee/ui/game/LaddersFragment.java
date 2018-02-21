package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.score.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.interfaces.timeout.TimeoutListener;

public class LaddersFragment extends Fragment implements NamedGameFragment, ScoreListener, TimeoutListener, TeamListener, SanctionListener {

    private LadderListAdapter mLadderListAdapter;
    private GameService       mGameService;

    public LaddersFragment() {
    }

    public static LaddersFragment newInstance() {
        LaddersFragment fragment = new LaddersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.points_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Ladders", "Initialise scores fragment");
        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        mGameService = ServicesProvider.getInstance().getGameService();

        mGameService.addScoreListener(this);
        mGameService.addTimeoutListener(this);
        mGameService.addTeamListener(this);
        mGameService.addSanctionListener(this);

        ListView setsList = view.findViewById(R.id.set_list);
        mLadderListAdapter = new LadderListAdapter(inflater, mGameService, mGameService, mGameService, mGameService, true);
        setsList.setAdapter(mLadderListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGameService.removeScoreListener(this);
        mGameService.removeTimeoutListener(this);
        mGameService.removeTeamListener(this);
        mGameService.removeSanctionListener(this);
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        mLadderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {}

    @Override
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(TeamType winner) {}

    @Override
    public void onTimeoutUpdated(TeamType teamType, int maxCount, int newCount) {
        mLadderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onTimeout(TeamType teamType, int duration) {}

    @Override
    public void onTechnicalTimeout(int duration) {}

    @Override
    public void onGameInterval(int duration) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {}

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (ActionOriginType.USER.equals(actionOriginType) && !PositionType.BENCH.equals(positionType)) {
            mLadderListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        mLadderListAdapter.notifyDataSetChanged();
    }
}
