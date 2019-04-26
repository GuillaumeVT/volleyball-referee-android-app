package com.tonkar.volleyballreferee.ui;

import android.content.ActivityNotFoundException;
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
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.business.billing.BillingManager;
import com.tonkar.volleyballreferee.api.ApiUtils;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.billing.BillingService;
import com.tonkar.volleyballreferee.ui.billing.PurchasesListActivity;
import com.tonkar.volleyballreferee.ui.data.RecordedGamesListActivity;
import com.tonkar.volleyballreferee.ui.data.SavedRulesListActivity;
import com.tonkar.volleyballreferee.ui.data.SavedTeamsListActivity;
import com.tonkar.volleyballreferee.ui.setup.ScheduledGamesListActivity;
import com.tonkar.volleyballreferee.ui.user.UserSignInActivity;
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

        computeItemsVisibility(navigationView);
        computePurchaseItemVisibility(navigationView.getMenu().findItem(R.id.action_purchase));

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
                    case R.id.action_saved_rules:
                        Log.i(Tags.STORED_RULES, "Saved Rules");
                        intent = new Intent(this, SavedRulesListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_settings:
                        Log.i(Tags.SETTINGS, "Settings");
                        intent = new Intent(this, SettingsActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_recorded_games:
                        Log.i(Tags.STORED_GAMES, "Recorded games");
                        intent = new Intent(this, RecordedGamesListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_saved_teams:
                        Log.i(Tags.STORED_TEAMS, "Saved teams");
                        intent = new Intent(this, SavedTeamsListActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_view_scheduled_games:
                        if (PrefUtils.isWebPremiumPurchased(this)) {
                            if (PrefUtils.isSignedIn(this)) {
                                Log.i(Tags.SCHEDULE_UI, "Scheduled games");
                                intent = new Intent(this, ScheduledGamesListActivity.class);
                            } else {
                                Log.i(Tags.WEB, "User sign in");
                                UiUtils.navigateToUserSignIn(this);
                                intent = new Intent(this, UserSignInActivity.class);
                            }
                            startActivity(intent);
                            UiUtils.animateForward(this);
                        }
                        break;
                    case R.id.action_view_live_games:
                        Log.i(Tags.WEB, "Live games");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiUtils.LIVE_URL));
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_search_online_games:
                        Log.i(Tags.WEB, "Search online games");
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(ApiUtils.SEARCH_URL));
                        startActivity(intent);
                        UiUtils.animateForward(this);
                        break;
                    case R.id.action_facebook:
                        Log.i(Tags.WEB, "Facebook");
                        Intent browserIntent;
                        try {
                            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("fb://page/1983857898556706"));
                            startActivity(browserIntent);
                            UiUtils.animateForward(this);
                        } catch (ActivityNotFoundException e) {
                            browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/VolleyballReferee/"));
                            startActivity(browserIntent);
                            UiUtils.animateForward(this);
                        }
                        break;
                    case R.id.action_credits:
                        Log.i(Tags.MAIN_UI, "Credits");
                        intent = new Intent(this, CreditsActivity.class);
                        startActivity(intent);
                        UiUtils.animateForward(this);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                Log.i(Tags.MAIN_UI, "Drawer");
                if (isNavigationDrawerOpen()) {
                    mDrawerLayout.closeDrawers();
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        if (isNavigationDrawerOpen()) {
            mDrawerLayout.closeDrawers();
        } else {
            super.onBackPressed();
        }
    }

    private boolean isNavigationDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START);
    }

    private void computeItemsVisibility(NavigationView navigationView) {
        if (PrefUtils.isWebPremiumPurchased(this)) {
            if (PrefUtils.isSignedIn(this)) {
                navigationView.getMenu().findItem(R.id.action_view_scheduled_games).setVisible(true);
            } else {
                navigationView.getMenu().findItem(R.id.action_view_scheduled_games).setVisible(false);
            }
        } else {
            navigationView.getMenu().findItem(R.id.action_view_scheduled_games).setVisible(false);
        }
    }

    protected void computePurchaseItemVisibility(MenuItem item) {
        final BillingService billingService = new BillingManager(this);
        billingService.addBillingListener(() -> item.setVisible(!billingService.isAllPurchased()));
        billingService.executeServiceRequest(() -> item.setVisible(!billingService.isAllPurchased()));
    }
}
