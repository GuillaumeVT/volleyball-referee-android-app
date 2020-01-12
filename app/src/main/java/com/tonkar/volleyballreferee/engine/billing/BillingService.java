package com.tonkar.volleyballreferee.engine.billing;

import com.android.billingclient.api.SkuDetails;

import java.util.List;

public interface BillingService {

    String WEB_PREMIUM = "vbr_web_premium";
    String SCORE_SHEETS = "vbr_score_sheets";

    void addBillingListener(BillingListener listener);

    void removeBillingListener(BillingListener listener);

    void executeServiceRequest(Runnable runnable);

    List<SkuDetails> getSkuDetailsList();

    boolean isPurchased(String sku);

    boolean isAllPurchased();

    void launchPurchase(SkuDetails skuDetails);
}
