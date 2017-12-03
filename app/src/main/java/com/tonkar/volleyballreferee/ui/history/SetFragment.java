package com.tonkar.volleyballreferee.ui.history;

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
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.game.SetsListAdapter;
import com.tonkar.volleyballreferee.ui.game.SubstitutionsListAdapter;

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
        Log.i("VBR-RecentActivity", "Create set fragment");
        long gameDate = getArguments().getLong("game_date");
        int setIndex = getArguments().getInt("set_index");
        RecordedGameService gameService = ServicesProvider.getInstance().getGameHistoryService().getRecordedGameService(gameDate);

        View view = inflater.inflate(R.layout.fragment_set, container, false);

        FrameLayout ladderLayout = view.findViewById(R.id.ladder_layout);
        SetsListAdapter setsListAdapter = new SetsListAdapter(inflater, gameService, gameService, false);
        ladderLayout.addView(setsListAdapter.getView(setIndex, null, ladderLayout));

        GridView homeTeamLineup = view.findViewById(R.id.home_team_lineup);
        LineupAdapter homeTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.HOME, setIndex);
        homeTeamLineup.setAdapter(homeTeamLineupAdapter);

        GridView guestTeamLineup = view.findViewById(R.id.guest_team_lineup);
        LineupAdapter guestTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), gameService, TeamType.GUEST, setIndex);
        guestTeamLineup.setAdapter(guestTeamLineupAdapter);

        ListView homeTeamSubstitutions = view.findViewById(R.id.home_team_substitutions);
        SubstitutionsListAdapter homeTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.HOME, setIndex);
        homeTeamSubstitutions.setAdapter(homeTeamSubstitutionsAdapter);

        ListView guestTeamSubstitutions = view.findViewById(R.id.guest_team_substitutions);
        SubstitutionsListAdapter guestTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, gameService, TeamType.GUEST, setIndex);
        guestTeamSubstitutions.setAdapter(guestTeamSubstitutionsAdapter);

        return view;
    }

}
