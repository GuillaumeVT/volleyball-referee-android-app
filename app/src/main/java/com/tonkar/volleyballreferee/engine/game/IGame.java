package com.tonkar.volleyballreferee.engine.game;

import com.tonkar.volleyballreferee.engine.game.sanction.ISanction;
import com.tonkar.volleyballreferee.engine.game.score.IScore;
import com.tonkar.volleyballreferee.engine.game.timeout.ITimeout;
import com.tonkar.volleyballreferee.engine.stored.IStoredGame;
import com.tonkar.volleyballreferee.engine.team.ITeam;

public interface IGame extends IGeneral, IScore, ITeam, ITimeout, ISanction {

    boolean areNotificationsEnabled();

    void restoreGame(IStoredGame storedGame);

}
