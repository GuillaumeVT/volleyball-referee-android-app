package com.tonkar.volleyballreferee.ui.stored.game;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.game.UsageType;
import com.tonkar.volleyballreferee.engine.stored.StoredGamesManager;
import com.tonkar.volleyballreferee.ui.stored.game.scoresheet.ScoreSheetActivity;
import com.tonkar.volleyballreferee.ui.util.UiUtils;

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
        UiUtils.updateToolbarLogo(toolbar, mStoredGame.getKind(), UsageType.NORMAL);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateGame();

        View generateScoreSheetLayout = findViewById(R.id.generate_score_sheet_layout);

        if (PrefUtils.isScoreSheetsPurchased(this)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            generateScoreSheetLayout.setVisibility(View.VISIBLE);
        } else {
            generateScoreSheetLayout.setVisibility(View.GONE);
        }

        final ViewPager storedGamePager = findViewById(R.id.stored_game_pager);
        final StoredGameFragmentPagerAdapter storedGamePagerAdapter = new StoredGameFragmentPagerAdapter(mStoredGame, this, getSupportFragmentManager());
        storedGamePager.setAdapter(storedGamePagerAdapter);

        final TabLayout storedGameTabs = findViewById(R.id.stored_game_tabs);
        storedGameTabs.setupWithViewPager(storedGamePager);
    }

    public void generateScoreSheet(View view) {
        Log.i(Tags.STORED_GAMES, "Generate scoresheet");

        if (PrefUtils.isScoreSheetsPurchased(this)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            Intent intent = new Intent(this, ScoreSheetActivity.class);
            intent.putExtra("game", mGameId);
            startActivity(intent);
            UiUtils.animateForward(this);
        }
    }
}
