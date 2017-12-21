package com.tonkar.volleyballreferee;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.ContextCompat;

import com.tonkar.volleyballreferee.business.ServicesProvider;
import com.tonkar.volleyballreferee.business.history.JsonHistoryReader;
import com.tonkar.volleyballreferee.business.history.JsonHistoryWriter;
import com.tonkar.volleyballreferee.business.history.SavedTeam;
import com.tonkar.volleyballreferee.interfaces.BaseIndoorTeamService;
import com.tonkar.volleyballreferee.interfaces.GenderType;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.ui.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class SavedTeamsIOTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void save() {
        ServicesProvider.getInstance().restoreSavedTeamsService(mActivityRule.getActivity());

        ServicesProvider.getInstance().getSavedTeamsService().createTeam();
        BaseIndoorTeamService service = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();

        service.setTeamName(TeamType.HOME, "BRAZIL");
        service.setTeamColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt10));
        service.setLiberoColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt3));

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

        ServicesProvider.getInstance().getSavedTeamsService().createTeam();
        service = ServicesProvider.getInstance().getSavedTeamsService().getCurrentTeam();

        service.setTeamName(TeamType.HOME, "FRANCE");
        service.setTeamColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt3));
        service.setLiberoColor(TeamType.HOME, ContextCompat.getColor(mActivityRule.getActivity(), R.color.colorShirt8));

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

        List<SavedTeam> expectedList = new ArrayList<>();
        expectedList.add((SavedTeam) ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamService("BRAZIL", GenderType.GENTS));
        expectedList.add((SavedTeam) ServicesProvider.getInstance().getSavedTeamsService().getSavedTeamService("FRANCE", GenderType.GENTS));
        List<SavedTeam> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            JsonHistoryWriter.writeSavedTeamsStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = JsonHistoryReader.readSavedTeamsStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }
}
