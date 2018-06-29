package com.tonkar.volleyballreferee.interfaces.data;

public interface WebGamesService {

    boolean isGameIndexed(long gameDate);

    void toggleGameIndexed(long gameDate);

    void getGameFromCode(int code, AsyncGameRequestListener listener);

    void getUserGame(final String userId, final long id, final AsyncGameRequestListener listener);

    void getUserScheduledGames(final String userId, final AsyncGameRequestListener listener);
}
