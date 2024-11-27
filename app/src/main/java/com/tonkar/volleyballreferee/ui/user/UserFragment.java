package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;

public class UserFragment extends Fragment {

    public UserFragment() {}

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_user, container, false);

        if (PrefUtils.isSignedIn(requireContext())) {
            replaceFragment(UserAccountFragment.newInstance());
        } else if (PrefUtils.hasServerUrl(requireContext())) {
            replaceFragment(UserSignInFragment.newInstance());
        }

        return fragmentView;
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.user_container_view, fragment).commitAllowingStateLoss();
    }
}