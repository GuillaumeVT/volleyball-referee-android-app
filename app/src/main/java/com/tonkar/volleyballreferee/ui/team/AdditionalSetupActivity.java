package com.tonkar.volleyballreferee.ui.team;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.ui.UiUtils;
import com.tonkar.volleyballreferee.ui.game.GameActivity;

public class AdditionalSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_additional_setup);

        Log.i("VBR-ASActivity", "Create additional setup activity");

        setTitle("");

        final ViewPager additionalSetupPager = findViewById(R.id.additional_setup_pager);
        additionalSetupPager.setAdapter(new AdditionalSetupFragmentPagerAdapter(getSupportFragmentManager()));

        TabLayout additionalSetupTabs = findViewById(R.id.additional_setup_tabs);
        additionalSetupTabs.setupWithViewPager(additionalSetupPager);
    }

    public void validateTeams(View view) {
        Log.i("VBR-ASActivity", "Validate teams");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.teams_setup_title)).setMessage(getResources().getString(R.string.confirm_teams_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ServicesProvider.getInstance().getTeamService().initTeams();
                Log.i("VBR-ASActivity", "Start game activity");
                final Intent gameIntent = new Intent(AdditionalSetupActivity.this, GameActivity.class);
                startActivity(gameIntent);
            }
        });
        builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {}
        });
        AlertDialog alertDialog = builder.show();
        UiUtils.setAlertDialogMessageSize(alertDialog, getResources());
    }

}
