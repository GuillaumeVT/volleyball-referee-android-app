package com.tonkar.volleyballreferee.business.billing;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.billing.BillingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BillingManager implements BillingService, PurchasesUpdatedListener {

    private static final String TAG = "VBR-Billing";

    private Context              mContext;
    private BillingClient        mBillingClient;
    private boolean              mIsServiceConnected;
    private List<SkuDetails>     mSkuDetailsList;
    private Map<String, Boolean> mPurchasedSkus;

    public BillingManager(Context context) {
        mContext = context;
        mIsServiceConnected = false;
        mSkuDetailsList = new ArrayList<>();
        mPurchasedSkus = new HashMap<>();

        mPurchasedSkus.put(WEB_PREMIUM, false);

        mBillingClient = BillingClient.newBuilder(mContext).setListener(this).build();

        startServiceConnection(new Runnable() {
            @Override
            public void run() {
                querySkuList();
                queryPurchases();
            }
        });
    }

    @Override
    public void onPurchasesUpdated(int responseCode, @Nullable List<Purchase> purchases) {
        if (responseCode == BillingClient.BillingResponse.OK) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (responseCode == BillingClient.BillingResponse.USER_CANCELED) {
            Log.w(TAG, "Purchase canceled by user");
        } else {
            Log.w(TAG, String.format("Unknown purchase response code code %d", responseCode));
        }
    }

    private void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(@BillingClient.BillingResponse int billingResponseCode) {
                Log.d(TAG, String.format("Billing setup finished with response code %d", billingResponseCode));

                if (billingResponseCode == BillingClient.BillingResponse.OK) {
                    mIsServiceConnected = true;
                    if (executeOnSuccess != null) {
                        executeOnSuccess.run();
                    }
                }
            }

            @Override
            public void onBillingServiceDisconnected() {
                mIsServiceConnected = false;
            }
        });
    }

    private void executeServiceRequest(Runnable runnable) {
        if (mIsServiceConnected) {
            runnable.run();
        } else {
            startServiceConnection(runnable);
        }
    }

    private void querySkuList() {
        SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
        params.setSkusList(Arrays.asList(BillingService.IN_APP_SKUS)).setType(BillingClient.SkuType.INAPP);
        mBillingClient.querySkuDetailsAsync(
                params.build(),
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(int responseCode, List<SkuDetails> skuDetailsList) {
                        if (responseCode == BillingClient.BillingResponse.OK && skuDetailsList != null) {
                            mSkuDetailsList.clear();
                            mSkuDetailsList.addAll(skuDetailsList);
                        }
                    }
                });
    }

    private void queryPurchases() {
        Runnable queryToExecute = new Runnable() {
            @Override
            public void run() {
                Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
                onPurchasesUpdated(purchasesResult.getResponseCode(), purchasesResult.getPurchasesList());
            }
        };

        executeServiceRequest(queryToExecute);
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getSku().equals(BillingService.WEB_PREMIUM)) {
            mPurchasedSkus.put(WEB_PREMIUM, true);
            PrefUtils.purchaseWebPremium(mContext, true);
        }
    }

    @Override
    public List<SkuDetails> getSkuDetailsList() {
        return new ArrayList<>(mSkuDetailsList);
    }

    @Override
    public boolean isPurchased(String sku) {
        return mPurchasedSkus.get(sku);
    }

}
