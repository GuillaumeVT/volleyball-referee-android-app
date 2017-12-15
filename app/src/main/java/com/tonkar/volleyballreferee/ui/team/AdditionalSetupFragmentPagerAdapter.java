package com.tonkar.volleyballreferee.ui.team;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class AdditionalSetupFragmentPagerAdapter extends FragmentPagerAdapter {

    private AdditionalSetupFragment mHomeTeamAdditionalSetupFragment;
    private AdditionalSetupFragment mGuestTeamAdditionalSetupFragment;

    AdditionalSetupFragmentPagerAdapter(FragmentManager fm) {
        super(fm);

        mHomeTeamAdditionalSetupFragment = AdditionalSetupFragment.newInstance(TeamType.HOME);
        mGuestTeamAdditionalSetupFragment = AdditionalSetupFragment.newInstance(TeamType.GUEST);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = mHomeTeamAdditionalSetupFragment;
                break;
            case 1:
                fragment = mGuestTeamAdditionalSetupFragment;
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";

        switch (position) {
            case 0:
                title = ServicesProvider.getInstance().getTeamService().getTeamName(TeamType.HOME);
                break;
            case 1:
                title = ServicesProvider.getInstance().getTeamService().getTeamName(TeamType.GUEST);
                break;
        }

        return title;
    }

}