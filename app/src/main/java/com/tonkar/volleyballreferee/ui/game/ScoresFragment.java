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
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ScoreClient;
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class ScoresFragment extends Fragment implements NamedGameFragment, ScoreClient, TeamClient, ScoreListener {

    private SetsListAdapter mSetsListAdapter;
    private ScoreService    mScoreService;
    private TeamService     mTeamService;

    public ScoresFragment() {
    }

    public static ScoresFragment newInstance() {
        ScoresFragment fragment = new ScoresFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.scores_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Ladders", "Create scores fragment");
        setScoreService(ServicesProvider.getInstance().getScoreService());
        setTeamService(ServicesProvider.getInstance().getTeamService());

        mScoreService.addScoreListener(this);

        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        ListView setsList = view.findViewById(R.id.set_list);
        mSetsListAdapter = new SetsListAdapter(inflater, mScoreService, mTeamService, true);
        setsList.setAdapter(mSetsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScoreService.removeScoreListener(this);
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void setScoreService(ScoreService scoreService) {
        mScoreService = scoreService;
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        mSetsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType) {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(TeamType winner) {}

}
