package com.tonkar.volleyballreferee.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.tonkar.volleyballreferee.BuildConfig;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.ui.billing.PurchasesListActivity;
import com.tonkar.volleyballreferee.ui.data.game.StoredGamesListActivity;
import com.tonkar.volleyballreferee.ui.data.rules.StoredRulesListActivity;
import com.tonkar.volleyballreferee.ui.data.team.StoredTeamsListActivity;
import com.tonkar.volleyballreferee.ui.setup.ScheduledGamesListActivity;
import com.tonkar.volleyballreferee.ui.user.ColleaguesListActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public abstract class NavigationActivity extends AppCompatActivity {

    protected DrawerLayout mDrawerLayout;

    protected abstract String getToolbarTitle();

    protected abstract int getCheckedItem();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void initNavigationMenu() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getToolbarTitle());
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.navigation_view);
        navigationView.getMenu().findItem(getCheckedItem()).setChecked(true);

        computeWebItemVisibility(navigationView);

        navigationView.setNavigationItemSelectedListener(item -> {
            if (getCheckedItem() != item.getItemId()) {
                switch (item.getItemId()) {
                    case R.id.action_home:
                        Log.i(Tags.MAIN_UI, "Home");
                        UiUtils.navigateToHome(this, R.anim.slide_in_right, R.anim.slide_out_left);
                        break;
                    case R.id.action_purchase:
                        Log.i(Tags.BILLING, "Purchase");
                        Intent intent = new Intent(this, PurchasesListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_stored_rules:
                        Log.i(Tags.STORED_RULES, "Stored Rules");
                        intent = new Intent(this, StoredRulesListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_settings:
                        Log.i(Tags.SETTINGS, "Settings");
                        intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_stored_games:
                        Log.i(Tags.STORED_GAMES, "Stored games");
                        intent = new Intent(this, StoredGamesListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_stored_teams:
                        Log.i(Tags.STORED_TEAMS, "Stored teams");
                        intent = new Intent(this, StoredTeamsListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_available_games:
                        if (PrefUtils.canSync(this)) {
                            Log.i(Tags.SCHEDULE_UI, "Scheduled games");
                            intent = new Intent(this, ScheduledGamesListActivity.class);
                            startActivity(intent);
                            UiUtils.animateForward(this);
                        }
                        break;
                    case R.id.action_colleagues:
                        if (PrefUtils.canSync(this)) {
                            Log.i(Tags.USER_UI, "Colleagues");
                            intent = new Intent(this, ColleaguesListActivity.class);
                            startActivity(intent);
                            UiUtils.animateForward(this);
                        }
                        break;
                    case R.id.action_live_games_vbr_com:
                        Log.i(Tags.WEB, "Live games on VBR.com");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s/search?type=live", BuildConfig.SERVER_ADDRESS)));
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        navigationView.post(() -> {
                            navigationView.getMenu().findItem(R.id.action_live_games_vbr_com).setChecked(false);
                            navigationView.getMenu().findItem(getCheckedItem()).setChecked(true);
                        });
                        break;
                    case R.id.action_credits:
                        Log.i(Tags.MAIN_UI, "Credits");
                        intent = new Intent(this, CreditsActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_privacy_policy:
                        Log.i(Tags.WEB, "Privacy policy on VBR.com");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s/privacy-policy", BuildConfig.SERVER_ADDRESS)));
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        navigationView.post(() -> {
                            navigationView.getMenu().findItem(R.id.action_privacy_policy).setChecked(false);
                            navigationView.getMenu().findItem(getCheckedItem()).setChecked(true);
                        });
                        break;
                }
            }
            mDrawerLayout.closeDrawers();
            return true;
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Log.i(Tags.MAIN_UI, "Drawer");
            if (isNavigationDrawerOpen()) {
                mDrawerLayout.closeDrawers();
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (isNavigationDrawerOpen()) {
            mDrawerLayout.closeDrawers();
        } else {
            if (this instanceof MainActivity) {
                super.onBackPressed();
            } else {
                UiUtils.navigateToHome(this);
            }
        }
    }

    private boolean isNavigationDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void computeWebItemVisibility(NavigationView navigationView) {
        boolean canSync = PrefUtils.canSync(this);
        navigationView.getMenu().findItem(R.id.action_available_games).setVisible(canSync);
        navigationView.getMenu().findItem(R.id.action_colleagues).setVisible(canSync);
    }
}
