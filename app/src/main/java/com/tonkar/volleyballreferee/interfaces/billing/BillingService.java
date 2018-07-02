package com.tonkar.volleyballreferee.interfaces.billing;

import com.android.billingclient.api.SkuDetails;

import java.util.List;

public interface BillingService {

    String WEB_PREMIUM = "vbr_web_premium";

    String[] IN_APP_SKUS = { WEB_PREMIUM };

    void addBillingListener(BillingListener listener);

    void removeBillingListener(BillingListener listener);

    List<SkuDetails> getSkuDetailsList();

    boolean isPurchased(String sku);

    void launchPurchase(String sku);
}
