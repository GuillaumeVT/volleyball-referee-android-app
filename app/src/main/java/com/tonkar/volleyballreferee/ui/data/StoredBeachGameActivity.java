package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.Tags;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.ui.util.UiUtils;
import com.tonkar.volleyballreferee.ui.game.LadderListAdapter;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class StoredBeachGameActivity extends StoredGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameId = getIntent().getStringExtra("game");
        mStoredGamesService = new StoredGames(this);
        mStoredGameService = mStoredGamesService.getGame(mGameId);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create stored beach game activity");
        setContentView(R.layout.activity_stored_beach_game);

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

        gameDate.setText(formatter.format(new Date(mStoredGameService.getScheduledAt())));
        homeTeamName.setText(mStoredGameService.getTeamName(TeamType.HOME));
        guestTeamName.setText(mStoredGameService.getTeamName(TeamType.GUEST));
        homeTeamSets.setText(UiUtils.formatNumberFromLocale(mStoredGameService.getSets(TeamType.HOME)));
        guestTeamSets.setText(UiUtils.formatNumberFromLocale(mStoredGameService.getSets(TeamType.GUEST)));

        UiUtils.colorTeamText(this, mStoredGameService.getTeamColor(TeamType.HOME), homeTeamSets);
        UiUtils.colorTeamText(this, mStoredGameService.getTeamColor(TeamType.GUEST), guestTeamSets);

        gameScore.setText(mStoredGameService.getScore());

        ListView setsList = findViewById(R.id.stored_game_set_list);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(getLayoutInflater(), mStoredGameService, mStoredGameService, mStoredGameService, mStoredGameService, false);
        setsList.setAdapter(ladderListAdapter);
    }

}
