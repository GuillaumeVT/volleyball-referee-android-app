package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class LiberoSetupFragmentPagerAdapter extends FragmentPagerAdapter {

    private final Context               mContext;
    private       LiberoSetupFragment   mHomeTeamLiberoSetupFragment;
    private       LiberoSetupFragment   mGuestTeamLiberoSetupFragment;

    LiberoSetupFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        mContext = context;
        mHomeTeamLiberoSetupFragment = LiberoSetupFragment.newInstance(TeamType.HOME);
        mGuestTeamLiberoSetupFragment = LiberoSetupFragment.newInstance(TeamType.GUEST);
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
                fragment = mHomeTeamLiberoSetupFragment;
                break;
            case 1:
                fragment = mGuestTeamLiberoSetupFragment;
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        String title = "";

        switch (position) {
            case 0:
                title = mContext.getResources().getString(R.string.home_team_tab);
                break;
            case 1:
                title = mContext.getResources().getString(R.string.guest_team_tab);
                break;
        }

        return title;
    }
}