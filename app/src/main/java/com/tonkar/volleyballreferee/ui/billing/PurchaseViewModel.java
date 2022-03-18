package com.tonkar.volleyballreferee.ui.billing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.tonkar.volleyballreferee.engine.billing.BillingListener;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

import java.util.ArrayList;
import java.util.List;

public class PurchaseViewModel extends AndroidViewModel implements BillingListener {

    private       BillingService                    mBillingService;
    private final MutableLiveData<List<SkuDetails>> mPurchaseList;

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
        mPurchaseList = new MutableLiveData<>(new ArrayList<>());
    }

    LiveData<List<SkuDetails>> getPurchaseList() {
        return mPurchaseList;
    }

    @Override
    public void onPurchasesUpdated() {
        updatePurchaseList();
    }

    void init(BillingService billingService) {
        mBillingService = billingService;
        updatePurchaseList();
    }

    void updatePurchaseList() {
        List<SkuDetails> purchaseList = new ArrayList<>();

        for (SkuDetails skuDetails : mBillingService.getSkuDetailsList(BillingClient.SkuType.SUBS)) {
            if (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(skuDetails.getSku())) {
                purchaseList.add(skuDetails);
            }
        }
        for (SkuDetails skuDetails : mBillingService.getSkuDetailsList(BillingClient.SkuType.INAPP)) {
            if (BillingService.SCORE_SHEETS.equals(skuDetails.getSku())) {
                purchaseList.add(skuDetails);
            }
        }

        mPurchaseList.postValue(purchaseList);
    }
}