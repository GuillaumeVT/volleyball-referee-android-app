package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.data.UserId;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class UserActivity extends AppCompatActivity {

    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.i("VBR-UserActivity", "Create user activity");

        setTitle("");

        // Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).requestId().build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    public void goToScheduledGames(View view) {
        Log.i("VBR-UserActivity", "Go to scheduled games");

        /*GameFactory.createPointBasedGame(refereeName, UserId.VBR_USER_ID);

        Log.i("VBR-MainActivity", "Start activity to setup teams quickly");
        final Intent intent = new Intent(this, QuickTeamsSetupActivity.class);
        startActivity(intent);*/
    }

    public void signOut(View view) {
        Log.i("VBR-UserActivity", "Sign out");

        String userId = PrefUtils.getUserId(this);

        if (UserId.isGoogle(userId)) {
            googleSignOut();
        } else if (UserId.isFacebook(userId)) {
            facebookSignOut();
        }
    }

    private void googleSignOut() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                signOut();
            }
        });
    }

    private void facebookSignOut() {
        LoginManager.getInstance().logOut();
        signOut();
    }

    private void signOut() {
        PrefUtils.signOut(UserActivity.this);
        Toast.makeText(UserActivity.this, getResources().getString(R.string.user_signed_out), Toast.LENGTH_LONG).show();
        UiUtils.navigateToHome(UserActivity.this);
    }
}
