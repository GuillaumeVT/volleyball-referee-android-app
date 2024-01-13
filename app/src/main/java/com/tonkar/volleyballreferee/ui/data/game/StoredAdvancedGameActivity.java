package com.tonkar.volleyballreferee.ui.data.game;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.*;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

public class StoredAdvancedGameActivity extends StoredGameActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mGameId = getIntent().getStringExtra("game");
        mStoredGamesService = new StoredGamesManager(this);
        mStoredGame = mStoredGamesService.getGame(mGameId);

        if (mStoredGame == null) {
            getOnBackPressedDispatcher().onBackPressed();
        }

        super.onCreate(savedInstanceState);

        Log.i(Tags.STORED_GAMES, "Create stored advanced game activity");
        setContentView(R.layout.activity_stored_advanced_game);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        UiUtils.updateToolbarLogo(toolbar, mStoredGame.getKind(), UsageType.NORMAL);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateGame();

        initScoreSheetAvailability();

        final ViewPager2 storedGamePager = findViewById(R.id.stored_game_pager);
        final StoredGameFragmentStateAdapter storedGameStateAdapter = new StoredGameFragmentStateAdapter(this,
                                                                                                         mStoredGame.getNumberOfSets());
        storedGamePager.setAdapter(storedGameStateAdapter);

        final TabLayout storedGameTabs = findViewById(R.id.stored_game_tabs);
        new TabLayoutMediator(storedGameTabs, storedGamePager, (tab, position) -> {
            String tabText = position == 0 ? getString(R.string.players) : position == 1 ? getString(R.string.rules) : String.format(
                    getString(R.string.set_number), position - 1);
            tab.setText(tabText);
        }).attach();
    }

    public void generateScoreSheet(View view) {
        super.generateScoreSheet();
    }
}
