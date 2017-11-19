package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.PositionType;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamListener;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CourtFragment extends Fragment implements NamedGameFragment, TeamClient, TeamListener {

    protected       View                      mView;
    protected       TeamService               mTeamService;
    protected       TeamType                  mTeamOnLeftSide;
    protected       TeamType                  mTeamOnRightSide;
    protected final Map<PositionType, Button> mLeftTeamPositions;
    protected final Map<PositionType, Button> mRightTeamPositions;

    public CourtFragment() {
        mLeftTeamPositions = new HashMap<>();
        mRightTeamPositions = new HashMap<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTeamService.removeTeamListener(this);
        mLeftTeamPositions.clear();
        mRightTeamPositions.clear();
    }

    @Override
    public String getGameFragmentTitle(Context context) {
        return context.getResources().getString(R.string.court_position_tab);
    }

    protected void initView() {
        Log.i("VBR-Court", "Create court fragment");
        setTeamService(ServicesProvider.getInstance().getTeamService());

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        mTeamService.addTeamListener(this);
    }

    protected void addButtonOnLeftSide(final PositionType positionType, final Button button) {
        mLeftTeamPositions.put(positionType, button);
    }

    protected void addButtonOnRightSide(final PositionType positionType, final Button button) {
        mRightTeamPositions.put(positionType, button);
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;

        onTeamRotated(mTeamOnLeftSide);
        onTeamRotated(mTeamOnRightSide);
    }

    @Override
    public void onPlayerChanged(TeamType teamType, int number, PositionType positionType, ActionOriginType actionOriginType) {
        if (PositionType.BENCH.equals(positionType)) {
            onTeamRotated(teamType);
        } else {
            final Map<PositionType, Button> teamPositions;

            if (mTeamOnLeftSide.equals(teamType)) {
                teamPositions = mLeftTeamPositions;
            } else {
                teamPositions = mRightTeamPositions;
            }

            Button button = teamPositions.get(positionType);
            button.setText(String.valueOf(number));
            applyColor(teamType, number, button);
        }
    }

    @Override
    public void onTeamRotated(TeamType teamType) {
        final Map<PositionType, Button> teamPositions;

        if (mTeamOnLeftSide.equals(teamType)) {
            teamPositions = mLeftTeamPositions;
        } else {
            teamPositions = mRightTeamPositions;
        }

        for (final Button button : teamPositions.values()) {
            button.setText("!");
            applyColor(teamType, button);
        }

        final List<Integer> players = mTeamService.getPlayersOnCourt(teamType);

        for (Integer number : players) {
            final PositionType positionType = mTeamService.getPlayerPosition(teamType, number);
            Button button = teamPositions.get(positionType);
            button.setText(String.valueOf(number));
            applyColor(teamType, number, button);
        }
    }

    protected void applyColor(TeamType teamType, Button button) {
        UiUtils.colorTeamButton(mView.getContext(), mTeamService.getTeamColor(teamType), button);
    }

    protected abstract void applyColor(TeamType teamType, int number, Button button);

}
