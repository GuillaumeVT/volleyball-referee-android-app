package com.tonkar.volleyballreferee.ui.game;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.GameService;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import androidx.fragment.app.Fragment;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

public class SanctionsFragment extends Fragment implements TeamListener, SanctionListener, GameServiceHandler {

    private GameService          mGameService;
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

        if (mGameService != null) {
            mGameService.addSanctionListener(this);
            mGameService.addTeamListener(this);

            ListView leftTeamSanctionsList = view.findViewById(R.id.left_team_sanctions_list);
            mLeftTeamSanctionsListAdapter = new SanctionsListAdapter(getActivity(), inflater, mGameService, mGameService, mGameService.getTeamOnLeftSide());
            leftTeamSanctionsList.setAdapter(mLeftTeamSanctionsListAdapter);

            ListView rightTeamSanctionsList = view.findViewById(R.id.right_team_sanctions_list);
            mRightTeamSanctionsListAdapter = new SanctionsListAdapter(getActivity(), inflater, mGameService, mGameService, mGameService.getTeamOnRightSide());
            rightTeamSanctionsList.setAdapter(mRightTeamSanctionsListAdapter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGameService != null) {
            mGameService.removeSanctionListener(this);
            mGameService.removeTeamListener(this);
        }
    }

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
    public void setGameService(GameService gameService) {
        mGameService = gameService;
    }
}
