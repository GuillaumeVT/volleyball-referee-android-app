package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.util.Log;

import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.Tags;
import com.tonkar.volleyballreferee.engine.billing.BillingManager;
import com.tonkar.volleyballreferee.engine.billing.BillingService;
import com.tonkar.volleyballreferee.ui.NavigationActivity;

public class PurchasesListActivity extends NavigationActivity {

    private BillingService    mBillingService;
    private PurchaseViewModel mPurchaseViewModel;

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

        final RecyclerView purchaseList = findViewById(R.id.purchases_list);
        final PurchasesListAdapter purchasesListAdapter = new PurchasesListAdapter(new PurchasesListAdapter.PurchaseDiff(), mBillingService);
        purchasesListAdapter.setHasStableIds(true);
        purchaseList.setAdapter(purchasesListAdapter);
        purchaseList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        mPurchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);
        mPurchaseViewModel.init(mBillingService);
        mPurchaseViewModel
                .getPurchaseList()
                .observe(this, purchasesListAdapter::submitList);
        mBillingService.addBillingListener(mPurchaseViewModel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBillingService.removeBillingListener(mPurchaseViewModel);
    }
}
