package com.tonkar.volleyballreferee.ui.team;

import android.annotation.TargetApi;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.interfaces.UsageType;
import com.tonkar.volleyballreferee.ui.UiUtils;

public class TeamsSetupActivity extends AppCompatActivity {

    private BaseTeamService mTeamService;
    private Button          mNextButton;
    private ImageButton     mGenderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-TSActivity", "Create team setup activity");
        setContentView(R.layout.activity_teams_setup);

        if (!ServicesProvider.getInstance().areServicesAvailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getApplicationContext());
        }

        mTeamService = ServicesProvider.getInstance().getTeamService();

        setTitle("");

        mNextButton = findViewById(R.id.next_button);
        mGenderButton = findViewById(R.id.switch_gender_button);

        updateGender(mTeamService.getGenderType());

        final ViewPager teamSetupPager = findViewById(R.id.team_setup_pager);
        teamSetupPager.setAdapter(new TeamSetupFragmentPagerAdapter(this, getSupportFragmentManager()));

        TabLayout teamSetupTabs = findViewById(R.id.team_setup_tabs);
        teamSetupTabs.setupWithViewPager(teamSetupPager);

        computeNextButtonActivation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        ServicesProvider.getInstance().getGamesHistoryService().saveSetupGame(ServicesProvider.getInstance().getGameService());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_teams_setup, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup_scoreboard_usage:
                final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
                builder.setTitle(getResources().getString(R.string.scoreboard_usage_title)).setMessage(getResources().getString(R.string.scoreboard_usage_message));
                builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ServicesProvider.getInstance().getGameService().setUsageType(UsageType.SCOREBOARD);

                        Log.i("VBR-TSActivity", "Start activity to setup teams quickly");
                        final Intent intent = new Intent(TeamsSetupActivity.this, QuickTeamsSetupActivity.class);
                        intent.putExtra("scoreboard_usage", true);
                        startActivity(intent);
                    }
                });
                builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {}
                });
                AlertDialog alertDialog = builder.show();
                UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void computeNextButtonActivation() {
        if (mTeamService.getTeamName(TeamType.HOME).isEmpty() || mTeamService.getNumberOfPlayers(TeamType.HOME) < 6
                ||mTeamService.getTeamName(TeamType.GUEST).isEmpty() || mTeamService.getNumberOfPlayers(TeamType.GUEST) < 6) {
            Log.i("VBR-TSActivity", "Next button is disabled");
            mNextButton.setEnabled(false);
        } else {
            Log.i("VBR-TSActivity", "Next button is enabled");
            mNextButton.setEnabled(true);
        }
    }

    public void switchGender(View view) {
        Log.i("VBR-TSActivity", "Switch gender");
        GenderType genderType = mTeamService.getGenderType().next();
        updateGender(genderType);
    }

    private void updateGender(GenderType genderType) {
        mTeamService.setGenderType(genderType);
        switch (genderType) {
            case MIXED:
                mGenderButton.setImageResource(R.drawable.ic_mixed);
                break;
            case LADIES:
                mGenderButton.setImageResource(R.drawable.ic_ladies);
                break;
            case GENTS:
                mGenderButton.setImageResource(R.drawable.ic_gents);
                break;
        }
        colorGender(genderType);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void colorGender(GenderType genderType) {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            switch (genderType) {
                case MIXED:
                    mGenderButton.setImageTintList(ContextCompat.getColorStateList(this, R.color.colorMixed));
                    break;
                case LADIES:
                    mGenderButton.setImageTintList(ContextCompat.getColorStateList(this, R.color.colorLadies));
                    break;
                case GENTS:
                    mGenderButton.setImageTintList(ContextCompat.getColorStateList(this, R.color.colorGents));
                    break;
            }
        }
    }

    public void validateTeams(View view) {
        Log.i("VBR-TSActivity", "Validate teams");

        Log.i("VBR-TSActivity", "Start liberos setup activity");
        final Intent intent = new Intent(this, AdditionalSetupActivity.class);
        startActivity(intent);
    }

}
