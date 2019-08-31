package com.tonkar.volleyballreferee.ui.stored.game;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.ui.stored.rules.RulesFragment;
import com.tonkar.volleyballreferee.ui.stored.team.TeamsFragment;

import java.util.ArrayList;
import java.util.List;

public class StoredGameFragmentPagerAdapter extends FragmentStatePagerAdapter {

    private final IStoredGame       mStoredGame;
    private final Context           mContext;
    private       TeamsFragment     mTeamsFragment;
    private final List<SetFragment> mSetFragments;
    private       RulesFragment     mRulesFragment;

    StoredGameFragmentPagerAdapter(IStoredGame storedGame, Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);

        mStoredGame = storedGame;
        mContext = context;
        mTeamsFragment = TeamsFragment.newInstance();
        mRulesFragment = RulesFragment.newInstance();
        mSetFragments = new ArrayList<>();

        for (int setIndex = 0; setIndex < mStoredGame.getNumberOfSets(); setIndex++) {
            SetFragment setFragment = SetFragment.newInstance(setIndex);
            mSetFragments.add(setFragment);
        }
    }

    @Override
    public int getCount() {
        return 2 + mStoredGame.getNumberOfSets();
    }

    @Override
    public @NonNull Fragment getItem(int position) {
        return position == 0 ? mTeamsFragment : position == 1 ? mRulesFragment : mSetFragments.get(position - 2);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return position == 0 ? mContext.getString(R.string.players) : position == 1 ? mContext.getString(R.string.rules) : String.format(mContext.getString(R.string.set_number), position - 1);
    }
}
