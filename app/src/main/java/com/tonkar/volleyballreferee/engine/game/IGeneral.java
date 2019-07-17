package com.tonkar.volleyballreferee.engine.game;

public interface IGeneral extends IBaseGeneral {

    void addGeneralListener(GeneralListener listener);

    void removeGeneralListener(GeneralListener listener);

    void startMatch();

    void resetCurrentSet();
}
