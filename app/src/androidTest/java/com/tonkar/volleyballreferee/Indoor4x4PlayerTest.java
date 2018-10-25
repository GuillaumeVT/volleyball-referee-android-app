package com.tonkar.volleyballreferee;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.Indoor4x4Player;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

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
