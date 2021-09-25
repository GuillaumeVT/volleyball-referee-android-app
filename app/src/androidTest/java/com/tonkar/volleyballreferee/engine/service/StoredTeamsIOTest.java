package com.tonkar.volleyballreferee.engine.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.graphics.Color;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.api.model.ApiTeam;
import com.tonkar.volleyballreferee.engine.game.GameType;
import com.tonkar.volleyballreferee.engine.team.GenderType;
import com.tonkar.volleyballreferee.engine.team.IBaseTeam;
import com.tonkar.volleyballreferee.engine.team.TeamType;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoredTeamsIOTest {

    private Context mContext;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void save() {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mContext.getApplicationContext());

        IBaseTeam teamService = storedTeamsService.createTeam(GameType.INDOOR);
        teamService.setTeamName(TeamType.HOME, "BRAZIL");
        teamService.setTeamColor(TeamType.HOME, Color.parseColor("#f3bc07"));
        teamService.setLiberoColor(TeamType.HOME, Color.parseColor("#034694"));

        teamService.addPlayer(TeamType.HOME, 1);
        teamService.addPlayer(TeamType.HOME, 3);
        teamService.addPlayer(TeamType.HOME, 4);
        teamService.addPlayer(TeamType.HOME, 5);
        teamService.addPlayer(TeamType.HOME, 6);
        teamService.addPlayer(TeamType.HOME, 8);
        teamService.addPlayer(TeamType.HOME, 9);
        teamService.addPlayer(TeamType.HOME, 10);
        teamService.addPlayer(TeamType.HOME, 11);
        teamService.addPlayer(TeamType.HOME, 13);
        teamService.addPlayer(TeamType.HOME, 16);
        teamService.addPlayer(TeamType.HOME, 18);
        teamService.addPlayer(TeamType.HOME, 19);
        teamService.addPlayer(TeamType.HOME, 20);

        teamService.addLibero(TeamType.HOME, 6);
        teamService.addLibero(TeamType.HOME, 8);

        teamService.setCaptain(TeamType.HOME, 1);
        teamService.setGender(TeamType.HOME, GenderType.GENTS);

        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR, teamService, TeamType.HOME);

        teamService = storedTeamsService.createTeam(GameType.INDOOR);
        teamService.setTeamName(TeamType.HOME, "FRANCE");
        teamService.setTeamColor(TeamType.HOME, Color.parseColor("#034694"));
        teamService.setLiberoColor(TeamType.HOME, Color.parseColor("#bc0019"));

        teamService.addPlayer(TeamType.HOME, 2);
        teamService.addPlayer(TeamType.HOME, 5);
        teamService.addPlayer(TeamType.HOME, 6);
        teamService.addPlayer(TeamType.HOME, 8);
        teamService.addPlayer(TeamType.HOME, 9);
        teamService.addPlayer(TeamType.HOME, 10);
        teamService.addPlayer(TeamType.HOME, 11);
        teamService.addPlayer(TeamType.HOME, 12);
        teamService.addPlayer(TeamType.HOME, 14);
        teamService.addPlayer(TeamType.HOME, 16);
        teamService.addPlayer(TeamType.HOME, 17);
        teamService.addPlayer(TeamType.HOME, 18);
        teamService.addPlayer(TeamType.HOME, 20);
        teamService.addPlayer(TeamType.HOME, 21);

        teamService.addLibero(TeamType.HOME, 2);
        teamService.addLibero(TeamType.HOME, 20);

        teamService.setCaptain(TeamType.HOME, 6);
        teamService.setGender(TeamType.HOME, GenderType.GENTS);

        storedTeamsService.createAndSaveTeamFrom(GameType.INDOOR, teamService, TeamType.HOME);
    }

    @Test
    public void writeThenRead() {
        StoredTeamsService storedTeamsService = new StoredTeamsManager(mContext.getApplicationContext());

        List<ApiTeam> expectedList = new ArrayList<>();
        expectedList.add(storedTeamsService.getTeam(GameType.INDOOR,"BRAZIL", GenderType.GENTS));
        expectedList.add(storedTeamsService.getTeam(GameType.INDOOR,"FRANCE", GenderType.GENTS));
        List<ApiTeam> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredTeamsManager.writeTeamsStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredTeamsManager.readTeamsStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

}
