package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.service.*;
import com.tonkar.volleyballreferee.ui.billing.PurchaseListFragment;

import java.net.HttpURLConnection;

public class UserFragment extends Fragment {

    protected SwipeRefreshLayout mSyncLayout;

    public UserFragment() {}

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_user, container, false);

        mSyncLayout = fragmentView.findViewById(R.id.user_sync_progress_layout);
        mSyncLayout.setEnabled(false);

        if (PrefUtils.isSignedIn(requireContext())) {
            replaceFragment(UserAccountFragment.newInstance());
        } else if (PrefUtils.shouldSignIn(requireContext())) {
            showProgressIndicator();
            StoredUserService storedUserService = new StoredUserManager(requireContext());
            storedUserService.getUser(PrefUtils.getWebPremiumBillingToken(requireContext()), new AsyncUserRequestListener() {
                @Override
                public void onUserReceived(ApiUserSummary user) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            hideProgressIndicator();
                            replaceFragment(UserSignInFragment.newInstance());
                        });
                    }
                }

                @Override
                public void onUserTokenReceived(ApiUserToken userToken) {}

                @Override
                public void onUserPasswordRecoveryInitiated() {}

                @Override
                public void onError(int httpCode) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            hideProgressIndicator();
                            if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
                                replaceFragment(UserSignUpFragment.newInstance());
                            }
                        });
                    }
                }
            });
        } else if (!PrefUtils.isWebPremiumSubscribed(requireContext()) && !PrefUtils.isWebPremiumPurchased(requireContext())) {
            replaceFragment(PurchaseListFragment.newInstance());
        }

        return fragmentView;
    }

    public void showProgressIndicator() {
        mSyncLayout.setRefreshing(true);
    }

    public void hideProgressIndicator() {
        mSyncLayout.setRefreshing(false);
    }

    private void replaceFragment(Fragment fragment) {
        getChildFragmentManager().beginTransaction().replace(R.id.user_container_view, fragment).commitAllowingStateLoss();
    }
}