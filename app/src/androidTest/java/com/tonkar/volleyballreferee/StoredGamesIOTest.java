package com.tonkar.volleyballreferee;

import android.graphics.Color;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.api.*;
import com.tonkar.volleyballreferee.business.data.StoredGame;
import com.tonkar.volleyballreferee.business.data.StoredGames;
import com.tonkar.volleyballreferee.interfaces.GameStatus;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.sanction.SanctionType;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
import com.tonkar.volleyballreferee.business.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class StoredGamesIOTest {

    @Test
    public void writeThenRead_all() {
        List<StoredGame> expectedList = new ArrayList<>();
        expectedList.add(someStoredGame1());
        expectedList.add(someStoredGame2());
        List<StoredGame> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredGames.writeStoredGamesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredGames.readStoredGamesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

    @Test
    public void writeThenRead_one() {
        StoredGame storedGame1 = someStoredGame1();
        try {
            byte[] bytes1 = StoredGames.storedGameToByteArray(storedGame1);
            StoredGame readGame1 = StoredGames.byteArrayToStoredGame(bytes1);
            assertEquals(storedGame1, readGame1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StoredGame storedGame2 = someStoredGame2();
        try {
            byte[] bytes2 = StoredGames.storedGameToByteArray(storedGame2);
            StoredGame readGame2 = StoredGames.byteArrayToStoredGame(bytes2);
            assertEquals(storedGame2, readGame2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StoredGame someStoredGame1() {
        StoredGame game = new StoredGame();
        game.setId(UUID.randomUUID().toString());
        game.setCreatedBy(Authentication.VBR_USER_ID);
        game.setCreatedAt(1234L);
        game.setUpdatedAt(651234L);
        game.setScheduledAt(65432L);
        game.setKind(GameType.INDOOR);
        game.setGender(GenderType.LADIES);
        game.setRules(Rules.officialIndoorRules());
        game.setLeagueId(UUID.randomUUID().toString());
        game.setLeagueName("VBR League");
        game.setDivisionName("VBR Division");
        game.setRefereedBy(Authentication.VBR_USER_ID);
        game.setRefereeName("VBR");
        game.setMatchStatus(GameStatus.LIVE);
        game.setSets(TeamType.HOME, 0);
        game.setSets(TeamType.GUEST, 2);
        game.setScore("2-1");

        ApiTeam team1 = game.getTeam(TeamType.HOME);
        team1.setId(UUID.randomUUID().toString());
        team1.setCreatedBy(Authentication.VBR_USER_ID);
        team1.setCreatedAt(4869434L);
        team1.setUpdatedAt(1869694L);
        team1.setName("Team 1");
        team1.setColorInt(Color.parseColor("#123456"));
        team1.setLiberoColorInt(Color.parseColor("#ffffff"));
        Collections.addAll(team1.getPlayers(), new ApiPlayer(1), new ApiPlayer(3), new ApiPlayer(5), new ApiPlayer(7), new ApiPlayer(9), new ApiPlayer(11), new ApiPlayer(13));
        Collections.addAll(team1.getLiberos(), new ApiPlayer(2));
        team1.setCaptain(11);
        team1.setKind(GameType.INDOOR);

        ApiTeam team2 = game.getTeam(TeamType.GUEST);
        team2.setId(UUID.randomUUID().toString());
        team2.setCreatedBy(Authentication.VBR_USER_ID);
        team2.setCreatedAt(58594L);
        team2.setUpdatedAt(9578594L);
        team2.setName("Team 2");
        team2.setColorInt(Color.parseColor("#a1b2c3"));
        team2.setLiberoColorInt(Color.parseColor("#000000"));
        Collections.addAll(team2.getPlayers(), new ApiPlayer(2), new ApiPlayer(4), new ApiPlayer(6), new ApiPlayer(8), new ApiPlayer(10), new ApiPlayer(12), new ApiPlayer(14), new ApiPlayer(16), new ApiPlayer(18));
        team2.setCaptain(2);
        team2.setKind(GameType.INDOOR);

        ApiSet set1 = new ApiSet();
        game.getSets().add(set1);
        set1.setDuration(5555L);
        set1.setPoints(TeamType.HOME, 3);
        set1.setPoints(TeamType.GUEST, 2);
        set1.setTimeouts(TeamType.HOME, 1);
        set1.setTimeouts(TeamType.GUEST, 0);
        Collections.addAll(set1.getLadder(), TeamType.HOME, TeamType.GUEST, TeamType.GUEST, TeamType.HOME, TeamType.HOME);
        set1.setServing(TeamType.HOME);
        set1.setFirstServing(TeamType.GUEST);
        for (int index = 1; index <= 6; index++) {
            set1.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
        }
        for (int index = 1; index <= 4; index++) {
            set1.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }
        for (int index = 1; index <= 3; index++) {
            set1.getStartingPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
        }
        for (int index = 1; index <= 2; index++) {
            set1.getStartingPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }
        Collections.addAll(set1.getSubstitutions(TeamType.HOME), new ApiSubstitution(1, 5, 12, 10), new ApiSubstitution(7, 2, 37, 1));
        set1.setActingCaptain(TeamType.HOME, 1);
        set1.setActingCaptain(TeamType.GUEST, 2);

        ApiSet set2 = new ApiSet();
        game.getSets().add(set2);
        set2.setDuration(4540L);
        set2.setPoints(TeamType.HOME, 4);
        set2.setPoints(TeamType.GUEST, 3);
        set2.setTimeouts(TeamType.HOME, 2);
        set2.setTimeouts(TeamType.GUEST, 1);
        Collections.addAll(set2.getLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.GUEST);
        set2.setServing(TeamType.GUEST);
        set2.setFirstServing(TeamType.HOME);
        for (int index = 1; index <= 6; index++) {
            set2.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getStartingPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
            set2.getStartingPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }
        Collections.addAll(set2.getSubstitutions(TeamType.HOME), new ApiSubstitution(7, 1, 1, 53), new ApiSubstitution(4, 2, 0, 32));
        Collections.addAll(set2.getSubstitutions(TeamType.GUEST), new ApiSubstitution(10, 50, 1, 1));
        set2.setActingCaptain(TeamType.HOME, 1);
        set2.setActingCaptain(TeamType.GUEST, 2);

        game.getGivenSanctions(TeamType.HOME).add(new ApiSanction(SanctionType.YELLOW, 5, 1, 12, 14));
        game.getGivenSanctions(TeamType.GUEST).add(new ApiSanction(SanctionType.RED, 8, 3, 20, 1));

        return game;
    }

    private StoredGame someStoredGame2() {
        StoredGame game = new StoredGame();
        game.setId(UUID.randomUUID().toString());
        game.setCreatedBy(Authentication.VBR_USER_ID);
        game.setCreatedAt(646516L);
        game.setUpdatedAt(58473L);
        game.setScheduledAt(764578L);
        game.setKind(GameType.BEACH);
        game.setGender(GenderType.GENTS);
        game.setRules(Rules.officialBeachRules());
        game.setLeagueId(UUID.randomUUID().toString());
        game.setLeagueName("VBR Beach League");
        game.setDivisionName("VBR Beach Division");
        game.setRefereedBy(Authentication.VBR_USER_ID);
        game.setRefereeName("VBR");
        game.setMatchStatus(GameStatus.SCHEDULED);
        game.setSets(TeamType.HOME, 1);
        game.setSets(TeamType.GUEST, 0);
        game.setScore("21-19 5-2");

        ApiTeam team1 = game.getTeam(TeamType.HOME);
        team1.setId(UUID.randomUUID().toString());
        team1.setCreatedBy(Authentication.VBR_USER_ID);
        team1.setCreatedAt(5386831L);
        team1.setUpdatedAt(7069493L);
        team1.setName("Player A / Player B");
        team1.setColorInt(Color.parseColor("#1234ab"));
        Collections.addAll(team1.getPlayers(), new ApiPlayer(1), new ApiPlayer(2));
        team1.setKind(GameType.BEACH);

        ApiTeam team2 = game.getTeam(TeamType.GUEST);
        team2.setId(UUID.randomUUID().toString());
        team2.setCreatedBy(Authentication.VBR_USER_ID);
        team2.setCreatedAt(794302L);
        team2.setUpdatedAt(149593L);
        team2.setName("Player C / Player D");
        team2.setColorInt(Color.parseColor("#efa1c3"));
        Collections.addAll(team2.getPlayers(), new ApiPlayer(1), new ApiPlayer(2));
        team2.setKind(GameType.BEACH);

        ApiSet set1 = new ApiSet();
        game.getSets().add(set1);
        set1.setDuration(12L);
        set1.setPoints(TeamType.HOME, 0);
        set1.setPoints(TeamType.GUEST, 2);
        set1.setTimeouts(TeamType.HOME, 1);
        set1.setTimeouts(TeamType.GUEST, 1);
        Collections.addAll(set1.getLadder(), TeamType.GUEST, TeamType.GUEST);
        set1.setServing(TeamType.GUEST);
        set1.setFirstServing(TeamType.GUEST);
        for (int index = 1; index <= 2; index++) {
            set1.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
        }
        for (int index = 1; index <= 1; index++) {
            set1.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }

        ApiSet set2 = new ApiSet();
        game.getSets().add(set2);
        set2.setDuration(0L);
        set2.setPoints(TeamType.HOME, 6);
        set2.setPoints(TeamType.GUEST, 2);
        set2.setTimeouts(TeamType.HOME, 0);
        set2.setTimeouts(TeamType.GUEST, 0);
        Collections.addAll(set2.getLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST, TeamType.HOME);
        set2.setServing(TeamType.HOME);
        set2.setFirstServing(TeamType.HOME);
        for (int index = 1; index <= 2; index++) {
            set2.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }

        game.getGivenSanctions(TeamType.HOME).add(new ApiSanction(SanctionType.RED_DISQUALIFICATION, 200, 2, 4, 8));
        game.getGivenSanctions(TeamType.GUEST).add(new ApiSanction(SanctionType.RED_EXPULSION, 100, 0, 5, 6));

        return game;
    }

}
