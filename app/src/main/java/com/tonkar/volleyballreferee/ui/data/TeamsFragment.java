package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import androidx.fragment.app.Fragment;

public class TeamsFragment extends Fragment {

    public TeamsFragment() {}

    public static TeamsFragment newInstance(long gameDate) {
        TeamsFragment fragment = new TeamsFragment();
        Bundle args = new Bundle();
        args.putLong("game_date", gameDate);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SAVED_GAMES, "Create teams fragment");
        View view = inflater.inflate(R.layout.fragment_teams, container, false);

        long gameDate = getArguments().getLong("game_date");
        BaseTeamService teamService = ServicesProvider.getInstance().getRecordedGamesService(getActivity().getApplicationContext()).getRecordedGameService(gameDate);

        GridView homeTeamPlayersList = view.findViewById(R.id.home_team_players_list);
        PlayersListAdapter homeTeamPlayersListAdapter = new PlayersListAdapter(inflater, getActivity(), teamService, TeamType.HOME);
        homeTeamPlayersList.setAdapter(homeTeamPlayersListAdapter);

        GridView guestTeamPlayersList = view.findViewById(R.id.guest_team_players_list);
        PlayersListAdapter guestTeamPlayersListAdapter = new PlayersListAdapter(inflater, getActivity(), teamService, TeamType.GUEST);
        guestTeamPlayersList.setAdapter(guestTeamPlayersListAdapter);

        return view;
    }

}
