package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import com.tonkar.volleyballreferee.api.ApiLeague;
import com.tonkar.volleyballreferee.api.ApiSelectedLeague;
import com.tonkar.volleyballreferee.api.Authentication;
import com.tonkar.volleyballreferee.business.data.StoredLeagues;
import com.tonkar.volleyballreferee.business.data.StoredRules;
import com.tonkar.volleyballreferee.interfaces.GameType;
import com.tonkar.volleyballreferee.interfaces.data.StoredLeaguesService;
import com.tonkar.volleyballreferee.interfaces.data.StoredRulesService;
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
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class StoredLeaguesIOTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

    @Test
    public void save() {
        ApiSelectedLeague selectedLeague1 = new ApiSelectedLeague();
        selectedLeague1.setId(UUID.randomUUID().toString());
        selectedLeague1.setCreatedBy(Authentication.VBR_USER_ID);
        selectedLeague1.setCreatedAt(System.currentTimeMillis());
        selectedLeague1.setUpdatedAt(System.currentTimeMillis());
        selectedLeague1.setKind(GameType.INDOOR);
        selectedLeague1.setName("Test League");
        selectedLeague1.setDivision("Test division");

        ApiSelectedLeague selectedLeague2 = new ApiSelectedLeague();
        selectedLeague2.setId(UUID.randomUUID().toString());
        selectedLeague2.setCreatedBy(Authentication.VBR_USER_ID);
        selectedLeague2.setCreatedAt(System.currentTimeMillis());
        selectedLeague2.setUpdatedAt(System.currentTimeMillis());
        selectedLeague2.setKind(GameType.INDOOR);
        selectedLeague2.setName("Test League Bis");
        selectedLeague2.setDivision("Test division Bis");

        StoredLeaguesService storedLeaguesService = new StoredLeagues(mActivityRule.getActivity().getApplicationContext());
        storedLeaguesService.createAndSaveLeagueFrom(selectedLeague1);
        storedLeaguesService.createAndSaveLeagueFrom(selectedLeague2);
    }

    @Test
    public void writeThenRead() {
        StoredLeaguesService storedLeaguesService = new StoredLeagues(mActivityRule.getActivity().getApplicationContext());

        List<ApiLeague> expectedList = new ArrayList<>();
        expectedList.add(storedLeaguesService.getLeague(GameType.INDOOR, "Test League"));
        expectedList.add(storedLeaguesService.getLeague(GameType.INDOOR, "Test League Bis"));
        List<ApiLeague> actualList = new ArrayList<>();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            StoredLeagues.writeLeaguesStream(outputStream, expectedList);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            actualList = StoredLeagues.readLeaguesStream(inputStream);

        } catch (IOException e) {
            e.printStackTrace();
        }

        assertEquals(expectedList, actualList);
        assertNotEquals(0, actualList.size());
    }

    @Test
    public void clear() {
        StoredRulesService storedRulesService = new StoredRules(mActivityRule.getActivity().getApplicationContext());
        storedRulesService.deleteAllRules();
    }
}
