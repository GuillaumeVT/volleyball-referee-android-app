package com.tonkar.volleyballreferee.business.billing;

import android.app.Activity;
import android.util.Log;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.tonkar.volleyballreferee.business.PrefUtils;
import com.tonkar.volleyballreferee.interfaces.billing.BillingListener;
import com.tonkar.volleyballreferee.interfaces.billing.BillingService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import androidx.annotation.Nullable;

public class BillingManager implements BillingService, PurchasesUpdatedListener {

    private static final String TAG = "VBR-Billing";

    private       Activity             mActivity;
    private       BillingClient        mBillingClient;
    private       boolean              mIsServiceConnected;
    private final List<SkuDetails>     mSkuDetailsList;
    private final Map<String, Boolean> mPurchasedSkus;
    private final Set<BillingListener> mBillingListeners;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mIsServiceConnected = false;
        mSkuDetailsList = new ArrayList<>();
        mPurchasedSkus = new HashMap<>();
        mBillingListeners = new HashSet<>();

        mPurchasedSkus.put(WEB_PREMIUM, false);

        mBillingClient = BillingClient.newBuilder(mActivity).setListener(this).build();

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

        for (BillingListener listener : mBillingListeners) {
            listener.onPurchasesUpdated();
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

    @Override
    public void executeServiceRequest(Runnable runnable) {
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

                        for (BillingListener listener : mBillingListeners) {
                            listener.onPurchasesUpdated();
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
            PrefUtils.purchaseWebPremium(mActivity, true);
        }
    }

    @Override
    public void addBillingListener(BillingListener listener) {
        mBillingListeners.add(listener);
    }

    @Override
    public void removeBillingListener(BillingListener listener) {
        mBillingListeners.remove(listener);
    }

    @Override
    public List<SkuDetails> getSkuDetailsList() {
        return new ArrayList<>(mSkuDetailsList);
    }

    @Override
    public boolean isPurchased(String sku) {
        return mPurchasedSkus.get(sku);
    }

    @Override
    public boolean isAllPurchased() {
        boolean allPurchased = true;

        for (String sku : IN_APP_SKUS) {
            allPurchased = allPurchased && mPurchasedSkus.get(sku);
        }

        return allPurchased;
    }

    @Override
    public void launchPurchase(String sku) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSku(sku).setType(BillingClient.SkuType.INAPP).build();
        mBillingClient.launchBillingFlow(mActivity, flowParams);
    }

}
