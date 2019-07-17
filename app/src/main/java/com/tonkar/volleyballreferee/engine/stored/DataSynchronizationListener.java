package com.tonkar.volleyballreferee.engine.stored;

public interface DataSynchronizationListener {

    void onSynchronizationSucceeded();

    void onSynchronizationFailed();
}
