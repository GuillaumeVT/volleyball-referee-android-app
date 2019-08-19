package com.tonkar.volleyballreferee.engine.billing;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.Nullable;
import com.android.billingclient.api.*;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;

import java.util.*;

public class BillingManager implements BillingService, PurchasesUpdatedListener {

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

        mBillingClient = BillingClient.newBuilder(mActivity).enablePendingPurchases().setListener(this).build();

        startServiceConnection(() -> {
            querySkuList();
            queryPurchases();
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (Purchase purchase : purchases) {
                handlePurchase(purchase);
            }
        } else if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.USER_CANCELED) {
            Log.w(Tags.BILLING, "Purchase canceled by user");
        } else {
            Log.w(Tags.BILLING, String.format("Unknown purchase response code code %d", billingResult.getResponseCode()));
        }

        for (BillingListener listener : mBillingListeners) {
            listener.onPurchasesUpdated();
        }
    }

    private void startServiceConnection(final Runnable executeOnSuccess) {
        mBillingClient.startConnection(new BillingClientStateListener() {
            @Override
            public void onBillingSetupFinished(BillingResult billingResult) {
                Log.d(Tags.BILLING, String.format("Billing setup finished with response code %d", billingResult.getResponseCode()));
                if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
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
                (billingResult, skuDetailsList) -> {
                    if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        mSkuDetailsList.clear();
                        mSkuDetailsList.addAll(skuDetailsList);
                    }

                    for (BillingListener listener : mBillingListeners) {
                        listener.onPurchasesUpdated();
                    }
                });
    }

    private void queryPurchases() {
        Runnable queryToExecute = () -> {
            Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(BillingClient.SkuType.INAPP);
            onPurchasesUpdated(purchasesResult.getBillingResult(), purchasesResult.getPurchasesList());
        };

        executeServiceRequest(queryToExecute);
    }

    private void handlePurchase(Purchase purchase) {
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED && purchase.getSku().equals(BillingService.WEB_PREMIUM)) {
            mPurchasedSkus.put(WEB_PREMIUM, true);
            if (!PrefUtils.isWebPremiumPurchased(mActivity)) {
                PrefUtils.purchaseWebPremium(mActivity, purchase.getPurchaseToken());
                mActivity.recreate();
            }
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
    public void launchPurchase(SkuDetails skuDetails) {
        BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
        mBillingClient.launchBillingFlow(mActivity, flowParams);
    }

}
