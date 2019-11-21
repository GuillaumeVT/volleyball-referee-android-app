package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.engine.team.player.PositionType;
import com.tonkar.volleyballreferee.engine.team.player.SnowPlayer;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class SnowPlayerTest {

    @Test
    public void rotation_next() {
        SnowPlayer player = new SnowPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_3, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_previous() {
        SnowPlayer player = new SnowPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_3, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_bench() {
        SnowPlayer player = new SnowPlayer(1);
        assertEquals(PositionType.BENCH, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

    @Test
    public void rotation_abnormal() {
        SnowPlayer player = new SnowPlayer(1);
        player.setPosition(PositionType.POSITION_6);

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

}
