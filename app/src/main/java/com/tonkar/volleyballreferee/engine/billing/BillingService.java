package com.tonkar.volleyballreferee.engine.billing;

import com.android.billingclient.api.SkuDetails;

import java.util.List;

public interface BillingService {

    String WEB_PREMIUM              = "vbr_web_premium";
    String WEB_PREMIUM_SUBSCRIPTION = "vbr_web_premium_sub";
    String SCORE_SHEETS             = "vbr_score_sheets";

    void addBillingListener(BillingListener listener);

    void removeBillingListener(BillingListener listener);

    void executeServiceRequest(String skuType, Runnable runnable);

    List<SkuDetails> getSkuDetailsList(String skuType);

    boolean isPurchased(String skuType, String sku);

    boolean isAllPurchased();

    void launchPurchase(String skuType, SkuDetails skuDetails);
}
