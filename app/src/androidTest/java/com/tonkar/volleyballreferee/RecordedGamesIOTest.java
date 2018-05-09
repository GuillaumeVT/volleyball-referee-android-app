package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.data.JsonIOUtils;
import com.tonkar.volleyballreferee.business.data.RecordedGame;
import com.tonkar.volleyballreferee.business.data.RecordedPlayer;
import com.tonkar.volleyballreferee.business.data.RecordedSet;
import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.sanction.Sanction;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.Substitution;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class RecordedGamesIOTest {

    @Test
    public void writeThenRead_all() {
        List<RecordedGame> expectedList = new ArrayList<>();
        expectedList.add(someRecordedGame1());
        expectedList.add(someRecordedGame2());
        List<RecordedGame> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonIOUtils.writeRecordedGamesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = JsonIOUtils.readRecordedGamesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

    @Test
    public void writeThenRead_one() {
        RecordedGame recordedGame1 = someRecordedGame1();
        try {
            byte[] bytes1 = JsonIOUtils.recordedGameToByteArray(recordedGame1);
            RecordedGame readGame1 = JsonIOUtils.byteArrayToRecordedGame(bytes1);
            assertEquals(recordedGame1, readGame1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        RecordedGame recordedGame2 = someRecordedGame2();
        try {
            byte[] bytes2 = JsonIOUtils.recordedGameToByteArray(recordedGame2);
            RecordedGame readGame2 = JsonIOUtils.byteArrayToRecordedGame(bytes2);
            assertEquals(recordedGame2, readGame2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private RecordedGame someRecordedGame1() {
        // TODO all new fields
        RecordedGame recordedGame = new RecordedGame();
        recordedGame.setGameType(GameType.INDOOR);
        recordedGame.setGameDate(123456L);
        recordedGame.setGenderType(GenderType.LADIES);
        recordedGame.setMatchCompleted(false);
        recordedGame.setSets(TeamType.HOME, 0);
        recordedGame.setSets(TeamType.GUEST, 2);

        RecordedTeam team1 = recordedGame.getTeam(TeamType.HOME);
        team1.setName("Team 1");
        team1.setColor(Color.parseColor("#123456"));
        team1.setLiberoColor(Color.parseColor("#ffffff"));
        Collections.addAll(team1.getPlayers(), 1, 3, 5, 7, 9, 11, 13);
        Collections.addAll(team1.getLiberos(), 2);
        team1.setCaptain(11);

        RecordedTeam team2 = recordedGame.getTeam(TeamType.GUEST);
        team2.setName("Team 2");
        team2.setColor(Color.parseColor("#a1b2c3"));
        team2.setLiberoColor(Color.parseColor("#000000"));
        Collections.addAll(team2.getPlayers(), 2, 4, 6, 8, 10, 12, 14, 16, 18);
        team2.setCaptain(2);

        RecordedSet set1 = new RecordedSet();
        recordedGame.getSets().add(set1);
        set1.setDuration(5555L);
        set1.setPoints(TeamType.HOME, 3);
        set1.setPoints(TeamType.GUEST, 2);
        set1.setTimeouts(TeamType.HOME, 1);
        set1.setTimeouts(TeamType.GUEST, 0);
        Collections.addAll(set1.getPointsLadder(), TeamType.HOME, TeamType.GUEST, TeamType.GUEST, TeamType.HOME, TeamType.HOME);
        set1.setServingTeam(TeamType.HOME);
        for (int index = 1; index <= 6; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getCurrentPlayers(TeamType.HOME).add(player);
        }
        for (int index = 1; index <= 4; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getCurrentPlayers(TeamType.GUEST).add(player);
        }
        for (int index = 1; index <= 3; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getStartingPlayers(TeamType.HOME).add(player);
        }
        for (int index = 1; index <= 2; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getStartingPlayers(TeamType.GUEST).add(player);
        }
        Collections.addAll(set1.getSubstitutions(TeamType.HOME), new Substitution(1, 5, 12, 10), new Substitution(7, 2, 37, 1));
        set1.setActingCaptain(TeamType.HOME, 1);
        set1.setActingCaptain(TeamType.GUEST, 2);

        RecordedSet set2 = new RecordedSet();
        recordedGame.getSets().add(set2);
        set2.setDuration(4540L);
        set2.setPoints(TeamType.HOME, 4);
        set2.setPoints(TeamType.GUEST, 3);
        set2.setTimeouts(TeamType.HOME, 2);
        set2.setTimeouts(TeamType.GUEST, 1);
        Collections.addAll(set2.getPointsLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.GUEST);
        set2.setServingTeam(TeamType.GUEST);
        for (int index = 1; index <= 6; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.HOME).add(player);
            set2.getStartingPlayers(TeamType.HOME).add(player);
            set2.getCurrentPlayers(TeamType.GUEST).add(player);
            set2.getStartingPlayers(TeamType.GUEST).add(player);
        }
        Collections.addAll(set2.getSubstitutions(TeamType.HOME), new Substitution(7, 1, 1, 53), new Substitution(4, 2, 0, 32));
        Collections.addAll(set2.getSubstitutions(TeamType.GUEST), new Substitution(10, 50, 1, 1));
        set2.setActingCaptain(TeamType.HOME, 1);
        set2.setActingCaptain(TeamType.GUEST, 2);

        recordedGame.getGivenSanctions(TeamType.HOME).add(new Sanction(5, SanctionType.YELLOW, 1, 12, 14));
        recordedGame.getGivenSanctions(TeamType.GUEST).add(new Sanction(8, SanctionType.RED, 3, 20, 1));

        return recordedGame;
    }

    private RecordedGame someRecordedGame2() {
        // TODO all new fields
        RecordedGame recordedGame = new RecordedGame();
        recordedGame.setGameType(GameType.BEACH);
        recordedGame.setGameDate(646516L);
        recordedGame.setGenderType(GenderType.GENTS);
        recordedGame.setMatchCompleted(false);
        recordedGame.setSets(TeamType.HOME, 1);
        recordedGame.setSets(TeamType.GUEST, 0);

        RecordedTeam team1 = recordedGame.getTeam(TeamType.HOME);
        team1.setName("Player A / Player B");
        team1.setColor(Color.parseColor("#1234ab"));
        Collections.addAll(team1.getPlayers(), 1, 2);

        RecordedTeam team2 = recordedGame.getTeam(TeamType.GUEST);
        team2.setName("Player C / Player D");
        team2.setColor(Color.parseColor("#efa1c3"));
        Collections.addAll(team2.getPlayers(), 1, 2);

        RecordedSet set1 = new RecordedSet();
        recordedGame.getSets().add(set1);
        set1.setDuration(12L);
        set1.setPoints(TeamType.HOME, 0);
        set1.setPoints(TeamType.GUEST, 2);
        set1.setTimeouts(TeamType.HOME, 1);
        set1.setTimeouts(TeamType.GUEST, 1);
        Collections.addAll(set1.getPointsLadder(), TeamType.GUEST, TeamType.GUEST);
        set1.setServingTeam(TeamType.GUEST);
        for (int index = 1; index <= 2; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getCurrentPlayers(TeamType.HOME).add(player);
        }
        for (int index = 1; index <= 1; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set1.getCurrentPlayers(TeamType.GUEST).add(player);
        }

        RecordedSet set2 = new RecordedSet();
        recordedGame.getSets().add(set2);
        set2.setDuration(0L);
        set2.setPoints(TeamType.HOME, 6);
        set2.setPoints(TeamType.GUEST, 2);
        set2.setTimeouts(TeamType.HOME, 0);
        set2.setTimeouts(TeamType.GUEST, 0);
        Collections.addAll(set2.getPointsLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.HOME);
        set2.setServingTeam(TeamType.HOME);
        for (int index = 1; index <= 2; index++) {
            RecordedPlayer player = new RecordedPlayer();
            player.setNumber(index);
            player.setPositionType(PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.HOME).add(player);
            set2.getCurrentPlayers(TeamType.GUEST).add(player);
        }

        recordedGame.getGivenSanctions(TeamType.HOME).add(new Sanction(-1, SanctionType.RED_DISQUALIFICATION, 2, 4, 8));
        recordedGame.getGivenSanctions(TeamType.GUEST).add(new Sanction(-1, SanctionType.RED_EXPULSION, 0, 5, 6));

        return recordedGame;
    }

}
