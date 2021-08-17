package com.tonkar.volleyballreferee.engine.service;

public interface DataSynchronizationListener {

    void onSynchronizationSucceeded();

    void onSynchronizationFailed();
}
