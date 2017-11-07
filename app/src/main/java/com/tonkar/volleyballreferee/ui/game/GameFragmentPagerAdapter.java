package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.GameService;

public class GameFragmentPagerAdapter extends FragmentPagerAdapter {

    private final GameService         mGameService;
    private final Context             mContext;
    private       IndoorCourtFragment mIndoorCourtFragment;
    private       BeachCourtFragment  mBeachCourtFragment;
    private final ScoresFragment      mScoresFragment;
    private       int                 mCount;

    GameFragmentPagerAdapter(GameService gameService, Context context, FragmentManager fm) {
        super(fm);

        mGameService = gameService;
        mContext = context;
        mScoresFragment = ScoresFragment.newInstance();

        switch (mGameService.getGameType()) {
            case INDOOR:
                if (mGameService.getRules().isTeamOf6Players()) {
                    mIndoorCourtFragment = IndoorCourtFragment.newInstance();
                    mCount = 2;
                } else {
                    mCount = 1;
                }
                break;
            case BEACH:
                mBeachCourtFragment = BeachCourtFragment.newInstance();
                mCount = 2;
                break;
        }
    }

    @Override
    public int getCount() {
        return mCount;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                if (mCount == 1) {
                    fragment = mScoresFragment;
                } else {
                    switch (mGameService.getGameType()) {
                        case INDOOR:
                            fragment = mIndoorCourtFragment;
                            break;
                        case BEACH:
                            fragment = mBeachCourtFragment;
                            break;
                    }
                }
                break;
            case 1:
                fragment = mScoresFragment;
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";

        switch (position) {
            case 0:
                if (mCount == 1) {
                    title = mContext.getResources().getString(R.string.scores_tab);
                } else {
                    title = mContext.getResources().getString(R.string.court_position_tab);
                }
                break;
            case 1:
                title = mContext.getResources().getString(R.string.scores_tab);
                break;
        }

        return title;
    }
}
