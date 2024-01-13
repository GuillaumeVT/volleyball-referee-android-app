package com.tonkar.volleyballreferee.ui.data.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.ui.game.ladder.LadderListAdapter;

public class StoredBasicGameActivity extends StoredGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameId = getIntent().getStringExtra("game");
        mStoredGamesService = new StoredGamesManager(this);
        mStoredGame = mStoredGamesService.getGame(mGameId);

        if (mStoredGame == null) {
            getOnBackPressedDispatcher().onBackPressed();
        }

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create stored basic game activity");
        setContentView(R.layout.activity_stored_basic_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateGame();

        initScoreSheetAvailability();

        ListView setsList = findViewById(R.id.stored_game_set_list);
        LadderListAdapter ladderListAdapter = new LadderListAdapter(getLayoutInflater(), mStoredGame, mStoredGame, mStoredGame, mStoredGame,
                                                                    false);
        setsList.setAdapter(ladderListAdapter);
    }

    public void generateScoreSheet(View view) {
        super.generateScoreSheet();
    }
}
