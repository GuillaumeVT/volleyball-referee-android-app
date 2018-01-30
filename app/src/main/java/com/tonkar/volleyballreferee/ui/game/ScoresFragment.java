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
import com.tonkar.volleyballreferee.interfaces.ScoreListener;
import com.tonkar.volleyballreferee.interfaces.ScoreService;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class ScoresFragment extends Fragment implements NamedGameFragment, ScoreListener {

    private SetsListAdapter mSetsListAdapter;
    private ScoreService    mScoreService;

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
        return context.getResources().getString(R.string.points_tab);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-Ladders", "Initialise scores fragment");
        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        mScoreService = ServicesProvider.getInstance().getScoreService();
        TeamService teamService = ServicesProvider.getInstance().getTeamService();

        mScoreService.addScoreListener(this);

        ListView setsList = view.findViewById(R.id.set_list);
        mSetsListAdapter = new SetsListAdapter(inflater, mScoreService, teamService, true);
        setsList.setAdapter(mSetsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mScoreService.removeScoreListener(this);
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
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(TeamType winner) {}

}
