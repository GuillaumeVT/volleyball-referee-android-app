package com.tonkar.volleyballreferee;

import android.support.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.game.GameSet;
import com.tonkar.volleyballreferee.interfaces.TeamType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GameSetTest {

    @Test
    public void point_add() {
        GameSet set = new GameSet(8, 0, TeamType.GUEST);
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
        GameSet set = new GameSet(12, 0, TeamType.HOME);
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
        GameSet set = new GameSet(5, 2, TeamType.HOME);

        set.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, set.getLeadingTeam());

        set.addPoint(TeamType.GUEST);
        set.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getLeadingTeam());
    }

    @Test
    public void team_serving() {
        GameSet set = new GameSet(7, 1, TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getServingTeam());

        set.addPoint(TeamType.GUEST);
        assertEquals(TeamType.GUEST, set.getServingTeam());

        set.addPoint(TeamType.HOME);
        assertEquals(TeamType.HOME, set.getServingTeam());
    }

    @Test
    public void winSet_normal() {
        GameSet set = new GameSet(4, 0, TeamType.HOME);

        for (int index = 0; index < 4; index++) {
            assertEquals(false, set.isSetCompleted());
            set.addPoint(TeamType.GUEST);
        }

        assertEquals(true, set.isSetCompleted());
    }

    @Test
    public void winSet_2PointsGap() {
        GameSet set = new GameSet(3, 0, TeamType.HOME);

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
}
