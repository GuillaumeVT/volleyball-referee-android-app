package com.tonkar.volleyballreferee.ui.game.ladder;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.*;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.game.timeout.TimeoutListener;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

public class LaddersFragment extends Fragment
        implements ScoreListener, TimeoutListener, TeamListener, SanctionListener, GameServiceHandler {

    private LadderListAdapter mLadderListAdapter;
    private IGame             mGame;

    public LaddersFragment() {
    }

    public static LaddersFragment newInstance() {
        LaddersFragment fragment = new LaddersFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SCORE, "Initialise scores fragment");
        View view = inflater.inflate(R.layout.fragment_scores, container, false);

        if (mGame != null) {
            mGame.addScoreListener(this);
            mGame.addTimeoutListener(this);
            mGame.addTeamListener(this);
            mGame.addSanctionListener(this);

            ListView setsList = view.findViewById(R.id.set_list);
            mLadderListAdapter = new LadderListAdapter(inflater, mGame, mGame, mGame, mGame, true);
            setsList.setAdapter(mLadderListAdapter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGame != null) {
            mGame.removeScoreListener(this);
            mGame.removeTimeoutListener(this);
            mGame.removeTeamListener(this);
            mGame.removeSanctionListener(this);
        }
    }

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {
        mLadderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onServiceSwapped(TeamType teamType, boolean isStart) {}

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
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {}

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (ActionOriginType.USER.equals(actionOriginType) && !PositionType.BENCH.equals(positionType)) {
            mLadderListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        mLadderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {
        mLadderListAdapter.notifyDataSetChanged();
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
