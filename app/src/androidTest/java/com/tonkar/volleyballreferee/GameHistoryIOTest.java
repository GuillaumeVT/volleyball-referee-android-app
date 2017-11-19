package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.history.JsonHistoryReader;
import com.tonkar.volleyballreferee.business.history.JsonHistoryWriter;
import com.tonkar.volleyballreferee.business.history.RecordedGame;
import com.tonkar.volleyballreferee.business.history.RecordedSet;
import com.tonkar.volleyballreferee.business.history.RecordedTeam;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class GameHistoryIOTest {

    @Test
    public void writeThenRead() {
        List<RecordedGame> expectedList = new ArrayList<>();
        expectedList.add(someRecordedGame1());
        expectedList.add(someRecordedGame2());
        List<RecordedGame> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonHistoryWriter.writeRecordedGamesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = JsonHistoryReader.readRecordedGamesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

    private RecordedGame someRecordedGame1() {
        RecordedTeam team1 = new RecordedTeam("Team 1", Color.parseColor("#123456"));
        RecordedTeam team2 = new RecordedTeam("Team 2", Color.parseColor("#a1b2c3"));

        List<RecordedSet> sets = new ArrayList<>();

        List<TeamType> ladder1 = Arrays.asList(TeamType.HOME, TeamType.GUEST, TeamType.GUEST, TeamType.HOME, TeamType.HOME);
        RecordedSet set1 = new RecordedSet(5555L, 3, 2, ladder1);

        List<TeamType> ladder2 = Arrays.asList(TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.GUEST);
        RecordedSet set2 = new RecordedSet(4540L, 4, 3, ladder2);

        sets.add(set1);
        sets.add(set2);

        return new RecordedGame(GameType.INDOOR, 123456L, team1, team2, sets);
    }

    private RecordedGame someRecordedGame2() {
        RecordedTeam team1 = new RecordedTeam("Player A / Player B", Color.parseColor("#1234ab"));
        RecordedTeam team2 = new RecordedTeam("Player C / Player D", Color.parseColor("#efa1c3"));

        List<RecordedSet> sets = new ArrayList<>();

        List<TeamType> ladder1 = Arrays.asList(TeamType.GUEST, TeamType.GUEST);
        RecordedSet set1 = new RecordedSet(12L, 0, 2, ladder1);

        List<TeamType> ladder2 = Arrays.asList(TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.HOME);
        RecordedSet set2 = new RecordedSet(0L, 6, 2, ladder2);

        sets.add(set1);
        sets.add(set2);

        return new RecordedGame(GameType.BEACH, 646516L, team1, team2, sets);
    }

}
