package com.tonkar.volleyballreferee.interfaces;

public interface GeneralService extends BaseGeneralService {

    void addGeneralListener(GeneralListener listener);

    void removeGeneralListener(GeneralListener listener);

    void startMatch();
}
