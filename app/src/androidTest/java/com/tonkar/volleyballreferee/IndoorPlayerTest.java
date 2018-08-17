package com.tonkar.volleyballreferee;

import androidx.test.runner.AndroidJUnit4;

import com.tonkar.volleyballreferee.business.team.IndoorPlayer;
import com.tonkar.volleyballreferee.interfaces.team.PositionType;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class IndoorPlayerTest {

    @Test
    public void rotation_next() {
        IndoorPlayer player = new IndoorPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_6, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.POSITION_5, player.getPosition());

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
        IndoorPlayer player = new IndoorPlayer(1);
        player.setPosition(PositionType.POSITION_1);

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_2, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_3, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_4, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_5, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_6, player.getPosition());

        player.turnToPreviousPosition();
        assertEquals(PositionType.POSITION_1, player.getPosition());
    }

    @Test
    public void rotation_bench() {
        IndoorPlayer player = new IndoorPlayer(1);
        assertEquals(PositionType.BENCH, player.getPosition());

        player.turnToNextPosition();
        assertEquals(PositionType.BENCH, player.getPosition());
    }

}
