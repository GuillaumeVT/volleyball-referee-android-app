package com.tonkar.volleyballreferee.ui.user;

import android.os.Bundle;
import android.util.Log;

import android.widget.Button;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.ui.AuthenticationActivity;

public class UserSignInActivity extends AuthenticationActivity {

    @Override
    protected String getToolbarTitle() {
        return "";
    }

    @Override
    protected int getCheckedItem() {
        return 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_sign_in);

        Log.i(Tags.WEB, "Create user sign in activity");

        setTitle(getString(R.string.my_account));

        Button googleSignInButton = findViewById(R.id.google_sign_in_button);
        googleSignInButton.setOnClickListener(button -> googleSignIn());

        Button facebookSignInButton = findViewById(R.id.facebook_sign_in_button);
        facebookSignInButton.setOnClickListener(button -> facebookSignIn());
    }

}
