package com.tonkar.volleyballreferee.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.widget.TextView;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.ui.AuthenticationActivity;
import com.tonkar.volleyballreferee.ui.setup.ScheduledGamesListActivity;

import java.util.Locale;

public class UserActivity extends AuthenticationActivity {

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.view_online_account);
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_view_account;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.i(Tags.WEB, "Create user activity");

        initNavigationMenu();

        TextView userIdText = findViewById(R.id.user_id_text);
        userIdText.setText(String.format(Locale.getDefault(), getResources().getString(R.string.user_signed_in), PrefUtils.getAuthentication(this).getUserId()));

        initButtonOnClickListeners();
    }

    private void initButtonOnClickListeners() {
        findViewById(R.id.goto_account_button).setOnClickListener(button -> goToOnlineAccount(null));
        findViewById(R.id.start_scheduled_list_game_button).setOnClickListener(button -> goToScheduledGames(null));
        findViewById(R.id.sign_out_button).setOnClickListener(button -> signOut(null));
    }

    public void goToScheduledGames(View view) {
        Log.i(Tags.WEB, "Go to scheduled games");
        final Intent intent = new Intent(this, ScheduledGamesListActivity.class);
        startActivity(intent);
    }

    public void goToOnlineAccount(View view) {
        Log.i(Tags.WEB, "View online account");
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebUtils.USER_URL));
        startActivity(intent);
    }

    public void signOut(View view) {
        Log.i(Tags.WEB, "Sign out");
        signOut();
    }
}
