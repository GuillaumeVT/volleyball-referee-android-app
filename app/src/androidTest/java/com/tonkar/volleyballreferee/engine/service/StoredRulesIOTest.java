package com.tonkar.volleyballreferee.engine.service;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.api.model.RulesDto;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.rules.Rules;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoredRulesIOTest {

    private Context mContext;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void save() {
        StoredRulesService storedRulesService = new StoredRulesManager(mContext.getApplicationContext());

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

        storedRulesService.createAndSaveRulesFrom(rules);

        rules = storedRulesService.createRules(GameType.BEACH);
        rules.setName("Test Rules 2");
        rules.setCreatedBy(UUID.randomUUID().toString());
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

        storedRulesService.createAndSaveRulesFrom(rules);
    }

    @Test
    public void writeThenRead() {
        StoredRulesService storedRulesService = new StoredRulesManager(mContext.getApplicationContext());

        List<RulesDto> expectedList = new ArrayList<>();
        expectedList.add(storedRulesService.getRules(GameType.INDOOR, "Test Rules 1"));
        expectedList.add(storedRulesService.getRules(GameType.BEACH, "Test Rules 2"));

        List<RulesDto> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredRulesManager.writeRulesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredRulesManager.readRulesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());

        for (RulesDto rules : expectedList) {
            storedRulesService.deleteRules(rules.getId());
        }
    }

}
