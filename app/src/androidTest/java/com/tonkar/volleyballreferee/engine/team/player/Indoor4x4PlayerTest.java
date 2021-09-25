package com.tonkar.volleyballreferee.engine.team.player;

import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class Indoor4x4PlayerTest {

    @Test
    public void rotation_next() {
        Indoor4x4Player player = new Indoor4x4Player(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_4, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_3, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_previous() {
        Indoor4x4Player player = new Indoor4x4Player(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_3, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_4, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_bench() {
        Indoor4x4Player player = new Indoor4x4Player(1);
        assertEquals(PositionType.BENCH, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

}
