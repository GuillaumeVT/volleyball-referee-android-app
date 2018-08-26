package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.SanctionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.LadderListAdapter;
import com.tonkar.volleyballreferee.ui.game.SubstitutionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.TimeoutsListAdapter;

import androidx.fragment.app.Fragment;

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
        Log.i(Tags.SAVED_GAMES, "Create set fragment");
        View view = inflater.inflate(R.layout.fragment_set, container, false);

        long gameDate = getArguments().getLong("game_date");
        int setIndex = getArguments().getInt("set_index");
        RecordedGameService gameService = ServicesProvider.getInstance().getRecordedGamesService(getActivity().getApplicationContext()).getRecordedGameService(gameDate);

        FrameLayout ladderLayout = view.findViewById(R.id.ladder_layout);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(inflater, gameService, gameService, gameService, gameService, false);
        ladderLayout.addView(ladderListAdapter.getView(setIndex, null, ladderLayout));

        if (gameService.isStartingLineupConfirmed(setIndex)) {
            GridView homeTeamLineup = view.findViewById(R.id.home_team_lineup);
            final LineupAdapter homeTeamLineupAdapter;
            if (GameType.INDOOR.equals(gameService.getGameType())) {
                homeTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.HOME, setIndex);
            } else {
                homeTeamLineupAdapter = new Lineup4x4Adapter(inflater, getActivity(), gameService, TeamType.HOME, setIndex);
            }
            homeTeamLineup.setAdapter(homeTeamLineupAdapter);

            GridView guestTeamLineup = view.findViewById(R.id.guest_team_lineup);
            final LineupAdapter guestTeamLineupAdapter;
            if (GameType.INDOOR.equals(gameService.getGameType())) {
                guestTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.GUEST, setIndex);
            } else {
                guestTeamLineupAdapter = new Lineup4x4Adapter(inflater, getActivity(), gameService, TeamType.GUEST, setIndex);
            }
            guestTeamLineup.setAdapter(guestTeamLineupAdapter);
        } else {
            view.findViewById(R.id.set_lineup_card).setVisibility(View.GONE);
        }

        if (gameService.getSubstitutions(TeamType.HOME, setIndex).isEmpty() && gameService.getSubstitutions(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_substitutions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSubstitutions = view.findViewById(R.id.home_team_substitutions);
            SubstitutionsListAdapter homeTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.HOME, setIndex);
            homeTeamSubstitutions.setAdapter(homeTeamSubstitutionsAdapter);

            ListView guestTeamSubstitutions = view.findViewById(R.id.guest_team_substitutions);
            SubstitutionsListAdapter guestTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.GUEST, setIndex);
            guestTeamSubstitutions.setAdapter(guestTeamSubstitutionsAdapter);
        }

        if (gameService.getCalledTimeouts(TeamType.HOME, setIndex).isEmpty() && gameService.getCalledTimeouts(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_timeouts_card).setVisibility(View.GONE);
        } else {
            GridView homeTeamTimeouts = view.findViewById(R.id.home_team_timeouts);
            TimeoutsListAdapter homeTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.HOME, setIndex);
            homeTeamTimeouts.setAdapter(homeTeamTimeoutsAdapter);

            GridView guestTeamTimeouts = view.findViewById(R.id.guest_team_timeouts);
            TimeoutsListAdapter guestTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.GUEST, setIndex);
            guestTeamTimeouts.setAdapter(guestTeamTimeoutsAdapter);
        }

        if (gameService.getGivenSanctions(TeamType.HOME, setIndex).isEmpty() && gameService.getGivenSanctions(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_sanctions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSanctions = view.findViewById(R.id.home_team_sanctions);
            SanctionsListAdapter homeTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.HOME, setIndex);
            homeTeamSanctions.setAdapter(homeTeamSanctionsAdapter);

            ListView guestTeamSanctions = view.findViewById(R.id.guest_team_sanctions);
            SanctionsListAdapter guestTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, gameService, gameService, TeamType.GUEST, setIndex);
            guestTeamSanctions.setAdapter(guestTeamSanctionsAdapter);
        }

        return view;
    }

}
