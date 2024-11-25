package com.tonkar.volleyballreferee.engine.service;

import static org.junit.Assert.*;

import android.content.Context;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.tonkar.volleyballreferee.engine.api.model.*;
import com.tonkar.volleyballreferee.engine.game.GameType;

import org.junit.*;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.io.*;
import java.util.*;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoredLeaguesIOTest {

    private Context mContext;

    @Before
    public void init() {
        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    @Test
    public void save() {
        SelectedLeagueDto selectedLeague1 = new SelectedLeagueDto();
        selectedLeague1.setId(UUID.randomUUID().toString());
        selectedLeague1.setCreatedBy(null);
        selectedLeague1.setCreatedAt(System.currentTimeMillis());
        selectedLeague1.setUpdatedAt(System.currentTimeMillis());
        selectedLeague1.setKind(GameType.INDOOR);
        selectedLeague1.setName("Test League");
        selectedLeague1.setDivision("Test division");

        SelectedLeagueDto selectedLeague2 = new SelectedLeagueDto();
        selectedLeague2.setId(UUID.randomUUID().toString());
        selectedLeague2.setCreatedBy(null);
        selectedLeague2.setCreatedAt(System.currentTimeMillis());
        selectedLeague2.setUpdatedAt(System.currentTimeMillis());
        selectedLeague2.setKind(GameType.INDOOR);
        selectedLeague2.setName("Test League Bis");
        selectedLeague2.setDivision("Test division Bis");

        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext.getApplicationContext());
        storedLeaguesService.createAndSaveLeagueFrom(selectedLeague1);
        storedLeaguesService.createAndSaveLeagueFrom(selectedLeague2);
    }

    @Test
    public void writeThenRead() {
        StoredLeaguesService storedLeaguesService = new StoredLeaguesManager(mContext.getApplicationContext());

        List<LeagueDto> expectedList = new ArrayList<>();
        expectedList.add(storedLeaguesService.getLeague(GameType.INDOOR, "Test League"));
        expectedList.add(storedLeaguesService.getLeague(GameType.INDOOR, "Test League Bis"));
        List<LeagueDto> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredLeaguesManager.writeLeaguesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredLeaguesManager.readLeaguesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }
}
