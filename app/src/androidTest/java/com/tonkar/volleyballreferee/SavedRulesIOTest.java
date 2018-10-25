package com.tonkar.volleyballreferee;

import android.content.Context;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.SavedRules;
import com.tonkar.volleyballreferee.rules.Rules;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SavedRulesIOTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void save() {
        Context applicationContext = mActivityRule.getActivity().getApplicationContext();

        ServicesProvider.getInstance().getSavedRulesService(applicationContext).createRules();
        Rules rules = ServicesProvider.getInstance().getSavedRulesService(applicationContext).getCurrentRules();

        rules.setName("Test Rules 1");
        rules.setUserId("66bgghvgh55@google");
        rules.setDate(34567654L);
        rules.setSetsPerGame(15);
        rules.setPointsPerSet(22);
        rules.setTieBreakInLastSet(false);
        rules.setPointsInTieBreak(19);
        rules.setTwoPointsDifference(false);
        rules.setSanctionsEnabled(true);
        rules.setTeamTimeoutsEnabled(false);
        rules.setTeamTimeoutsPerSet(6);
        rules.setTeamTimeoutDuration(1234);
        rules.setTechnicalTimeoutsEnabled(true);
        rules.setTechnicalTimeoutDuration(756);
        rules.setGameIntervalsEnabled(true);
        rules.setGameIntervalDuration(532);
        rules.setTeamSubstitutionsPerSet(0);
        rules.setBeachCourtSwitchesEnabled(false);
        rules.setBeachCourtSwitchFrequency(6);
        rules.setBeachCourtSwitchFrequencyTieBreak(1);
        rules.setCustomConsecutiveServesPerPlayer(77);

        ServicesProvider.getInstance().getSavedRulesService(applicationContext).saveCurrentRules();

        ServicesProvider.getInstance().getSavedRulesService(applicationContext).createRules();
        rules = ServicesProvider.getInstance().getSavedRulesService(applicationContext).getCurrentRules();

        rules.setName("Test Rules 2");
        rules.setUserId("byg765bvg66v@facebook");
        rules.setDate(494030L);
        rules.setSetsPerGame(1);
        rules.setPointsPerSet(99);
        rules.setTieBreakInLastSet(true);
        rules.setPointsInTieBreak(0);
        rules.setTwoPointsDifference(true);
        rules.setSanctionsEnabled(false);
        rules.setTeamTimeoutsEnabled(true);
        rules.setTeamTimeoutsPerSet(9);
        rules.setTeamTimeoutDuration(765);
        rules.setTechnicalTimeoutsEnabled(false);
        rules.setTechnicalTimeoutDuration(40);
        rules.setGameIntervalsEnabled(false);
        rules.setGameIntervalDuration(90);
        rules.setTeamSubstitutionsPerSet(2);
        rules.setBeachCourtSwitchesEnabled(true);
        rules.setBeachCourtSwitchFrequency(8);
        rules.setBeachCourtSwitchFrequencyTieBreak(9);
        rules.setCustomConsecutiveServesPerPlayer(10);

        ServicesProvider.getInstance().getSavedRulesService(applicationContext).saveCurrentRules();
    }

    @Test
    public void writeThenRead() {
        Context applicationContext = mActivityRule.getActivity().getApplicationContext();

        List<Rules> expectedList = new ArrayList<>();
        expectedList.add(ServicesProvider.getInstance().getSavedRulesService(applicationContext).getSavedRules("Test Rules 1"));
        expectedList.add(ServicesProvider.getInstance().getSavedRulesService(applicationContext).getSavedRules("Test Rules 2"));

        List<Rules> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            SavedRules.writeRulesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = SavedRules.readRulesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

    @Test
    public void clear() {
        ServicesProvider.getInstance().getSavedRulesService(mActivityRule.getActivity().getApplicationContext()).deleteAllSavedRules();
    }
}
