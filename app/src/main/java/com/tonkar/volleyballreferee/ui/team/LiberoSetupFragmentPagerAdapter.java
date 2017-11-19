package com.tonkar.volleyballreferee.ui.team;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.TeamClient;
import com.tonkar.volleyballreferee.interfaces.TeamService;
import com.tonkar.volleyballreferee.interfaces.TeamType;

public class LiberoSetupFragmentPagerAdapter extends FragmentPagerAdapter implements TeamClient {

    private LiberoSetupFragment mHomeTeamLiberoSetupFragment;
    private LiberoSetupFragment mGuestTeamLiberoSetupFragment;
    private TeamService         mTeamService;

    LiberoSetupFragmentPagerAdapter(FragmentManager fm) {
        super(fm);

        setTeamService(ServicesProvider.getInstance().getTeamService());
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
                title = mTeamService.getTeamName(TeamType.HOME);
                break;
            case 1:
                title = mTeamService.getTeamName(TeamType.GUEST);
                break;
        }

        return title;
    }

    @Override
    public void setTeamService(TeamService teamService) {
        mTeamService = teamService;
    }
}