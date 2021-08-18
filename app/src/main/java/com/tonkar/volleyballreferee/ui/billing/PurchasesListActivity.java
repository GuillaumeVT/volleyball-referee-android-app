package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.billing.BillingListener;
import com.tonkar.volleyballreferee.engine.billing.BillingManager;
import com.tonkar.volleyballreferee.engine.billing.BillingService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;

public class PurchasesListActivity extends NavigationActivity implements BillingListener {

    private BillingService       mBillingService;
    private PurchasesListAdapter mPurchasesListAdapter;

    @Override
    protected String getToolbarTitle() {
        return getString(R.string.purchase);
    }

    @Override
    protected int getCheckedItem() {
        return R.id.action_purchase;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Tags.BILLING, "Create purchases list activity");
        setContentView(R.layout.activity_purchases_list);

        initNavigationMenu();

        mBillingService = new BillingManager(this);

        final ListView purchasesList = findViewById(R.id.purchases_list);
        mPurchasesListAdapter = new PurchasesListAdapter(this, getLayoutInflater(), mBillingService);
        purchasesList.setAdapter(mPurchasesListAdapter);

        mBillingService.addBillingListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingService.removeBillingListener(this);
    }

    @Override
    public void onPurchasesUpdated() {
        runOnUiThread(() -> mPurchasesListAdapter.updatePurchasesList());
    }
}
