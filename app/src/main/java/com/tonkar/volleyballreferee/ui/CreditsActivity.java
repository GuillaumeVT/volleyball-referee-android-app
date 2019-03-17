package com.tonkar.volleyballreferee.ui;

import android.os.Bundle;
import com.tonkar.volleyballreferee.R;

public class CreditsActivity extends NavigationActivity {

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.app_name);
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_credits;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_credits);

        initNavigationMenu();
    }

}
