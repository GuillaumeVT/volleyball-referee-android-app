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
import com.tonkar.volleyballreferee.interfaces.GameClient;
import com.tonkar.volleyballreferee.interfaces.GameListener;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class ScoresFragment extends Fragment implements NamedGameFragment, GameClient, TeamClient, GameListener {

    private SetsListAdapter mSetsListAdapter;
    private GameService     mGameService;
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
        setGameService(ServicesProvider.getInstance().getGameService());
        setTeamService(ServicesProvider.getInstance().getTeamService());

        mGameService.addGameListener(this);

        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        ListView setsList = view.findViewById(R.id.set_list);
        mSetsListAdapter = new SetsListAdapter(inflater, mGameService, mTeamService, true);
        setsList.setAdapter(mSetsListAdapter);

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mGameService.removeGameListener(this);
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void setGameService(GameService gameService) {
        mGameService = gameService;
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
    public void onGameCompleted(TeamType winner) {}

}
