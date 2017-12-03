package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.preference.PreferenceManager;

import com.tonkar.volleyballreferee.interfaces.ScoreService;

public class GameFragmentPagerAdapter extends FragmentPagerAdapter {

    private final ScoreService          mScoreService;
    private final Context               mContext;
    private       IndoorCourtFragment   mIndoorCourtFragment;
    private       BeachCourtFragment    mBeachCourtFragment;
    private final ScoresFragment        mScoresFragment;
    private       SubstitutionsFragment mSubstitutionsFragment;
    private       int                   mCount;

    GameFragmentPagerAdapter(ScoreService scoreService, Context context, FragmentManager fm) {
        super(fm);

        mScoreService = scoreService;
        mContext = context;
        mScoresFragment = ScoresFragment.newInstance();

        switch (mScoreService.getGameType()) {
            case INDOOR:
                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                boolean normalUsageSetting = Boolean.parseBoolean(sharedPreferences.getString("pref_application_usage", String.valueOf(true)));
                if (normalUsageSetting) {
                    mIndoorCourtFragment = IndoorCourtFragment.newInstance();
                    mSubstitutionsFragment = SubstitutionsFragment.newInstance();
                    mCount = 3;
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
                    switch (mScoreService.getGameType()) {
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
            case 2:
                fragment = mSubstitutionsFragment;
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ((NamedGameFragment) getItem(position)).getGameFragmentTitle(mContext);
    }
}
