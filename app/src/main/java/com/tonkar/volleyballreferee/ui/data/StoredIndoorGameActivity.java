package com.tonkar.volleyballreferee.ui.data;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.tabs.TabLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.Tags;

import androidx.viewpager.widget.ViewPager;

public class StoredIndoorGameActivity extends StoredGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameId = getIntent().getStringExtra("game");
        mStoredGamesService = new StoredGames(this);
        mStoredGameService = mStoredGamesService.getGame(mGameId);

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create stored indoor game activity");
        setContentView(R.layout.activity_stored_indoor_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateGame();

        final ViewPager indoorGamePager = findViewById(R.id.indoor_game_pager);
        final StoredIndoorGameFragmentPagerAdapter indoorGamePagerAdapter = new StoredIndoorGameFragmentPagerAdapter(mStoredGameService, this, getSupportFragmentManager());
        indoorGamePager.setAdapter(indoorGamePagerAdapter);

        final TabLayout indoorGameTabs = findViewById(R.id.indoor_game_tabs);
        indoorGameTabs.setupWithViewPager(indoorGamePager);
    }
}
