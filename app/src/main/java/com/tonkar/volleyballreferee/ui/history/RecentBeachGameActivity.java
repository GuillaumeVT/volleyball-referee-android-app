package com.tonkar.volleyballreferee.ui.history;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.game.SetsListAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecentBeachGameActivity extends RecentGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recent_beach_game);

        Log.i("VBR-RecentActivity", "Create recent beach game activity");

        Intent intent = getIntent();
        mGameDate = intent.getLongExtra("game_date", 0L);

        DateFormat formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, Locale.getDefault());
        formatter.setTimeZone(TimeZone.getDefault());

        setTitle(formatter.format(new Date(mGameDate)));

        mGamesHistoryService = ServicesProvider.getInstance().getGameHistoryService();

        RecordedGameService recordedGameService = mGamesHistoryService.getRecordedGameService(mGameDate);

        TextView homeTeamName = findViewById(R.id.home_team_name_text);
        TextView guestTeamName = findViewById(R.id.guest_team_name_text);
        TextView homeTeamSets = findViewById(R.id.home_team_set_text);
        TextView guestTeamSets = findViewById(R.id.guest_team_set_text);
        TextView gameScore = findViewById(R.id.game_score);

        homeTeamName.setText(recordedGameService.getTeamName(TeamType.HOME));
        guestTeamName.setText(recordedGameService.getTeamName(TeamType.GUEST));
        homeTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(String.valueOf(recordedGameService.getSets(TeamType.GUEST)));

        color(homeTeamSets, recordedGameService.getTeamColor(TeamType.HOME));
        color(guestTeamSets, recordedGameService.getTeamColor(TeamType.GUEST));

        gameScore.setText(buildScore(recordedGameService));

        ListView setsList = findViewById(R.id.recent_game_set_list);
        SetsListAdapter setsListAdapter = new SetsListAdapter(getLayoutInflater(), recordedGameService, recordedGameService, false);
        setsList.setAdapter(setsListAdapter);
    }

}
