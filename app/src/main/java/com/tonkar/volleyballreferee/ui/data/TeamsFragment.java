package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import androidx.fragment.app.Fragment;
import com.tonkar.volleyballreferee.ui.interfaces.RecordedGameServiceHandler;

public class TeamsFragment extends Fragment implements RecordedGameServiceHandler {

    private RecordedGameService mRecordedGameService;

    public TeamsFragment() {}

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SAVED_GAMES, "Create teams fragment");
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        GridView homeTeamPlayersList = view.findViewById(R.id.home_team_players_list);
        PlayersListAdapter homeTeamPlayersListAdapter = new PlayersListAdapter(inflater, getActivity(), mRecordedGameService, TeamType.HOME);
        homeTeamPlayersList.setAdapter(homeTeamPlayersListAdapter);

        GridView guestTeamPlayersList = view.findViewById(R.id.guest_team_players_list);
        PlayersListAdapter guestTeamPlayersListAdapter = new PlayersListAdapter(inflater, getActivity(), mRecordedGameService, TeamType.GUEST);
        guestTeamPlayersList.setAdapter(guestTeamPlayersListAdapter);

        return view;
    }

    @Override
    public void setRecordedGameService(RecordedGameService recordedGameService) {
        mRecordedGameService = recordedGameService;
    }
}
