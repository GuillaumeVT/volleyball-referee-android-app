package com.tonkar.volleyballreferee.ui.screenshots;

import android.Manifest;
import android.os.Environment;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.game.BrazilFranceIndoorGame;
import com.tonkar.volleyballreferee.engine.game.ItalyUsaBeachGame;
import com.tonkar.volleyballreferee.engine.service.StoredGamesManager;
import com.tonkar.volleyballreferee.ui.MainActivity;
import com.tonkar.volleyballreferee.ui.game.GameActivity;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class Pixel4aScreenshots extends Screenshots {

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE);

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        mUiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
        mStoredGamesService = new StoredGamesManager(mContext);
        mScreenshotsDirectory = new File(mContext.getExternalFilesDir(Environment.DIRECTORY_SCREENSHOTS), LocalDate.now() + "_pixel_4a");

        if (!mScreenshotsDirectory.exists()) {
            mScreenshotsDirectory.mkdirs();
        }
    }

    @Test
    public void screenshot1() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        new ItalyUsaBeachGame().playGame_technicalTimeout(mContext, mStoredGamesService);

        ActivityScenario.launch(MainActivity.class);

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.resume_game_card))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot1", 2190000L, 2230000L);
        }
    }

    @Test
    public void screenshot2() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        new BrazilFranceIndoorGame().playGame_lastSetEnd(mContext, mStoredGamesService);

        ActivityScenario.launch(GameActivity.class);

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.swap_teams_button))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot2", 150000L, 160000L);
        }
    }

    @Test
    public void screenshot3() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        new ItalyUsaBeachGame().playGame_lastSetEnd(mContext, mStoredGamesService);

        ActivityScenario.launch(GameActivity.class);

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.swap_teams_button))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot3", 997000L, 1010000L);
        }
    }

    @Test
    public void screenshot4() throws IOException {
        PrefUtils.setNightMode(mContext, "dark");

        new BrazilFranceIndoorGame().playGame_substitutions(mContext, mStoredGamesService);

        ActivityScenario.launch(GameActivity.class);

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.left_team_position_3))
                    .perform(ViewActions.click());

            Espresso
                    .onView(ViewMatchers.withText(android.R.string.no))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot4", 113000L, 130000L);
        }
    }

    @Test
    public void screenshot5() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        new ItalyUsaBeachGame().playGame_technicalTimeout(mContext, mStoredGamesService);

        ActivityScenario.launch(GameActivity.class);

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.left_team_cards_button))
                    .perform(ViewActions.click());

            Espresso
                    .onView(ViewMatchers.withId(R.id.delay_warning_button))
                    .perform(ViewActions.click());

            Espresso
                    .onView(ViewMatchers.withText(android.R.string.ok))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot5", 240000L, 270000L);

            Espresso
                    .onView(ViewMatchers.withText(android.R.string.no))
                    .perform(ViewActions.click());
        }
    }

    @Test
    public void screenshot6() throws IOException {
        PrefUtils.setNightMode(mContext, "dark");

        new ItalyUsaBeachGame().playGame_lastSetEnd(mContext, mStoredGamesService);

        ActivityScenario.launch(GameActivity.class);

        Espresso
                .onView(ViewMatchers.withId(R.id.ladder_tab))
                .perform(ViewActions.click());

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.set_list))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot6", 115000L, 130000L);
        }
    }

    @Test
    public void screenshot7() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        ActivityScenario.launch(MainActivity.class);

        Espresso
                .onView(ViewMatchers.withId(R.id.navigation_fragment))
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.action_stored_rules))
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.add_rules_button))
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.add_indoor_rules_button))
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.rules_name_input_text))
                .perform(ViewActions.typeText("Test Rules"))
                .perform(ViewActions.closeSoftKeyboard());

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            takeScreenshot("screenshot7", 120000L, 150000L);
        }
    }

    @Test
    public void screenshot8() throws IOException {
        PrefUtils.setNightMode(mContext, "light");

        new BrazilFranceIndoorGame().playGame_complete(mContext, mStoredGamesService);

        ActivityScenario.launch(MainActivity.class);

        Espresso
                .onView(ViewMatchers.withId(R.id.navigation_fragment))
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.action_stored_teams))
                .perform(ViewActions.click());

        Espresso
                .onData(Matchers.anything())
                .inAdapterView(ViewMatchers.withId(R.id.stored_teams_list))
                .atPosition(0)
                .perform(ViewActions.click());

        Espresso
                .onView(ViewMatchers.withId(R.id.edit_team_button))
                .perform(ViewActions.click());

        for (Locale locale : mLocales) {
            setAppLanguage(locale);

            Espresso
                    .onView(ViewMatchers.withId(R.id.team_name_input_layout))
                    .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));

            takeScreenshot("screenshot8", 210000L, 235000L);
        }
    }
}
