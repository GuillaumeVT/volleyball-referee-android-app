package com.tonkar.volleyballreferee.ui.team;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.ui.game.GameActivity;

public class LiberosSetupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_liberos_setup);

        Log.i("VBR-LSActivity", "Create liberos setup activity");

        final ViewPager liberoSetupPager = findViewById(R.id.libero_setup_pager);
        liberoSetupPager.setAdapter(new LiberoSetupFragmentPagerAdapter(this, getSupportFragmentManager()));

        TabLayout liberoSetupTabs = findViewById(R.id.libero_setup_tabs);
        liberoSetupTabs.setupWithViewPager(liberoSetupPager);
    }

    public void validateLiberos(View view) {
        Log.i("VBR-LSActivity", "Validate liberos");

        Log.i("VBR-LSActivity", "Start game activity");
        final Intent gameIntent = new Intent(this, GameActivity.class);
        startActivity(gameIntent);
    }

}
