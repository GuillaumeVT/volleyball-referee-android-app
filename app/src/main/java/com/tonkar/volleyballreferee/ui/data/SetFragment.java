package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.SanctionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.LadderListAdapter;
import com.tonkar.volleyballreferee.ui.game.SubstitutionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.TimeoutsListAdapter;

public class SetFragment extends Fragment {

    public SetFragment() {}

    public static SetFragment newInstance(long gameDate, int setIndex) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putLong("game_date", gameDate);
        args.putInt("set_index", setIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-RGameActivity", "Create set fragment");
        View view = inflater.inflate(R.layout.fragment_set, container, false);

        long gameDate = getArguments().getLong("game_date");
        int setIndex = getArguments().getInt("set_index");
        RecordedGameService gameService = ServicesProvider.getInstance().getRecordedGamesService().getRecordedGameService(gameDate);

        FrameLayout ladderLayout = view.findViewById(R.id.ladder_layout);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(inflater, gameService, gameService, gameService, gameService, false);
        ladderLayout.addView(ladderListAdapter.getView(setIndex, null, ladderLayout));

        if (gameService.getPlayersInStartingLineup(TeamType.HOME, setIndex).size() > 0) {
            GridView homeTeamLineup = view.findViewById(R.id.home_team_lineup);
            LineupAdapter homeTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.HOME, setIndex);
            homeTeamLineup.setAdapter(homeTeamLineupAdapter);
        }

        if (gameService.getPlayersInStartingLineup(TeamType.GUEST, setIndex).size() > 0) {
            GridView guestTeamLineup = view.findViewById(R.id.guest_team_lineup);
            LineupAdapter guestTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.GUEST, setIndex);
            guestTeamLineup.setAdapter(guestTeamLineupAdapter);
        }

        ListView homeTeamSubstitutions = view.findViewById(R.id.home_team_substitutions);
        SubstitutionsListAdapter homeTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.HOME, setIndex);
        homeTeamSubstitutions.setAdapter(homeTeamSubstitutionsAdapter);

        ListView guestTeamSubstitutions = view.findViewById(R.id.guest_team_substitutions);
        SubstitutionsListAdapter guestTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.GUEST, setIndex);
        guestTeamSubstitutions.setAdapter(guestTeamSubstitutionsAdapter);

        GridView homeTeamTimeouts = view.findViewById(R.id.home_team_timeouts);
        TimeoutsListAdapter homeTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.HOME, setIndex);
        homeTeamTimeouts.setAdapter(homeTeamTimeoutsAdapter);

        GridView guestTeamTimeouts = view.findViewById(R.id.guest_team_timeouts);
        TimeoutsListAdapter guestTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.GUEST, setIndex);
        guestTeamTimeouts.setAdapter(guestTeamTimeoutsAdapter);

        ListView homeTeamSanctions = view.findViewById(R.id.home_team_sanctions);
        SanctionsListAdapter homeTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.HOME, setIndex);
        homeTeamSanctions.setAdapter(homeTeamSanctionsAdapter);

        ListView guestTeamSanctions = view.findViewById(R.id.guest_team_sanctions);
        SanctionsListAdapter guestTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.GUEST, setIndex);
        guestTeamSanctions.setAdapter(guestTeamSanctionsAdapter);

        return view;
    }

}
