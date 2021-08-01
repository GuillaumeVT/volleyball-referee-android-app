package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.AsyncUserRequestListener;
import com.tonkar.volleyballreferee.engine.stored.StoredUserManager;
import com.tonkar.volleyballreferee.engine.stored.StoredUserService;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserSummary;
import com.tonkar.volleyballreferee.engine.stored.api.ApiUserToken;
import com.tonkar.volleyballreferee.ui.util.ProgressIndicatorActivity;

import java.net.HttpURLConnection;

public class UserActivity extends ProgressIndicatorActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.i(Tags.USER_UI, "Create user activity");

        setTitle(getString(R.string.my_account));

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mSyncLayout = findViewById(R.id.user_sync_layout);
        mSyncLayout.setEnabled(false);

        if (PrefUtils.isSignedIn(this)) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, UserAccountFragment.newInstance()).commit();
        } else if (PrefUtils.shouldSignIn(this)) {
            showProgressIndicator();
            StoredUserService storedUserService = new StoredUserManager(this);
            storedUserService.getUser(PrefUtils.getWebPremiumBillingToken(this), new AsyncUserRequestListener() {
                @Override
                public void onUserReceived(ApiUserSummary user) {
                    runOnUiThread(() -> {
                        hideProgressIndicator();
                        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, UserSignInFragment.newInstance()).commit();
                    });
                }

                @Override
                public void onUserTokenReceived(ApiUserToken userToken) {}

                @Override
                public void onUserPasswordRecoveryInitiated() {}

                @Override
                public void onError(int httpCode) {
                    runOnUiThread(() -> {
                        hideProgressIndicator();
                        if (httpCode == HttpURLConnection.HTTP_NOT_FOUND) {
                            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, UserSignUpFragment.newInstance()).commit();
                        }
                    });
                }
            });
        }
    }

}
