package com.tonkar.volleyballreferee.ui.onboarding;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.appintro.*;
import com.github.appintro.model.SliderPage;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;

public class MainOnboardingActivity extends AppIntro {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SliderPage indoorPage = new SliderPage();
        indoorPage.setTitle(getString(R.string.volleyball_game));
        indoorPage.setDescription(getString(R.string.volleyball_game_description));
        indoorPage.setImageDrawable(R.drawable.indoor);
        indoorPage.setBackgroundDrawable(R.drawable.indoor_background);
        indoorPage.setTitleColorRes(R.color.colorOnDarkSurface);
        indoorPage.setDescriptionColorRes(R.color.colorOnDarkSurface);
        indoorPage.setBackgroundColorRes(R.color.colorBackgroundMain);

        addSlide(AppIntroFragment.createInstance(indoorPage));

        SliderPage beachPage = new SliderPage();
        beachPage.setTitle(getString(R.string.beach_volleyball_game));
        beachPage.setDescription(getString(R.string.beach_volleyball_game_description));
        beachPage.setImageDrawable(R.drawable.beach);
        beachPage.setBackgroundDrawable(R.drawable.beach_background);
        beachPage.setTitleColorRes(R.color.colorOnLightSurface);
        beachPage.setDescriptionColorRes(R.color.colorOnLightSurface);
        beachPage.setBackgroundColorRes(R.color.colorBackgroundMain);

        addSlide(AppIntroFragment.createInstance(beachPage));

        SliderPage scorePage = new SliderPage();
        scorePage.setTitle(getString(R.string.score_based_game));
        scorePage.setDescription(getString(R.string.score_based_game_description));
        scorePage.setImageDrawable(R.drawable.scoreboard);
        scorePage.setBackgroundDrawable(R.drawable.indoor_background);
        scorePage.setTitleColorRes(R.color.colorOnDarkSurface);
        scorePage.setDescriptionColorRes(R.color.colorOnDarkSurface);
        scorePage.setBackgroundColorRes(R.color.colorBackgroundMain);

        addSlide(AppIntroFragment.createInstance(scorePage));

        setTransformer(new AppIntroPageTransformerType.Parallax(1.0, -1.0, 2.0));

        setImmersive(true);
    }

    @Override
    protected void onDonePressed(@Nullable Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        completeMainOnboarding();
    }

    @Override
    protected void onSkipPressed(@Nullable Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        completeMainOnboarding();
    }

    private void completeMainOnboarding() {
        PrefUtils.completeOnboarding(this, PrefUtils.PREF_ONBOARDING_MAIN);
        finish();
    }
}