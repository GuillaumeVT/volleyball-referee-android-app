package com.tonkar.volleyballreferee;

import androidx.test.rule.ActivityTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.api.ApiRules;
import com.tonkar.volleyballreferee.business.data.StoredRules;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.StoredRulesService;
import com.tonkar.volleyballreferee.business.rules.Rules;
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
public class StoredRulesIOTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void save() {
        StoredRulesService storedRulesService = new StoredRules(mActivityRule.getActivity().getApplicationContext());

        Rules rules = storedRulesService.createRules(GameType.INDOOR);
        rules.setName("Test Rules 1");
        rules.setCreatedBy("66bgghvgh55@google");
        rules.setSetsPerGame(15);
        rules.setPointsPerSet(22);
        rules.setTieBreakInLastSet(false);
        rules.setPointsInTieBreak(19);
        rules.setTwoPointsDifference(false);
        rules.setSanctions(true);
        rules.setTeamTimeouts(false);
        rules.setTeamTimeoutsPerSet(6);
        rules.setTeamTimeoutDuration(1234);
        rules.setTechnicalTimeouts(true);
        rules.setTechnicalTimeoutDuration(756);
        rules.setGameIntervals(true);
        rules.setGameIntervalDuration(532);
        rules.setTeamSubstitutionsPerSet(0);
        rules.setBeachCourtSwitches(false);
        rules.setBeachCourtSwitchFreq(6);
        rules.setBeachCourtSwitchFreqTieBreak(1);
        rules.setCustomConsecutiveServesPerPlayer(77);

        storedRulesService.saveRules(rules);

        rules = storedRulesService.createRules(GameType.BEACH);
        rules.setName("Test Rules 2");
        rules.setCreatedBy("byg765bvg66v@facebook");
        rules.setSetsPerGame(1);
        rules.setPointsPerSet(99);
        rules.setTieBreakInLastSet(true);
        rules.setPointsInTieBreak(0);
        rules.setTwoPointsDifference(true);
        rules.setSanctions(false);
        rules.setTeamTimeouts(true);
        rules.setTeamTimeoutsPerSet(9);
        rules.setTeamTimeoutDuration(765);
        rules.setTechnicalTimeouts(false);
        rules.setTechnicalTimeoutDuration(40);
        rules.setGameIntervals(false);
        rules.setGameIntervalDuration(90);
        rules.setTeamSubstitutionsPerSet(2);
        rules.setBeachCourtSwitches(true);
        rules.setBeachCourtSwitchFreq(8);
        rules.setBeachCourtSwitchFreqTieBreak(9);
        rules.setCustomConsecutiveServesPerPlayer(10);

        storedRulesService.saveRules(rules);
    }

    @Test
    public void writeThenRead() {
        StoredRulesService storedRulesService = new StoredRules(mActivityRule.getActivity().getApplicationContext());

        List<ApiRules> expectedList = new ArrayList<>();
        expectedList.add(storedRulesService.getRules(GameType.INDOOR,"Test Rules 1"));
        expectedList.add(storedRulesService.getRules(GameType.BEACH, "Test Rules 2"));

        List<ApiRules> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredRules.writeRulesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredRules.readRulesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());

        for (ApiRules rules : expectedList) {
            storedRulesService.deleteRules(rules.getId());
        }
    }

    @Test
    public void clear() {
        StoredRulesService storedRulesService = new StoredRules(mActivityRule.getActivity().getApplicationContext());
        storedRulesService.deleteAllRules();
    }
}
