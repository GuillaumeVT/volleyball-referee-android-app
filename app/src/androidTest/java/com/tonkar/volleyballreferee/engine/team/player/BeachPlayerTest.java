package com.tonkar.volleyballreferee.engine.team.player;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BeachPlayerTest {

    @Test
    public void rotation_next() {
        BeachPlayer player = new BeachPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_previous() {
        BeachPlayer player = new BeachPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_bench() {
        BeachPlayer player = new BeachPlayer(1);
        assertEquals(PositionType.BENCH, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

    @Test
    public void rotation_abnormal() {
        BeachPlayer player = new BeachPlayer(1);
        player.setPosition(PositionType.POSITION_4);

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

}
