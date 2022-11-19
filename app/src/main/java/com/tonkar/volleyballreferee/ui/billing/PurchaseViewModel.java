package com.tonkar.volleyballreferee.ui.billing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.ProductDetails;
import com.tonkar.volleyballreferee.engine.billing.BillingListener;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

import java.util.ArrayList;
import java.util.List;

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
        for (ProductDetails productDetails : mBillingService.getProductDetailList(BillingClient.ProductType.INAPP)) {
            if (BillingService.SCORE_SHEETS.equals(productDetails.getProductId())) {
                purchaseList.add(productDetails);
            }
        }

        mPurchaseList.postValue(purchaseList);
    }
}