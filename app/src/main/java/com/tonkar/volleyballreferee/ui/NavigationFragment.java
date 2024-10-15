package com.tonkar.volleyballreferee.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.*;

import androidx.annotation.IdRes;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.navigation.NavigationView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class NavigationFragment extends Fragment {

    public NavigationFragment() {
    }

    public static NavigationFragment newInstance() {
        return new NavigationFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_navigation, container, false);

        NavigationView navigationView = fragmentView.findViewById(R.id.main_navigation_drawer_view);

        navigationView.setNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_available_games) {
                navigateToFragment(R.id.scheduled_games_list_fragment);
            } else if (itemId == R.id.action_stored_games) {
                navigateToFragment(R.id.stored_games_list_fragment);
            } else if (itemId == R.id.action_stored_teams) {
                navigateToFragment(R.id.stored_teams_list_fragment);
            } else if (itemId == R.id.action_stored_rules) {
                navigateToFragment(R.id.stored_rules_list_fragment);
            } else if (itemId == R.id.action_colleagues) {
                navigateToFragment(R.id.colleagues_list_fragment);
            } else if (itemId == R.id.action_settings) {
                navigateToFragment(R.id.settings_fragment);
            } else if (itemId == R.id.action_live_games_vbr_com) {
                Intent intent = new Intent(Intent.ACTION_VIEW,
                                           Uri.parse(String.format("%s/search?type=live", PrefUtils.getServerUrl(requireContext()))));
                startActivity(intent);
                UiUtils.animateForward(requireActivity());
            } else if (itemId == R.id.action_credits) {
                navigateToFragment(R.id.credit_fragment);
            }

            return true;
        });

        boolean onlineFeaturesVisible = PrefUtils.canSync(requireContext());
        navigationView.getMenu().findItem(R.id.action_available_games).setVisible(onlineFeaturesVisible);
        navigationView.getMenu().findItem(R.id.action_colleagues).setVisible(onlineFeaturesVisible);
        navigationView.getMenu().findItem(R.id.action_live_games_vbr_com).setVisible(onlineFeaturesVisible);

        return fragmentView;
    }

    private void navigateToFragment(@IdRes int fragmentId) {
        NavHostFragment navigationHostFragment = (NavHostFragment) requireActivity()
                .getSupportFragmentManager()
                .findFragmentById(R.id.main_container_view);
        if (navigationHostFragment != null) {
            NavController navigationController = navigationHostFragment.getNavController();
            navigationController.navigate(fragmentId);
        }
    }
}