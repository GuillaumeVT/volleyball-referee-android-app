package com.tonkar.volleyballreferee.ui.stored.game;

import android.os.Bundle;
import android.util.Log;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;

public class StoredAdvancedGameActivity extends StoredGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameId = getIntent().getStringExtra("game");
        mStoredGamesService = new StoredGamesManager(this);
        mStoredGame = mStoredGamesService.getGame(mGameId);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create stored advanced game activity");
        setContentView(R.layout.activity_stored_advanced_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateGame();

        final ViewPager storedGamePager = findViewById(R.id.stored_game_pager);
        final StoredGameFragmentPagerAdapter storedGamePagerAdapter = new StoredGameFragmentPagerAdapter(mStoredGame, this, getSupportFragmentManager());
        storedGamePager.setAdapter(storedGamePagerAdapter);

        final TabLayout storedGameTabs = findViewById(R.id.stored_game_tabs);
        storedGameTabs.setupWithViewPager(storedGamePager);
    }
}
