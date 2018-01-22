package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.UiUtils;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecordedIndoorGameActivity extends RecordedGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("VBR-RGameActivity", "Create recorded indoor game activity");
        setContentView(R.layout.activity_recorded_indoor_game);
        setTitle("");

        ServicesProvider.getInstance().restoreRecordedGamesService(getApplicationContext());

        Intent intent = getIntent();
        mGameDate = intent.getLongExtra("game_date", 0L);

        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService();

        RecordedGameService recordedGameService = mRecordedGamesService.getRecordedGameService(mGameDate);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        TextView gameDate = findViewById(R.id.game_date);
        TextView homeTeamName = findViewById(R.id.home_team_name_text);
        TextView guestTeamName = findViewById(R.id.guest_team_name_text);
        TextView homeTeamSets = findViewById(R.id.home_team_set_text);
        TextView guestTeamSets = findViewById(R.id.guest_team_set_text);
        TextView gameScore = findViewById(R.id.game_score);

        gameDate.setText(formatter.format(new Date(mGameDate)));
        homeTeamName.setText(recordedGameService.getTeamName(TeamType.HOME));
        guestTeamName.setText(recordedGameService.getTeamName(TeamType.GUEST));
        homeTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.GUEST)));

        UiUtils.colorTeamText(this, recordedGameService.getTeamColor(TeamType.HOME), homeTeamSets);
        UiUtils.colorTeamText(this, recordedGameService.getTeamColor(TeamType.GUEST), guestTeamSets);

        gameScore.setText(buildScore(recordedGameService));

        final ViewPager indoorGamePager = findViewById(R.id.indoor_game_pager);
        final RecordedIndoorGameFragmentPagerAdapter indoorGamePagerAdapter = new RecordedIndoorGameFragmentPagerAdapter(recordedGameService, this, getSupportFragmentManager());
        indoorGamePager.setAdapter(indoorGamePagerAdapter);

        final TabLayout indoorGameTabs = findViewById(R.id.indoor_game_tabs);
        indoorGameTabs.setupWithViewPager(indoorGamePager);
    }

}
