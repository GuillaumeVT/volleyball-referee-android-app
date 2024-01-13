package com.tonkar.volleyballreferee.ui.data.team;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGameHandler;

public class TeamsFragment extends Fragment implements StoredGameHandler {

    private IStoredGame mStoredGame;

    public TeamsFragment() {}

    public static TeamsFragment newInstance() {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.STORED_GAMES, "Create teams fragment");
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        ListView homeTeamPlayersList = view.findViewById(R.id.home_team_players_list);
        PlayersListAdapter homeTeamPlayersListAdapter = new PlayersListAdapter(inflater, requireActivity(), mStoredGame, TeamType.HOME);
        homeTeamPlayersList.setAdapter(homeTeamPlayersListAdapter);

        ListView guestTeamPlayersList = view.findViewById(R.id.guest_team_players_list);
        PlayersListAdapter guestTeamPlayersListAdapter = new PlayersListAdapter(inflater, requireActivity(), mStoredGame, TeamType.GUEST);
        guestTeamPlayersList.setAdapter(guestTeamPlayersListAdapter);

        return view;
    }

    @Override
    public void setStoredGame(IStoredGame storedGame) {
        mStoredGame = storedGame;
    }
}
