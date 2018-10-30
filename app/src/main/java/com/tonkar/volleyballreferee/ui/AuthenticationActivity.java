package com.tonkar.volleyballreferee.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.web.Authentication;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Collections;

public abstract class AuthenticationActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager    mFacebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestId().requestIdToken(getString(R.string.server_client_id)).build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Facebook
        mFacebookCallbackManager = CallbackManager.Factory.create();
    }

    protected void checkAuthentication() {
        if (PrefUtils.isSyncOn(this)) {
            Log.i(Tags.WEB, "Check authentication");

            Authentication authentication = PrefUtils.getAuthentication(this);

            switch (authentication.getProvider()) {
                case GOOGLE:
                    checkAuthenticationGoogle();
                    break;
                case FACEBOOK:
                    checkAuthenticationFacebook();
                    break;
                default:
                    break;
            }
        } else if (PrefUtils.shouldSignIn(this)) {
            UiUtils.navigateToUserSignIn(this);
        }
    }

    private void checkAuthenticationGoogle() {
        mGoogleSignInClient.silentSignIn().addOnCompleteListener(this, task -> {
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                PrefUtils.signIn(AuthenticationActivity.this, Authentication.of(account.getId(), Authentication.Provider.GOOGLE, account.getIdToken()));
            } catch (ApiException e) {
                googleSignOut(true);
            }
        });
    }

    private void checkAuthenticationFacebook() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            PrefUtils.signIn(this, Authentication.of(accessToken.getUserId(), Authentication.Provider.FACEBOOK, accessToken.getToken()));
        } else {
            facebookSignOut(true);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            onGoogleSignInResult(task);
        } else {
            mFacebookCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    protected void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            signedIn(Authentication.of(account.getId(), Authentication.Provider.GOOGLE, account.getIdToken()));
        } catch (ApiException e) {
            Log.e(Tags.WEB, "Error during Google sign in", e);
            UiUtils.makeText(this, String.format(getResources().getString(R.string.user_sign_in_error), "Google"), Toast.LENGTH_LONG).show();
        }
    }

    protected void facebookSignIn() {
        LoginManager.getInstance().logInWithReadPermissions(this, Collections.singletonList("public_profile"));
        LoginManager.getInstance().registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                signedIn(Authentication.of(loginResult.getAccessToken().getUserId(), Authentication.Provider.FACEBOOK, loginResult.getAccessToken().getToken()));
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException exception) {
                Log.e(Tags.WEB, "Error during Facebook sign in");
                UiUtils.makeText(AuthenticationActivity.this, String.format(getResources().getString(R.string.user_sign_in_error), "Facebook"), Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void signOut() {
        if (PrefUtils.isSyncOn(this)) {
            Log.i(Tags.WEB, "Sign out");

            Authentication authentication = PrefUtils.getAuthentication(this);

            switch (authentication.getProvider()) {
                case GOOGLE:
                    googleSignOut(false);
                    break;
                case FACEBOOK:
                    facebookSignOut(false);
                    break;
                default:
                    break;
            }
        }
    }

    private void googleSignOut(final boolean andSignInAgain) {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            signedOut(andSignInAgain);
            if (andSignInAgain) {
                googleSignIn();
            }
        });
    }

    private void facebookSignOut(final boolean andSignInAgain) {
        LoginManager.getInstance().logOut();
        signedOut(andSignInAgain);
        if (andSignInAgain) {
            facebookSignIn();
        }
    }

    private void signedOut(final boolean andSignInAgain) {
        PrefUtils.signOut(this);
        if (!andSignInAgain) {
            UiUtils.makeText(this, getResources().getString(R.string.user_signed_out), Toast.LENGTH_LONG).show();
            UiUtils.navigateToHome(this, false);
        }
    }

    public void signedIn(Authentication authentication) {
        PrefUtils.signIn(this, authentication);
        UiUtils.makeText(this, String.format(getResources().getString(R.string.user_signed_in), Authentication.Provider.GOOGLE.equals(authentication.getProvider()) ? "Google" : "Facebook"), Toast.LENGTH_LONG).show();
        UiUtils.navigateToHome(this, false);
    }

}
