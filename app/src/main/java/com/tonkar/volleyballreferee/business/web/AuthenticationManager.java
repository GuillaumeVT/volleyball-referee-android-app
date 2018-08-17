package com.tonkar.volleyballreferee.business.web;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import androidx.annotation.NonNull;

public class AuthenticationManager {

    public static GoogleSignInClient getGoogleClient(Activity activity) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestId().requestIdToken(activity.getString(R.string.server_client_id)).build();
        return GoogleSignIn.getClient(activity, gso);
    }

    public static void checkAuthentication(Activity activity) {
        if (PrefUtils.isSyncOn(activity)) {
            Log.i("VBR-Auth", "Check authentication");

            Authentication authentication = PrefUtils.getAuthentication(activity);

            switch (authentication.getProvider()) {
                case GOOGLE:
                    checkAuthenticationGoogle(activity);
                    break;
                case FACEBOOK:
                    checkAuthenticationFacebook(activity);
                    break;
                default:
                    break;
            }
        }
    }

    private static void checkAuthenticationGoogle(final Activity activity) {
        GoogleSignInClient googleSignInClient = getGoogleClient(activity);
        googleSignInClient.silentSignIn().addOnCompleteListener(activity, new OnCompleteListener<GoogleSignInAccount>() {
            @Override
            public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                try {
                    GoogleSignInAccount account = task.getResult(ApiException.class);
                    PrefUtils.signIn(activity, Authentication.of(account.getId(), Authentication.Provider.GOOGLE, account.getIdToken()));
                } catch (ApiException e) {
                    signOut(activity);
                }
            }
        });
    }

    private static void checkAuthenticationFacebook(final Activity activity) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
        if (isLoggedIn) {
            PrefUtils.signIn(activity, Authentication.of(accessToken.getUserId(), Authentication.Provider.FACEBOOK, accessToken.getToken()));
        } else {
            signOut(activity);
        }
    }

    public static void signOut(Activity activity) {
        if (PrefUtils.isSyncOn(activity)) {
            Log.i("VBR-Auth", "Sign out");

            Authentication authentication = PrefUtils.getAuthentication(activity);

            switch (authentication.getProvider()) {
                case GOOGLE:
                    googleSignOut(activity);
                    break;
                case FACEBOOK:
                    facebookSignOut(activity);
                    break;
                default:
                    break;
            }
        }
    }

    private static void googleSignOut(final Activity activity) {
        getGoogleClient(activity).signOut().addOnCompleteListener(activity, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                signedOut(activity);
            }
        });
    }

    private static void facebookSignOut(Activity activity) {
        LoginManager.getInstance().logOut();
        signedOut(activity);
    }

    private static void signedOut(Activity activity) {
        PrefUtils.signOut(activity);
        Toast.makeText(activity, activity.getResources().getString(R.string.user_signed_out), Toast.LENGTH_LONG).show();
        UiUtils.navigateToHome(activity);
    }

    public static void signedIn(Activity activity, Authentication authentication) {
        PrefUtils.signIn(activity, authentication);
        Toast.makeText(activity, String.format(activity.getResources().getString(R.string.user_signed_in), Authentication.Provider.GOOGLE.equals(authentication.getProvider()) ? "Google" : "Facebook"), Toast.LENGTH_LONG).show();
        UiUtils.navigateToUser(activity);
    }

}
