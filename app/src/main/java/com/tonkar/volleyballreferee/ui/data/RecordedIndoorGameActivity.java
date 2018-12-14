package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.RecordedGames;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import androidx.viewpager.widget.ViewPager;

public class RecordedIndoorGameActivity extends RecordedGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameDate = getIntent().getLongExtra("game_date", 0L);
        mRecordedGamesService = new RecordedGames(this);
        mRecordedGameService = mRecordedGamesService.getRecordedGameService(mGameDate);

        super.onCreate(savedInstanceState);

        Log.i(Tags.SAVED_GAMES, "Create recorded indoor game activity");
        setContentView(R.layout.activity_recorded_indoor_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        TextView gameDate = findViewById(R.id.game_date);
        TextView homeTeamName = findViewById(R.id.home_team_name_text);
        TextView guestTeamName = findViewById(R.id.guest_team_name_text);
        TextView homeTeamSets = findViewById(R.id.home_team_set_text);
        TextView guestTeamSets = findViewById(R.id.guest_team_set_text);
        TextView gameScore = findViewById(R.id.game_score);

        gameDate.setText(formatter.format(new Date(mGameDate)));
        homeTeamName.setText(mRecordedGameService.getTeamName(TeamType.HOME));
        guestTeamName.setText(mRecordedGameService.getTeamName(TeamType.GUEST));
        homeTeamSets.setText(UiUtils.formatNumberFromLocale(mRecordedGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(UiUtils.formatNumberFromLocale(mRecordedGameService.getSets(TeamType.GUEST)));

        UiUtils.colorTeamText(this, mRecordedGameService.getTeamColor(TeamType.HOME), homeTeamSets);
        UiUtils.colorTeamText(this, mRecordedGameService.getTeamColor(TeamType.GUEST), guestTeamSets);

        gameScore.setText(buildScore(mRecordedGameService));

        final ViewPager indoorGamePager = findViewById(R.id.indoor_game_pager);
        final RecordedIndoorGameFragmentPagerAdapter indoorGamePagerAdapter = new RecordedIndoorGameFragmentPagerAdapter(mRecordedGameService, this, getSupportFragmentManager());
        indoorGamePager.setAdapter(indoorGamePagerAdapter);

        final TabLayout indoorGameTabs = findViewById(R.id.indoor_game_tabs);
        indoorGameTabs.setupWithViewPager(indoorGamePager);
    }
}
