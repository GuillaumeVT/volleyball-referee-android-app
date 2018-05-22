package com.tonkar.volleyballreferee.ui.user;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class UserSignInActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 101;

    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager    mFacebookCallbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);

        Log.i("VBR-UserSignInActivity", "Create user sign in activity");

        setTitle("");

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestId().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        SignInButton googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        // Facebook
        mFacebookCallbackManager = CallbackManager.Factory.create();

        LoginButton facebookSignInButton = findViewById(R.id.facebook_sign_in_button);
        facebookSignInButton.registerCallback(mFacebookCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                PrefUtils.signIn(UserSignInActivity.this, UserId.userIdOf(loginResult.getAccessToken().getUserId(), UserId.Provider.FACEBOOK));
                Toast.makeText(UserSignInActivity.this, String.format(getResources().getString(R.string.user_signed_in), "facebook"), Toast.LENGTH_LONG).show();
                UiUtils.navigateToUser(UserSignInActivity.this);
            }

            @Override
            public void onCancel() {}

            @Override
            public void onError(FacebookException exception) {
                Log.e("VBR-UserSignInActivity", "Error during facebook sign in");
                Toast.makeText(UserSignInActivity.this, String.format(getResources().getString(R.string.user_sign_in_error), "facebook"), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                UiUtils.navigateToHome(this, false);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void googleSignIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void onGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            PrefUtils.signIn(this, UserId.userIdOf(account.getId(), UserId.Provider.GOOGLE));
            Toast.makeText(this, String.format(getResources().getString(R.string.user_signed_in), "google"), Toast.LENGTH_LONG).show();
            UiUtils.navigateToUser(this);
        } catch (ApiException e) {
            Log.e("VBR-UserSignInActivity", "Error during google sign in", e);
            Toast.makeText(this, String.format(getResources().getString(R.string.user_sign_in_error), "google"), Toast.LENGTH_LONG).show();
        }
    }

}