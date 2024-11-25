package com.tonkar.volleyballreferee.engine.service;

import static org.junit.Assert.*;

import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.*;
import com.tonkar.volleyballreferee.engine.game.sanction.SanctionType;
import com.tonkar.volleyballreferee.engine.rules.Rules;
import com.tonkar.volleyballreferee.engine.team.*;
import com.tonkar.volleyballreferee.engine.team.player.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.*;
import java.util.*;

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
            StoredGamesManager.writeStoredGamesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredGamesManager.readStoredGamesStream(inputStream);

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
            byte[] bytes1 = StoredGamesManager.storedGameToByteArray(storedGame1);
            StoredGame readGame1 = StoredGamesManager.byteArrayToStoredGame(bytes1);
            assertEquals(storedGame1, readGame1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        StoredGame storedGame2 = someStoredGame2();
        try {
            byte[] bytes2 = StoredGamesManager.storedGameToByteArray(storedGame2);
            StoredGame readGame2 = StoredGamesManager.byteArrayToStoredGame(bytes2);
            assertEquals(storedGame2, readGame2);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private StoredGame someStoredGame1() {
        StoredGame game = new StoredGame();
        game.setId(UUID.randomUUID().toString());
        game.setCreatedBy(null);
        game.setCreatedAt(1234L);
        game.setUpdatedAt(651234L);
        game.setScheduledAt(65432L);
        game.setKind(GameType.INDOOR);
        game.setGender(GenderType.LADIES);
        game.setRules(Rules.officialIndoorRules());
        game.setLeague(new SelectedLeagueDto());
        game.getLeague().setName("VBR League");
        game.getLeague().setDivision("VBR Division");
        game.setRefereedBy(null);
        game.setRefereeName("VBR");
        game.setMatchStatus(GameStatus.LIVE);
        game.setSets(TeamType.HOME, 0);
        game.setSets(TeamType.GUEST, 2);
        game.setScore("2-1");

        TeamDto team1 = game.getTeam(TeamType.HOME);
        team1.setId(UUID.randomUUID().toString());
        team1.setCreatedBy(null);
        team1.setCreatedAt(4869434L);
        team1.setUpdatedAt(1869694L);
        team1.setName("Team 1");
        team1.setColorInt(Color.parseColor("#123456"));
        team1.setLiberoColorInt(Color.parseColor("#ffffff"));
        Collections.addAll(team1.getPlayers(), new PlayerDto(1), new PlayerDto(3), new PlayerDto(5), new PlayerDto(7), new PlayerDto(9),
                           new PlayerDto(11), new PlayerDto(13));
        Collections.addAll(team1.getLiberos(), new PlayerDto(2));
        team1.setCaptain(11);
        team1.setKind(GameType.INDOOR);

        TeamDto team2 = game.getTeam(TeamType.GUEST);
        team2.setId(UUID.randomUUID().toString());
        team2.setCreatedBy(null);
        team2.setCreatedAt(58594L);
        team2.setUpdatedAt(9578594L);
        team2.setName("Team 2");
        team2.setColorInt(Color.parseColor("#a1b2c3"));
        team2.setLiberoColorInt(Color.parseColor("#000000"));
        Collections.addAll(team2.getPlayers(), new PlayerDto(2), new PlayerDto(4), new PlayerDto(6), new PlayerDto(8), new PlayerDto(10),
                           new PlayerDto(12), new PlayerDto(14), new PlayerDto(16), new PlayerDto(18));
        team2.setCaptain(2);
        team2.setKind(GameType.INDOOR);

        SetDto set1 = new SetDto();
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
        Collections.addAll(set1.getSubstitutions(TeamType.HOME), new SubstitutionDto(1, 5, 12, 10), new SubstitutionDto(7, 2, 37, 1));
        set1.setGameCaptain(TeamType.HOME, 1);
        set1.setGameCaptain(TeamType.GUEST, 2);

        SetDto set2 = new SetDto();
        game.getSets().add(set2);
        set2.setDuration(4540L);
        set2.setPoints(TeamType.HOME, 4);
        set2.setPoints(TeamType.GUEST, 3);
        set2.setTimeouts(TeamType.HOME, 2);
        set2.setTimeouts(TeamType.GUEST, 1);
        Collections.addAll(set2.getLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME, TeamType.GUEST,
                           TeamType.GUEST);
        set2.setServing(TeamType.GUEST);
        set2.setFirstServing(TeamType.HOME);
        for (int index = 1; index <= 6; index++) {
            set2.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getStartingPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
            set2.getStartingPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }
        Collections.addAll(set2.getSubstitutions(TeamType.HOME), new SubstitutionDto(7, 1, 1, 53), new SubstitutionDto(4, 2, 0, 32));
        Collections.addAll(set2.getSubstitutions(TeamType.GUEST), new SubstitutionDto(10, 50, 1, 1));
        set2.setGameCaptain(TeamType.HOME, 1);
        set2.setGameCaptain(TeamType.GUEST, 2);

        game.getAllSanctions(TeamType.HOME).add(new SanctionDto(SanctionType.YELLOW, 5, 1, 12, 14));
        game.getAllSanctions(TeamType.GUEST).add(new SanctionDto(SanctionType.RED, 8, 3, 20, 1));

        return game;
    }

    private StoredGame someStoredGame2() {
        StoredGame game = new StoredGame();
        game.setId(UUID.randomUUID().toString());
        game.setCreatedBy(null);
        game.setCreatedAt(646516L);
        game.setUpdatedAt(58473L);
        game.setScheduledAt(764578L);
        game.setKind(GameType.BEACH);
        game.setGender(GenderType.GENTS);
        game.setRules(Rules.officialBeachRules());
        game.setLeague(new SelectedLeagueDto());
        game.getLeague().setName("VBR Beach League");
        game.getLeague().setDivision("VBR Beach Division");
        game.setRefereedBy(null);
        game.setRefereeName("VBR");
        game.setMatchStatus(GameStatus.SCHEDULED);
        game.setSets(TeamType.HOME, 1);
        game.setSets(TeamType.GUEST, 0);
        game.setScore("21-19 5-2");

        TeamDto team1 = game.getTeam(TeamType.HOME);
        team1.setId(UUID.randomUUID().toString());
        team1.setCreatedBy(null);
        team1.setCreatedAt(5386831L);
        team1.setUpdatedAt(7069493L);
        team1.setName("Player A / Player B");
        team1.setColorInt(Color.parseColor("#1234ab"));
        Collections.addAll(team1.getPlayers(), new PlayerDto(1), new PlayerDto(2));
        team1.setKind(GameType.BEACH);

        TeamDto team2 = game.getTeam(TeamType.GUEST);
        team2.setId(UUID.randomUUID().toString());
        team2.setCreatedBy(null);
        team2.setCreatedAt(794302L);
        team2.setUpdatedAt(149593L);
        team2.setName("Player C / Player D");
        team2.setColorInt(Color.parseColor("#efa1c3"));
        Collections.addAll(team2.getPlayers(), new PlayerDto(1), new PlayerDto(2));
        team2.setKind(GameType.BEACH);

        SetDto set1 = new SetDto();
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

        SetDto set2 = new SetDto();
        game.getSets().add(set2);
        set2.setDuration(0L);
        set2.setPoints(TeamType.HOME, 6);
        set2.setPoints(TeamType.GUEST, 2);
        set2.setTimeouts(TeamType.HOME, 0);
        set2.setTimeouts(TeamType.GUEST, 0);
        Collections.addAll(set2.getLadder(), TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.HOME, TeamType.GUEST, TeamType.HOME,
                           TeamType.GUEST, TeamType.HOME);
        set2.setServing(TeamType.HOME);
        set2.setFirstServing(TeamType.HOME);
        for (int index = 1; index <= 2; index++) {
            set2.getCurrentPlayers(TeamType.HOME).setPlayerAt(index, PositionType.fromInt(index));
            set2.getCurrentPlayers(TeamType.GUEST).setPlayerAt(index, PositionType.fromInt(index));
        }

        game.getAllSanctions(TeamType.HOME).add(new SanctionDto(SanctionType.RED_DISQUALIFICATION, 200, 2, 4, 8));
        game.getAllSanctions(TeamType.GUEST).add(new SanctionDto(SanctionType.RED_EXPULSION, 100, 0, 5, 6));

        return game;
    }

}
