package com.tonkar.volleyballreferee.ui.game.substitution;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;

public class SubstitutionsFragment extends Fragment implements TeamListener, GameServiceHandler {

    private IClassicTeam             mIndoorTeam;
    private SubstitutionsListAdapter mLeftTeamSubstitutionsListAdapter;
    private SubstitutionsListAdapter mRightTeamSubstitutionsListAdapter;

    public SubstitutionsFragment() {}

    public static SubstitutionsFragment newInstance() {
        SubstitutionsFragment fragment = new SubstitutionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.GAME_UI, "Create substitutions fragment");
        View view = inflater.inflate(R.layout.fragment_substitutions, container, false);

        if (mIndoorTeam != null) {
            mIndoorTeam.addTeamListener(this);

            ListView leftTeamSubstitutionsList = view.findViewById(R.id.left_team_substitutions_list);
            mLeftTeamSubstitutionsListAdapter = new SubstitutionsListAdapter(requireActivity(), inflater, mIndoorTeam,
                                                                             mIndoorTeam.getTeamOnLeftSide());
            leftTeamSubstitutionsList.setAdapter(mLeftTeamSubstitutionsListAdapter);

            ListView rightTeamSubstitutionsList = view.findViewById(R.id.right_team_substitutions_list);
            mRightTeamSubstitutionsListAdapter = new SubstitutionsListAdapter(requireActivity(), inflater, mIndoorTeam,
                                                                              mIndoorTeam.getTeamOnRightSide());
            rightTeamSubstitutionsList.setAdapter(mRightTeamSubstitutionsListAdapter);
        }

        return view;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mIndoorTeam != null) {
            mIndoorTeam.removeTeamListener(this);
        }
    }

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mLeftTeamSubstitutionsListAdapter.setTeamType(leftTeamType);
        mRightTeamSubstitutionsListAdapter.setTeamType(rightTeamType);
        mLeftTeamSubstitutionsListAdapter.notifyDataSetChanged();
        mRightTeamSubstitutionsListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (ActionOriginType.USER.equals(actionOriginType)) {
            if (teamType.equals(mLeftTeamSubstitutionsListAdapter.getTeamType())) {
                mLeftTeamSubstitutionsListAdapter.notifyDataSetChanged();
            } else {
                mRightTeamSubstitutionsListAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType, boolean clockwise) {}

    @Override
    public void setGameService(IGame game) {
        mIndoorTeam = (IClassicTeam) game;
    }
}
