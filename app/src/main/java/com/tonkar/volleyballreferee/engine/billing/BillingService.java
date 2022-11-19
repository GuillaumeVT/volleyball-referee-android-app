package com.tonkar.volleyballreferee.engine.billing;

import com.android.billingclient.api.ProductDetails;

import java.util.List;

public interface BillingService {

    String WEB_PREMIUM              = "vbr_web_premium";
    String WEB_PREMIUM_SUBSCRIPTION = "vbr_web_premium_sub";
    String SCORE_SHEETS             = "vbr_score_sheets";

    void addBillingListener(BillingListener listener);

    void removeBillingListener(BillingListener listener);

    void executeServiceRequest(String productType, Runnable runnable);

    List<ProductDetails> getProductDetailList(String productType);

    boolean isPurchased(String productType, String product);

    void launchPurchase(String productType, ProductDetails productDetails);
}
