package com.tonkar.volleyballreferee.ui.game.court;

import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.*;
import com.tonkar.volleyballreferee.engine.game.score.ScoreListener;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.ui.interfaces.GameServiceHandler;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.*;

public abstract class CourtFragment extends Fragment implements ScoreListener, TeamListener, SanctionListener, GameServiceHandler {

    protected       View                              mView;
    protected       IGame                             mGame;
    protected       TeamType                          mTeamOnLeftSide;
    protected       TeamType                          mTeamOnRightSide;
    protected final Map<PositionType, MaterialButton> mLeftTeamPositions;
    protected final Map<PositionType, MaterialButton> mRightTeamPositions;
    protected final Map<PositionType, ImageView>      mLeftTeamSanctionImages;
    protected final Map<PositionType, ImageView>      mRightTeamSanctionImages;
    protected final Map<PositionType, TextView>       mLeftTeamSubstitutions;
    protected final Map<PositionType, TextView>       mRightTeamSubstitutions;

    CourtFragment() {
        mLeftTeamPositions = new HashMap<>();
        mRightTeamPositions = new HashMap<>();
        mLeftTeamSanctionImages = new HashMap<>();
        mRightTeamSanctionImages = new HashMap<>();
        mLeftTeamSubstitutions = new HashMap<>();
        mRightTeamSubstitutions = new HashMap<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        if (mGame != null) {
            mGame.removeScoreListener(this);
            mGame.removeTeamListener(this);
            mGame.removeSanctionListener(this);
            mLeftTeamPositions.clear();
            mRightTeamPositions.clear();
            mLeftTeamSanctionImages.clear();
            mRightTeamSanctionImages.clear();
            mLeftTeamSubstitutions.clear();
            mRightTeamSubstitutions.clear();
        }
    }

    protected void initView() {
        Log.i(Tags.GAME_UI, "Create court fragment");

        if (mGame != null) {
            mTeamOnLeftSide = mGame.getTeamOnLeftSide();
            mTeamOnRightSide = mGame.getTeamOnRightSide();
            mGame.addScoreListener(this);
            mGame.addTeamListener(this);
            mGame.addSanctionListener(this);
        }
    }

    protected void addButtonOnLeftSide(final PositionType positionType, final MaterialButton button) {
        mLeftTeamPositions.put(positionType, button);
    }

    protected void addButtonOnRightSide(final PositionType positionType, final MaterialButton button) {
        mRightTeamPositions.put(positionType, button);
    }

    protected void addSanctionImageOnLeftSide(final PositionType positionType, final ImageView sanctionImage) {
        mLeftTeamSanctionImages.put(positionType, sanctionImage);
    }

    protected void addSanctionImageOnRightSide(final PositionType positionType, final ImageView sanctionImage) {
        mRightTeamSanctionImages.put(positionType, sanctionImage);
    }

    protected void addSubstitutionOnLeftSide(final PositionType positionType, final TextView substitutionView) {
        mLeftTeamSubstitutions.put(positionType, substitutionView);
    }

    protected void addSubstitutionOnRightSide(final PositionType positionType, final TextView substitutionView) {
        mRightTeamSubstitutions.put(positionType, substitutionView);
    }

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;
    }

    @Override
    public void onStartingLineupSubmitted(TeamType teamType) {}

    @Override
    public void onPointsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onSetsUpdated(TeamType teamType, int newCount) {}

    @Override
    public void onSetStarted() {}

    @Override
    public void onSetCompleted() {}

    @Override
    public void onMatchCompleted(final TeamType winner) {}

    protected void updateSanction(TeamType teamType, int number, ImageView sanctionImage) {
        if (mGame.hasSanctions(teamType, number)) {
            SanctionType sanctionType = mGame.getMostSeriousSanction(teamType, number);
            UiUtils.setSanctionImage(sanctionImage, sanctionType);
            sanctionImage.setVisibility(View.VISIBLE);
        } else {
            sanctionImage.setVisibility(View.INVISIBLE);
        }
    }

}
