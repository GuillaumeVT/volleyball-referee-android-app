package com.tonkar.volleyballreferee.engine.billing;

import android.app.Activity;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;

import com.android.billingclient.api.AcknowledgePurchaseParams;
import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingClientStateListener;
import com.android.billingclient.api.BillingFlowParams;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsParams;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class BillingManager implements BillingService, PurchasesUpdatedListener {

    private       Activity             mActivity;
    private       BillingClient        mBillingClient;
    private       boolean              mIsServiceConnected;
    private final List<String>         mInAppSkus;
    private final List<SkuDetails>     mSkuDetailsList;
    private final Map<String, Boolean> mPurchasedSkus;
    private final Set<BillingListener> mBillingListeners;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mIsServiceConnected = false;
        mInAppSkus = new ArrayList<>();
        mSkuDetailsList = new ArrayList<>();
        mPurchasedSkus = new HashMap<>();
        mBillingListeners = new HashSet<>();

        mInAppSkus.add(WEB_PREMIUM);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mInAppSkus.add(SCORE_SHEETS);
        }

        mPurchasedSkus.put(WEB_PREMIUM, false);
        mPurchasedSkus.put(SCORE_SHEETS, false);

        mBillingClient = BillingClient.newBuilder(mActivity).enablePendingPurchases().setListener(this).build();

        startServiceConnection(() -> {
            querySkuList();
            queryPurchases();
        });
    }

    @Override
    public void onPurchasesUpdated(BillingResult billingResult, @Nullable List<Purchase> purchases) {
        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
            if (purchases != null && purchases.size() > 0) {
                for (Purchase purchase : purchases) {
                    handlePurchase(purchase);
                }
            } else if (PrefUtils.isWebPremiumPurchased(mActivity)) {
                mPurchasedSkus.put(WEB_PREMIUM, false);
                PrefUtils.unpurchaseWebPremium(mActivity);
                PrefUtils.signOut(mActivity);
                mActivity.recreate();
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
        params.setSkusList(mInAppSkus).setType(BillingClient.SkuType.INAPP);
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
        if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
            if (purchase.getSku().equals(BillingService.WEB_PREMIUM)) {
                mPurchasedSkus.put(WEB_PREMIUM, true);
                if (!PrefUtils.isWebPremiumPurchased(mActivity)) {
                    PrefUtils.purchaseWebPremium(mActivity, purchase.getPurchaseToken());
                    mActivity.recreate();
                }
            } else if (purchase.getSku().equals(BillingService.SCORE_SHEETS)) {
                mPurchasedSkus.put(SCORE_SHEETS, true);
                if (!PrefUtils.isScoreSheetsPurchased(mActivity)) {
                    PrefUtils.purchaseScoreSheets(mActivity, purchase.getPurchaseToken());
                    mActivity.recreate();
                }
            }

            if (!purchase.isAcknowledged()) {
                AcknowledgePurchaseParams acknowledgePurchaseParams = AcknowledgePurchaseParams
                        .newBuilder()
                        .setPurchaseToken(purchase.getPurchaseToken())
                        .build();
                mBillingClient.acknowledgePurchase(acknowledgePurchaseParams, billingResult -> {});
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

        for (String sku : mInAppSkus) {
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
