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

public class LiberosSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liberos_setup);

        Log.i("VBR-LSActivity", "Create liberos setup activity");

        final ViewPager liberoSetupPager = findViewById(R.id.libero_setup_pager);
        liberoSetupPager.setAdapter(new LiberoSetupFragmentPagerAdapter(getSupportFragmentManager()));

        TabLayout liberoSetupTabs = findViewById(R.id.libero_setup_tabs);
        liberoSetupTabs.setupWithViewPager(liberoSetupPager);
    }

    public void validateLiberos(View view) {
        Log.i("VBR-LSActivity", "Validate liberos");

        final AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.AppTheme_Dialog);
        builder.setTitle(getResources().getString(R.string.teams_setup_title)).setMessage(getResources().getString(R.string.confirm_teams_setup_question));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ServicesProvider.getInstance().getTeamService().initTeams();
                Log.i("VBR-LSActivity", "Start game activity");
                final Intent gameIntent = new Intent(LiberosSetupActivity.this, GameActivity.class);
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
