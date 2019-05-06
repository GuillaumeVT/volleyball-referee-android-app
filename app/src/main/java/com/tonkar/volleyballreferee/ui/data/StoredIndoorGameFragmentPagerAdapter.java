package com.tonkar.volleyballreferee.ui.data;

import android.content.Context;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.data.StoredGameService;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class StoredIndoorGameFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final StoredGameService mStoredGameService;
    private final Context           mContext;
    private       TeamsFragment     mTeamsFragment;
    private final List<SetFragment> mSetFragments;

    StoredIndoorGameFragmentPagerAdapter(StoredGameService storedGameService, Context context, FragmentManager fm) {
        super(fm);

        mStoredGameService = storedGameService;
        mContext = context;
        mTeamsFragment = TeamsFragment.newInstance();
        mSetFragments = new ArrayList<>();

        for (int setIndex = 0; setIndex < mStoredGameService.getNumberOfSets(); setIndex++) {
            SetFragment setFragment = SetFragment.newInstance(setIndex);
            mSetFragments.add(setFragment);
        }
    }

    @Override
    public int getCount() {
        return 1 + mStoredGameService.getNumberOfSets();
    }

    @Override
    public Fragment getItem(int position) {
        Fragment fragment;

        switch (position) {
            case 0:
                fragment = mTeamsFragment;
                break;
            default:
                fragment = mSetFragments.get(position - 1);
                break;
        }

        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        final String title;

        if (position == 0) {
            title = mContext.getString(R.string.players);
        } else {
            title = String.format(mContext.getString(R.string.set_number), position);
        }

        return title;
    }
}
