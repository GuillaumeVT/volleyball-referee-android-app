package com.tonkar.volleyballreferee.ui.team;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

public class TeamSetupFragmentPagerAdapter extends FragmentPagerAdapter {

    private final Context           mContext;
    private final TeamSetupFragment mHomeTeamSetupFragment;
    private final TeamSetupFragment mGuestTeamSetupFragment;
    private final MiscSetupFragment mMiscSetupFragment;

    TeamSetupFragmentPagerAdapter(Context context, FragmentManager fm) {
        super(fm);

        mContext = context;
        mHomeTeamSetupFragment = TeamSetupFragment.newInstance(TeamType.HOME);
        mGuestTeamSetupFragment = TeamSetupFragment.newInstance(TeamType.GUEST);
        mMiscSetupFragment = MiscSetupFragment.newInstance();
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment = null;

        switch (position) {
            case 0:
                fragment = mHomeTeamSetupFragment;
                break;
            case 1:
                fragment = mGuestTeamSetupFragment;
                break;
            case 2:
                fragment = mMiscSetupFragment;
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
            case 2:
                title = mContext.getResources().getString(R.string.misc_tab);
                break;
        }

        return title;
    }
}