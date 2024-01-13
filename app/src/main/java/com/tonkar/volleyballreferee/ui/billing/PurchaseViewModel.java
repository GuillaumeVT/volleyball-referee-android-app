package com.tonkar.volleyballreferee.ui.billing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.*;

import com.android.billingclient.api.*;
import com.tonkar.volleyballreferee.engine.billing.*;

import java.util.*;

public class PurchaseViewModel extends AndroidViewModel implements BillingListener {

    private       BillingService                        mBillingService;
    private final MutableLiveData<List<ProductDetails>> mPurchaseList;

    public PurchaseViewModel(@NonNull Application application) {
        super(application);
        mPurchaseList = new MutableLiveData<>(new ArrayList<>());
    }

    LiveData<List<ProductDetails>> getPurchaseList() {
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
        List<ProductDetails> purchaseList = new ArrayList<>();

        for (ProductDetails productDetails : mBillingService.getProductDetailList(BillingClient.ProductType.SUBS)) {
            if (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(productDetails.getProductId())) {
                purchaseList.add(productDetails);
            }
        }

        mPurchaseList.postValue(purchaseList);
    }
}