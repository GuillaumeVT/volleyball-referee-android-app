package com.tonkar.volleyballreferee.ui.stored;

import android.content.Context;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;

import java.util.ArrayList;
import java.util.List;

public class StoredIndoorGameFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final IStoredGame       mStoredGame;
    private final Context           mContext;
    private       TeamsFragment     mTeamsFragment;
    private final List<SetFragment> mSetFragments;

    StoredIndoorGameFragmentPagerAdapter(IStoredGame storedGame, Context context, FragmentManager fm) {
        super(fm);

        mStoredGame = storedGame;
        mContext = context;
        mTeamsFragment = TeamsFragment.newInstance();
        mSetFragments = new ArrayList<>();

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            SetFragment setFragment = SetFragment.newInstance(setIndex);
            mSetFragments.add(setFragment);
        }
    }

    @Override
    public int getCount() {
        return 1 + mStoredGame.getNumberOfSets();
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
