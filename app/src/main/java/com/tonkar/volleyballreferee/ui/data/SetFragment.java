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
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.SanctionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.LadderListAdapter;
import com.tonkar.volleyballreferee.ui.game.SubstitutionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.TimeoutsListAdapter;

import androidx.fragment.app.Fragment;
import com.tonkar.volleyballreferee.ui.interfaces.RecordedGameServiceHandler;

public class SetFragment extends Fragment implements RecordedGameServiceHandler {

    private RecordedGameService mRecordedGameService;

    public SetFragment() {}

    public static SetFragment newInstance(int setIndex) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putInt("set_index", setIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.SAVED_GAMES, "Create set fragment");
        View view = inflater.inflate(R.layout.fragment_set, container, false);

        int setIndex = getArguments().getInt("set_index");

        FrameLayout ladderLayout = view.findViewById(R.id.ladder_layout);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(inflater, mRecordedGameService, mRecordedGameService, mRecordedGameService, mRecordedGameService, false);
        ladderLayout.addView(ladderListAdapter.getView(setIndex, null, ladderLayout));

        if (mRecordedGameService.isStartingLineupConfirmed(setIndex)) {
            GridView homeTeamLineup = view.findViewById(R.id.home_team_lineup);
            final LineupAdapter homeTeamLineupAdapter;
            if (GameType.INDOOR.equals(mRecordedGameService.getGameType())) {
                homeTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), mRecordedGameService, TeamType.HOME, setIndex);
            } else {
                homeTeamLineupAdapter = new Lineup4x4Adapter(inflater, getActivity(), mRecordedGameService, TeamType.HOME, setIndex);
            }
            homeTeamLineup.setAdapter(homeTeamLineupAdapter);

            GridView guestTeamLineup = view.findViewById(R.id.guest_team_lineup);
            final LineupAdapter guestTeamLineupAdapter;
            if (GameType.INDOOR.equals(mRecordedGameService.getGameType())) {
                guestTeamLineupAdapter = new LineupAdapter(inflater, getActivity(), mRecordedGameService, TeamType.GUEST, setIndex);
            } else {
                guestTeamLineupAdapter = new Lineup4x4Adapter(inflater, getActivity(), mRecordedGameService, TeamType.GUEST, setIndex);
            }
            guestTeamLineup.setAdapter(guestTeamLineupAdapter);
        } else {
            view.findViewById(R.id.set_lineup_card).setVisibility(View.GONE);
        }

        if (mRecordedGameService.getSubstitutions(TeamType.HOME, setIndex).isEmpty() && mRecordedGameService.getSubstitutions(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_substitutions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSubstitutions = view.findViewById(R.id.home_team_substitutions);
            SubstitutionsListAdapter homeTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, mRecordedGameService, TeamType.HOME, setIndex);
            homeTeamSubstitutions.setAdapter(homeTeamSubstitutionsAdapter);

            ListView guestTeamSubstitutions = view.findViewById(R.id.guest_team_substitutions);
            SubstitutionsListAdapter guestTeamSubstitutionsAdapter = new SubstitutionsListAdapter(getActivity(), inflater, mRecordedGameService, TeamType.GUEST, setIndex);
            guestTeamSubstitutions.setAdapter(guestTeamSubstitutionsAdapter);
        }

        if (mRecordedGameService.getCalledTimeouts(TeamType.HOME, setIndex).isEmpty() && mRecordedGameService.getCalledTimeouts(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_timeouts_card).setVisibility(View.GONE);
        } else {
            GridView homeTeamTimeouts = view.findViewById(R.id.home_team_timeouts);
            TimeoutsListAdapter homeTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, mRecordedGameService, mRecordedGameService, TeamType.HOME, setIndex);
            homeTeamTimeouts.setAdapter(homeTeamTimeoutsAdapter);

            GridView guestTeamTimeouts = view.findViewById(R.id.guest_team_timeouts);
            TimeoutsListAdapter guestTeamTimeoutsAdapter = new TimeoutsListAdapter(getActivity(), inflater, mRecordedGameService, mRecordedGameService, TeamType.GUEST, setIndex);
            guestTeamTimeouts.setAdapter(guestTeamTimeoutsAdapter);
        }

        if (mRecordedGameService.getGivenSanctions(TeamType.HOME, setIndex).isEmpty() && mRecordedGameService.getGivenSanctions(TeamType.GUEST, setIndex).isEmpty()) {
            view.findViewById(R.id.set_sanctions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSanctions = view.findViewById(R.id.home_team_sanctions);
            SanctionsListAdapter homeTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, mRecordedGameService, mRecordedGameService, TeamType.HOME, setIndex);
            homeTeamSanctions.setAdapter(homeTeamSanctionsAdapter);

            ListView guestTeamSanctions = view.findViewById(R.id.guest_team_sanctions);
            SanctionsListAdapter guestTeamSanctionsAdapter = new SanctionsListAdapter(getActivity(), inflater, mRecordedGameService, mRecordedGameService, TeamType.GUEST, setIndex);
            guestTeamSanctions.setAdapter(guestTeamSanctionsAdapter);
        }

        return view;
    }

    @Override
    public void setRecordedGameService(RecordedGameService recordedGameService) {
        mRecordedGameService = recordedGameService;
    }
}
