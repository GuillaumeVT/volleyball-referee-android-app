package com.tonkar.volleyballreferee.ui.stored.game;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.ui.stored.team.TeamsFragment;

import java.util.ArrayList;
import java.util.List;

public class StoredIndoorGameFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final IStoredGame       mStoredGame;
    private final Context           mContext;
    private       TeamsFragment     mTeamsFragment;
    private final List<SetFragment> mSetFragments;

    StoredIndoorGameFragmentPagerAdapter(IStoredGame storedGame, Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

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
    public @NonNull Fragment getItem(int position) {
        return position == 0 ? mTeamsFragment : mSetFragments.get(position - 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? mContext.getString(R.string.players) : String.format(mContext.getString(R.string.set_number), position);
    }
}
