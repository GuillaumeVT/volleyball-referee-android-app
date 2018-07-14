package com.tonkar.volleyballreferee;

import android.graphics.Color;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.data.RecordedTeam;
import com.tonkar.volleyballreferee.business.data.SavedTeams;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.team.BaseTeamService;
import com.tonkar.volleyballreferee.interfaces.team.GenderType;
import com.tonkar.volleyballreferee.interfaces.team.TeamType;
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
public class SavedTeamsIOTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void save() {
        ServicesProvider.getInstance().restoreSavedTeamsService(mActivityRule.getActivity());

        ServicesProvider.getInstance().getSavedTeamsService().createTeam(GameType.INDOOR);
        BaseTeamService service = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();

        service.setTeamName(TeamType.HOME, "BRAZIL");
        service.setTeamColor(TeamType.HOME, Color.parseColor("#f3bc07"));
        service.setLiberoColor(TeamType.HOME, Color.parseColor("#034694"));

        service.addPlayer(TeamType.HOME, 1);
        service.addPlayer(TeamType.HOME, 3);
        service.addPlayer(TeamType.HOME, 4);
        service.addPlayer(TeamType.HOME, 5);
        service.addPlayer(TeamType.HOME, 6);
        service.addPlayer(TeamType.HOME, 8);
        service.addPlayer(TeamType.HOME, 9);
        service.addPlayer(TeamType.HOME, 10);
        service.addPlayer(TeamType.HOME, 11);
        service.addPlayer(TeamType.HOME, 13);
        service.addPlayer(TeamType.HOME, 16);
        service.addPlayer(TeamType.HOME, 18);
        service.addPlayer(TeamType.HOME, 19);
        service.addPlayer(TeamType.HOME, 20);

        service.addLibero(TeamType.HOME, 6);
        service.addLibero(TeamType.HOME, 8);

        service.setCaptain(TeamType.HOME, 1);
        service.setGenderType(TeamType.HOME, GenderType.GENTS);

        ServicesProvider.getInstance().getSavedTeamsService().saveCurrentTeam();

        ServicesProvider.getInstance().getSavedTeamsService().createTeam(GameType.INDOOR);
        service = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();

        service.setTeamName(TeamType.HOME, "FRANCE");
        service.setTeamColor(TeamType.HOME, Color.parseColor("#034694"));
        service.setLiberoColor(TeamType.HOME, Color.parseColor("#bc0019"));

        service.addPlayer(TeamType.HOME, 2);
        service.addPlayer(TeamType.HOME, 5);
        service.addPlayer(TeamType.HOME, 6);
        service.addPlayer(TeamType.HOME, 8);
        service.addPlayer(TeamType.HOME, 9);
        service.addPlayer(TeamType.HOME, 10);
        service.addPlayer(TeamType.HOME, 11);
        service.addPlayer(TeamType.HOME, 12);
        service.addPlayer(TeamType.HOME, 14);
        service.addPlayer(TeamType.HOME, 16);
        service.addPlayer(TeamType.HOME, 17);
        service.addPlayer(TeamType.HOME, 18);
        service.addPlayer(TeamType.HOME, 20);
        service.addPlayer(TeamType.HOME, 21);

        service.addLibero(TeamType.HOME, 2);
        service.addLibero(TeamType.HOME, 20);

        service.setCaptain(TeamType.HOME, 6);
        service.setGenderType(TeamType.HOME, GenderType.GENTS);

        ServicesProvider.getInstance().getSavedTeamsService().saveCurrentTeam();
    }

    @Test
    public void writeThenRead() {
        ServicesProvider.getInstance().restoreSavedTeamsService(mActivityRule.getActivity());

        List<RecordedTeam> expectedList = new ArrayList<>();
        expectedList.add(ServicesProvider.getInstance().getSavedTeamsService().getSavedTeam(GameType.INDOOR,"BRAZIL", GenderType.GENTS));
        expectedList.add(ServicesProvider.getInstance().getSavedTeamsService().getSavedTeam(GameType.INDOOR,"FRANCE", GenderType.GENTS));
        List<RecordedTeam> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            SavedTeams.writeTeamsStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = SavedTeams.readTeamsStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }
}
