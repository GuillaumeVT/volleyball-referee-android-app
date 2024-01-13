package com.tonkar.volleyballreferee.ui.data.game;

import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.service.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.ladder.LadderListAdapter;
import com.tonkar.volleyballreferee.ui.game.sanction.SanctionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.substitution.SubstitutionsListAdapter;
import com.tonkar.volleyballreferee.ui.game.timeout.TimeoutsListAdapter;
import com.tonkar.volleyballreferee.ui.interfaces.StoredGameHandler;

public class SetFragment extends Fragment implements StoredGameHandler {

    private IStoredGame mStoredGame;

    public SetFragment() {}

    public static SetFragment newInstance(int setIndex) {
        SetFragment fragment = new SetFragment();
        Bundle args = new Bundle();
        args.putInt("set_index", setIndex);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(Tags.STORED_GAMES, "Create set fragment");
        View view = inflater.inflate(R.layout.fragment_set, container, false);

        int setIndex = requireArguments().getInt("set_index");

        FrameLayout ladderLayout = view.findViewById(R.id.ladder_layout);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(inflater, mStoredGame, mStoredGame, mStoredGame, mStoredGame, false);
        ladderLayout.addView(ladderListAdapter.getView(setIndex, null, ladderLayout));

        if ((mStoredGame.getKind().equals(GameType.INDOOR) || mStoredGame.getKind().equals(GameType.INDOOR_4X4) || mStoredGame
                .getKind()
                .equals(GameType.SNOW)) && (mStoredGame.isStartingLineupConfirmed(TeamType.HOME,
                                                                                  setIndex) || mStoredGame.isStartingLineupConfirmed(
                TeamType.GUEST, setIndex))) {
            if (mStoredGame.isStartingLineupConfirmed(TeamType.HOME, setIndex)) {
                GridView homeTeamLineup = view.findViewById(R.id.home_team_lineup);

                switch (mStoredGame.getKind()) {
                    case INDOOR:
                        LineupAdapter homeTeamLineupAdapter = new LineupAdapter(inflater, requireActivity(), mStoredGame, TeamType.HOME,
                                                                                setIndex);
                        homeTeamLineup.setAdapter(homeTeamLineupAdapter);
                        break;
                    case INDOOR_4X4:
                        homeTeamLineupAdapter = new Lineup4x4Adapter(inflater, requireActivity(), mStoredGame, TeamType.HOME, setIndex);
                        homeTeamLineup.setAdapter(homeTeamLineupAdapter);
                        break;
                    case SNOW:
                        homeTeamLineupAdapter = new LineupSnowAdapter(inflater, requireActivity(), mStoredGame, TeamType.HOME, setIndex);
                        homeTeamLineup.setAdapter(homeTeamLineupAdapter);
                        break;
                }
            }

            if (mStoredGame.isStartingLineupConfirmed(TeamType.GUEST, setIndex)) {
                GridView guestTeamLineup = view.findViewById(R.id.guest_team_lineup);

                switch (mStoredGame.getKind()) {
                    case INDOOR:
                        LineupAdapter guestTeamLineupAdapter = new LineupAdapter(inflater, requireActivity(), mStoredGame, TeamType.GUEST,
                                                                                 setIndex);
                        guestTeamLineup.setAdapter(guestTeamLineupAdapter);
                        break;
                    case INDOOR_4X4:
                        guestTeamLineupAdapter = new Lineup4x4Adapter(inflater, requireActivity(), mStoredGame, TeamType.GUEST, setIndex);
                        guestTeamLineup.setAdapter(guestTeamLineupAdapter);
                        break;
                    case SNOW:
                        guestTeamLineupAdapter = new LineupSnowAdapter(inflater, requireActivity(), mStoredGame, TeamType.GUEST, setIndex);
                        guestTeamLineup.setAdapter(guestTeamLineupAdapter);
                        break;
                }
            }
        } else {
            view.findViewById(R.id.set_lineup_card).setVisibility(View.GONE);
        }

        if (mStoredGame.getSubstitutions(TeamType.HOME, setIndex).isEmpty() && mStoredGame
                .getSubstitutions(TeamType.GUEST, setIndex)
                .isEmpty()) {
            view.findViewById(R.id.set_substitutions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSubstitutions = view.findViewById(R.id.home_team_substitutions);
            SubstitutionsListAdapter homeTeamSubstitutionsAdapter = new SubstitutionsListAdapter(requireActivity(), inflater, mStoredGame,
                                                                                                 TeamType.HOME, setIndex);
            homeTeamSubstitutions.setAdapter(homeTeamSubstitutionsAdapter);

            ListView guestTeamSubstitutions = view.findViewById(R.id.guest_team_substitutions);
            SubstitutionsListAdapter guestTeamSubstitutionsAdapter = new SubstitutionsListAdapter(requireActivity(), inflater, mStoredGame,
                                                                                                  TeamType.GUEST, setIndex);
            guestTeamSubstitutions.setAdapter(guestTeamSubstitutionsAdapter);
        }

        if (mStoredGame.getCalledTimeouts(TeamType.HOME, setIndex).isEmpty() && mStoredGame
                .getCalledTimeouts(TeamType.GUEST, setIndex)
                .isEmpty()) {
            view.findViewById(R.id.set_timeouts_card).setVisibility(View.GONE);
        } else {
            GridView homeTeamTimeouts = view.findViewById(R.id.home_team_timeouts);
            TimeoutsListAdapter homeTeamTimeoutsAdapter = new TimeoutsListAdapter(requireActivity(), inflater, mStoredGame, mStoredGame,
                                                                                  TeamType.HOME, setIndex);
            homeTeamTimeouts.setAdapter(homeTeamTimeoutsAdapter);

            GridView guestTeamTimeouts = view.findViewById(R.id.guest_team_timeouts);
            TimeoutsListAdapter guestTeamTimeoutsAdapter = new TimeoutsListAdapter(requireActivity(), inflater, mStoredGame, mStoredGame,
                                                                                   TeamType.GUEST, setIndex);
            guestTeamTimeouts.setAdapter(guestTeamTimeoutsAdapter);
        }

        if (mStoredGame.getAllSanctions(TeamType.HOME, setIndex).isEmpty() && mStoredGame
                .getAllSanctions(TeamType.GUEST, setIndex)
                .isEmpty()) {
            view.findViewById(R.id.set_sanctions_card).setVisibility(View.GONE);
        } else {
            ListView homeTeamSanctions = view.findViewById(R.id.home_team_sanctions);
            SanctionsListAdapter homeTeamSanctionsAdapter = new SanctionsListAdapter(requireActivity(), inflater, mStoredGame, mStoredGame,
                                                                                     TeamType.HOME, setIndex);
            homeTeamSanctions.setAdapter(homeTeamSanctionsAdapter);

            ListView guestTeamSanctions = view.findViewById(R.id.guest_team_sanctions);
            SanctionsListAdapter guestTeamSanctionsAdapter = new SanctionsListAdapter(requireActivity(), inflater, mStoredGame, mStoredGame,
                                                                                      TeamType.GUEST, setIndex);
            guestTeamSanctions.setAdapter(guestTeamSanctionsAdapter);
        }

        return view;
    }

    @Override
    public void setStoredGame(IStoredGame storedGame) {
        mStoredGame = storedGame;
    }
}
