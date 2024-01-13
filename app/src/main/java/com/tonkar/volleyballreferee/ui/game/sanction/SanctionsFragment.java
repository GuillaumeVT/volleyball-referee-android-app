package com.tonkar.volleyballreferee.ui.game.sanction;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

public class SanctionsFragment extends Fragment implements TeamListener, SanctionListener, GameServiceHandler {

    private IGame                mGame;
    private SanctionsListAdapter mLeftTeamSanctionsListAdapter;
    private SanctionsListAdapter mRightTeamSanctionsListAdapter;

    public SanctionsFragment() {}

    public static SanctionsFragment newInstance() {
        SanctionsFragment fragment = new SanctionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create sanctions fragment");
        View view = inflater.inflate(R.layout.fragment_sanctions, container, false);

        if (mGame != null) {
            mGame.addSanctionListener(this);
            mGame.addTeamListener(this);

            ListView leftTeamSanctionsList = view.findViewById(R.id.left_team_sanctions_list);
            mLeftTeamSanctionsListAdapter = new SanctionsListAdapter(requireActivity(), inflater, mGame, mGame, mGame.getTeamOnLeftSide());
            leftTeamSanctionsList.setAdapter(mLeftTeamSanctionsListAdapter);

            ListView rightTeamSanctionsList = view.findViewById(R.id.right_team_sanctions_list);
            mRightTeamSanctionsListAdapter = new SanctionsListAdapter(requireActivity(), inflater, mGame, mGame,
                                                                      mGame.getTeamOnRightSide());
            rightTeamSanctionsList.setAdapter(mRightTeamSanctionsListAdapter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGame != null) {
            mGame.removeSanctionListener(this);
            mGame.removeTeamListener(this);
        }
    }

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamSanctionsListAdapter.setTeamType(leftTeamType);
        mRightTeamSanctionsListAdapter.setTeamType(rightTeamType);
        mLeftTeamSanctionsListAdapter.notifyDataSetChanged();
        mRightTeamSanctionsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {}

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void onSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (teamType.equals(mLeftTeamSanctionsListAdapter.getTeamType())) {
            mLeftTeamSanctionsListAdapter.notifyDataSetChanged();
        } else {
            mRightTeamSanctionsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onUndoSanction(TeamType teamType, SanctionType sanctionType, int number) {
        if (teamType.equals(mLeftTeamSanctionsListAdapter.getTeamType())) {
            mLeftTeamSanctionsListAdapter.notifyDataSetChanged();
        } else {
            mRightTeamSanctionsListAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setGameService(IGame game) {
        mGame = game;
    }
}
