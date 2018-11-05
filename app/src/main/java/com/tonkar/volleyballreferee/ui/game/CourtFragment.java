package com.tonkar.volleyballreferee.ui.game;

import android.widget.ImageView;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.View;

import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.ActionOriginType;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionListener;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionService;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamListener;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class CourtFragment extends Fragment implements TeamListener, SanctionListener {

    protected       View                              mView;
    protected       TeamService                       mTeamService;
    protected       SanctionService                   mSanctionService;
    protected       TeamType                          mTeamOnLeftSide;
    protected       TeamType                          mTeamOnRightSide;
    protected final Map<PositionType, MaterialButton> mLeftTeamPositions;
    protected final Map<PositionType, MaterialButton> mRightTeamPositions;
    protected final Map<PositionType, ImageView>      mLeftTeamSanctionImages;
    protected final Map<PositionType, ImageView>      mRightTeamSanctionImages;

    public CourtFragment() {
        mLeftTeamPositions = new HashMap<>();
        mRightTeamPositions = new HashMap<>();
        mLeftTeamSanctionImages = new HashMap<>();
        mRightTeamSanctionImages = new HashMap<>();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTeamService.removeTeamListener(this);
        mSanctionService.removeSanctionListener(this);
        mLeftTeamPositions.clear();
        mRightTeamPositions.clear();
        mLeftTeamSanctionImages.clear();
        mRightTeamSanctionImages.clear();
    }

    protected void initView() {
        Log.i(Tags.GAME_UI, "Create court fragment");
        mTeamService = ServicesProvider.getInstance().getTeamService();
        mSanctionService = ServicesProvider.getInstance().getSanctionService();

        mTeamOnLeftSide = mTeamService.getTeamOnLeftSide();
        mTeamOnRightSide = mTeamService.getTeamOnRightSide();
        mTeamService.addTeamListener(this);
        mSanctionService.addSanctionListener(this);
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

    @Override
    public void onTeamsSwapped(TeamType leftTeamType, TeamType rightTeamType, ActionOriginType actionOriginType) {
        mTeamOnLeftSide = leftTeamType;
        mTeamOnRightSide = rightTeamType;
    }

    protected void updateSanction(TeamType teamType, int number, ImageView sanctionImage) {
        if (mSanctionService.hasSanctions(teamType, number)) {
            SanctionType sanctionType = mSanctionService.getMostSeriousSanction(teamType, number);
            UiUtils.setSanctionImage(sanctionImage, sanctionType);
            sanctionImage.setVisibility(View.VISIBLE);
        } else {
            sanctionImage.setVisibility(View.INVISIBLE);
        }
    }

}
