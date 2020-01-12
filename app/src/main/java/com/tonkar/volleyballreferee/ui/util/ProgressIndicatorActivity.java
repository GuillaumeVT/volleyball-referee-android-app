package com.tonkar.volleyballreferee.ui.util;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public abstract class ProgressIndicatorActivity extends AppCompatActivity {

    protected SwipeRefreshLayout mSyncLayout;

    public void showProgressIndicator() {
        mSyncLayout.setRefreshing(true);
    }

    public void hideProgressIndicator() {
        mSyncLayout.setRefreshing(false);
    }

}
