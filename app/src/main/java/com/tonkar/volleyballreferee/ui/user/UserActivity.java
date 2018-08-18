package com.tonkar.volleyballreferee.ui.user;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.web.WebUtils;
import com.tonkar.volleyballreferee.ui.AuthenticationActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.setup.ScheduledGamesListActivity;

public class UserActivity extends AuthenticationActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        Log.i("VBR-UserActivity", "Create user activity");

        setTitle("");

        ServicesProvider.getInstance().restoreAllServicesAndSync(getApplicationContext());

        initButtonOnClickListeners();
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

    private void initButtonOnClickListeners() {
        findViewById(R.id.goto_account_button).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { goToOnlineAccount(null); }});
        findViewById(R.id.start_scheduled_list_game_button).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { goToScheduledGames(null); }});
        findViewById(R.id.sign_out_button).setOnClickListener(new View.OnClickListener() {@Override public void onClick(View view) { signOut(null); }});
    }

    public void goToScheduledGames(View view) {
        Log.i("VBR-UserActivity", "Go to scheduled games");
        final Intent intent = new Intent(this, ScheduledGamesListActivity.class);
        startActivity(intent);
    }

    public void goToOnlineAccount(View view) {
        Log.i("VBR-UserActivity", "View online account");
        final Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(WebUtils.USER_URL));
        startActivity(intent);
    }

    public void signOut(View view) {
        Log.i("VBR-UserActivity", "Sign out");
        signOut();
    }
}
