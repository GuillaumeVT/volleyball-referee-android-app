package com.tonkar.volleyballreferee.ui.game;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.UsageType;

import java.util.ArrayList;
import java.util.List;

public class GameFragmentPagerAdapter extends FragmentPagerAdapter {

    private final Context               mContext;
    private final List<Fragment>        mFragments;

    GameFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        mContext = context;
        mFragments = new ArrayList<>();

        switch (ServicesProvider.getInstance().getScoreService().getGameType()) {
            case INDOOR:
                if (UsageType.NORMAL.equals(ServicesProvider.getInstance().getGameService().getUsageType())) {
                    mFragments.add(IndoorCourtFragment.newInstance());
                    mFragments.add(LaddersFragment.newInstance());
                    mFragments.add(SubstitutionsFragment.newInstance());
                } else {
                    mFragments.add(LaddersFragment.newInstance());
                }
                break;
            case BEACH:
                mFragments.add(BeachCourtFragment.newInstance());
                mFragments.add(LaddersFragment.newInstance());
                break;
        }

        if (ServicesProvider.getInstance().getGameService().getRules().areTeamTimeoutsEnabled()) {
            mFragments.add(TimeoutsFragment.newInstance());
        }

        if (ServicesProvider.getInstance().getGameService().getRules().arePenaltyCardsEnabled()) {
            mFragments.add(PenaltyCardsFragment.newInstance());
        }
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return ((NamedGameFragment) getItem(position)).getGameFragmentTitle(mContext);
    }

}
