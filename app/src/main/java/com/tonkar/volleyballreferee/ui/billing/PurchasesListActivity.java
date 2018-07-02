package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.billing.BillingManager;
import com.tonkar.volleyballreferee.interfaces.billing.BillingListener;
import com.tonkar.volleyballreferee.interfaces.billing.BillingService;

public class PurchasesListActivity extends AppCompatActivity implements BillingListener {

    private static final String TAG = "VBR-BuyActivity";

    private BillingService       mBillingService;
    private PurchasesListAdapter mPurchasesListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Create purchases list activity");
        setContentView(R.layout.activity_purchases_list);

        setTitle(getResources().getString(R.string.purchase));

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
        mPurchasesListAdapter.updatePurchasesList();
    }
}
