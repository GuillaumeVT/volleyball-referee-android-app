package com.tonkar.volleyballreferee.ui.setup;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.SavedRulesService;
import com.tonkar.volleyballreferee.interfaces.team.TeamService;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.game.GameActivity;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.game.TimeBasedGameActivity;
import com.tonkar.volleyballreferee.ui.rules.RulesSetupFragment;

public class QuickGameSetupActivity extends AppCompatActivity {

    private MenuItem mConfirmItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-QGSActivity", "Create quick game setup activity");
        setContentView(R.layout.activity_quick_game_setup);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        setTitle("");

        final BottomNavigationView gameSetupNavigation = findViewById(R.id.quick_game_setup_nav);
        initGameSetupNavigation(gameSetupNavigation, savedInstanceState);

        computeConfirmItemVisibility();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getRecordedGamesService().saveSetupGame(ServicesProvider.getInstance().getGameService());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_quick_teams_setup, menu);

        mConfirmItem = menu.findItem(R.id.action_confirm);
        computeConfirmItemVisibility();

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_confirm:
                confirmSetup();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void computeConfirmItemVisibility() {
        if (mConfirmItem != null) {
            TeamService teamService = ServicesProvider.getInstance().getTeamService();
            Rules rules = ServicesProvider.getInstance().getGeneralService().getRules();

            if (teamService.getTeamName(TeamType.HOME).isEmpty() || teamService.getTeamName(TeamType.GUEST).isEmpty()
                    || rules.getName().length() == 0 || ServicesProvider.getInstance().getSavedRulesService().getSavedRules(rules.getName()) != null) {
                Log.i("VBR-QGSActivity", "Confirm button is invisible");
                mConfirmItem.setVisible(false);
            } else {
                Log.i("VBR-QGSActivity", "Confirm button is visible");
                mConfirmItem.setVisible(true);
            }
        }
    }

    public void confirmSetup() {
        Log.i("VBR-QGSActivity", "Validate setup");
        ServicesProvider.getInstance().getGeneralService().startMatch();

        if (GameType.TIME.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
            Log.i("VBR-QGSActivity", "Start time-based game activity");
            final Intent gameIntent = new Intent(this, TimeBasedGameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
        } else {
            saveRules();
            Log.i("VBR-QGSActivity", "Start game activity");
            final Intent gameIntent = new Intent(this, GameActivity.class);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            gameIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(gameIntent);
        }
    }

    private void saveRules() {
        SavedRulesService savedRulesService = ServicesProvider.getInstance().getSavedRulesService();
        savedRulesService.createAndSaveRulesFrom(ServicesProvider.getInstance().getGeneralService().getRules());
    }

    private void initGameSetupNavigation(final BottomNavigationView gameSetupNavigation, Bundle savedInstanceState) {
        gameSetupNavigation.setOnNavigationItemSelectedListener(
                new BottomNavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        final Fragment fragment;

                        switch (item.getItemId()) {
                            case R.id.teams_tab:
                                fragment = QuickGameSetupFragment.newInstance();
                                break;
                            case R.id.rules_tab:
                                if (GameType.TIME.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
                                    fragment = null;
                                } else {
                                    fragment = RulesSetupFragment.newInstance();
                                }
                                break;
                            default:
                                fragment = null;
                                break;
                        }

                        final FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                        transaction.replace(R.id.quick_game_setup_container, fragment).commit();

                        return true;
                    }
                }
        );

        if (savedInstanceState == null) {
            gameSetupNavigation.setSelectedItemId(R.id.teams_tab);
        }

        if (GameType.TIME.equals(ServicesProvider.getInstance().getGeneralService().getGameType())) {
            gameSetupNavigation.setVisibility(View.GONE);
        }
    }

}
