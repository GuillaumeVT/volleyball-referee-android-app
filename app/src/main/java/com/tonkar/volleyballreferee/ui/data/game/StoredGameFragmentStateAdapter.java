package com.tonkar.volleyballreferee.ui.data.game;

import androidx.annotation.NonNull;
import androidx.fragment.app.*;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.tonkar.volleyballreferee.ui.data.rules.RulesFragment;
import com.tonkar.volleyballreferee.ui.data.team.TeamsFragment;

public class StoredGameFragmentStateAdapter extends FragmentStateAdapter {

    private final int mNumberOfSets;

    StoredGameFragmentStateAdapter(FragmentActivity fragmentActivity, int numberOfSets) {
        super(fragmentActivity);
        mNumberOfSets = numberOfSets;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? TeamsFragment.newInstance() : position == 1 ? RulesFragment.newInstance() : SetFragment.newInstance(
                position - 2);
    }

    @Override
    public int getItemCount() {
        return 2 + mNumberOfSets;
    }
}
