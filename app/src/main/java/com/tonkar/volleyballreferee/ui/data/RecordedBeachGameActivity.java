package com.tonkar.volleyballreferee.ui.data;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.data.RecordedGameService;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.game.LadderListAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class RecordedBeachGameActivity extends RecordedGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.SAVED_GAMES, "Create recorded beach game activity");
        setContentView(R.layout.activity_recorded_beach_game);
        setTitle("");

        Intent intent = getIntent();
        mGameDate = intent.getLongExtra("game_date", 0L);

        mRecordedGamesService = ServicesProvider.getInstance().getRecordedGamesService(getApplicationContext());

        RecordedGameService recordedGameService = mRecordedGamesService.getRecordedGameService(mGameDate);

        DateFormat formatter = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
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
        homeTeamSets.setText(UiUtils.formatNumberFromLocale(recordedGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(UiUtils.formatNumberFromLocale(recordedGameService.getSets(TeamType.GUEST)));

        UiUtils.colorTeamText(this, recordedGameService.getTeamColor(TeamType.HOME), homeTeamSets);
        UiUtils.colorTeamText(this, recordedGameService.getTeamColor(TeamType.GUEST), guestTeamSets);

        gameScore.setText(buildScore(recordedGameService));

        ListView setsList = findViewById(R.id.recorded_game_set_list);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(getLayoutInflater(), recordedGameService, recordedGameService, recordedGameService, recordedGameService, false);
        setsList.setAdapter(ladderListAdapter);
    }

}
