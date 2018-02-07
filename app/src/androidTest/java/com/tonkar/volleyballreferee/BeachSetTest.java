package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.BeachSet;
import com.tonkar.volleyballreferee.interfaces.TeamType;
import com.tonkar.volleyballreferee.rules.Rules;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class BeachSetTest {

    @Test
    public void point_add() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 8, TeamType.GUEST);
        assertEquals(true, set.getPointsLadder().isEmpty());
        assertEquals(1, set.addPoint(TeamType.HOME));
        assertEquals(TeamType.HOME, set.getPointsLadder().get(0));
        assertEquals(2, set.addPoint(TeamType.HOME));
        assertEquals(TeamType.HOME, set.getPointsLadder().get(1));
        assertEquals(1, set.addPoint(TeamType.GUEST));
        assertEquals(TeamType.GUEST, set.getPointsLadder().get(2));
        assertEquals(3, set.addPoint(TeamType.HOME));
        assertEquals(TeamType.HOME, set.getPointsLadder().get(3));
    }

    @Test
    public void point_remove() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 12, TeamType.HOME);
        set.addPoint(TeamType.HOME);
        set.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, set.removeLastPoint());
        assertEquals(TeamType.HOME, set.getPointsLadder().get(0));
        assertEquals(TeamType.HOME, set.removeLastPoint());
        assertEquals(true, set.getPointsLadder().isEmpty());
        assertEquals(null, set.removeLastPoint());
    }

    @Test
    public void team_leading() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 5, TeamType.HOME);

        set.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, set.getLeadingTeam());

        set.addPoint(TeamType.GUEST);
        set.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getLeadingTeam());
    }

    @Test
    public void team_serving() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 7, TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getServingTeam());

        set.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getServingTeam());

        set.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, set.getServingTeam());
    }

    @Test
    public void winSet_normal() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 4, TeamType.HOME);

        for (int index = 0; index < 4; index++) {
            assertEquals(false, set.isSetCompleted());
            set.addPoint(TeamType.GUEST);
        }

        assertEquals(true, set.isSetCompleted());
    }

    @Test
    public void winSet_2PointsGap() {
        BeachSet set = new BeachSet(Rules.OFFICIAL_BEACH_RULES, 3, TeamType.HOME);

        for (int index = 0; index < 2; index++) {
            assertEquals(false, set.isSetCompleted());
            set.addPoint(TeamType.GUEST);
            set.addPoint(TeamType.HOME);
        }

        set.addPoint(TeamType.HOME);
        assertEquals(false, set.isSetCompleted());
        assertEquals(true, set.isSetPoint());
        set.addPoint(TeamType.HOME);
        assertEquals(true, set.isSetCompleted());
    }

    @Test
    public void winSet_1PointGap() {
        Rules rules = new Rules(3, 21, true, false, true, 1, 30,
                true, 30, true, 180, 0, true, 9999);
        BeachSet set = new BeachSet(rules, rules.getPointsPerSet(), TeamType.GUEST);

        for (int index = 0; index < 20; index++) {
            assertEquals(false, set.isSetCompleted());
            set.addPoint(TeamType.GUEST);
            set.addPoint(TeamType.HOME);
        }

        assertEquals(true, set.isSetPoint());
        set.addPoint(TeamType.GUEST);
        assertEquals(true, set.isSetCompleted());
    }
}
