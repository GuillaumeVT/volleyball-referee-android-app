package com.tonkar.volleyballreferee.engine.billing;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillingManager implements BillingService {

    private       Activity                   mActivity;
    private       InAppBillingManager        mInAppBillingManager;
    private       SubscriptionBillingManager mSubscriptionBillingManager;
    private final Set<BillingListener>       mBillingListeners;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingListeners = new HashSet<>();

        mInAppBillingManager = new InAppBillingManager();
        mSubscriptionBillingManager = new SubscriptionBillingManager();
    }

    private abstract class AbstractBillingManager implements PurchasesUpdatedListener {
        String        mSkuType;
        BillingClient mBillingClient;
        boolean       mIsServiceConnected;
        final List<String>     mSkus;
        final List<SkuDetails> mSkuDetailsList;
        final Set<String>      mPurchasedSkus;

        AbstractBillingManager(String skuType) {
            mSkuType = skuType;
            mIsServiceConnected = false;
            mSkus = new ArrayList<>();
            mSkuDetailsList = new ArrayList<>();
            mPurchasedSkus = new HashSet<>();
            mBillingClient = BillingClient.newBuilder(mActivity).enablePendingPurchases().setListener(this).build();
        }

        void startServiceConnection(final Runnable executeOnSuccess) {
            mBillingClient.startConnection(new BillingClientStateListener() {
                @Override
                public void onBillingSetupFinished(@NonNull BillingResult billingResult) {
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

        void querySkuList() {
            SkuDetailsParams.Builder params = SkuDetailsParams.newBuilder();
            params.setSkusList(mSkus).setType(mSkuType);
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

        void queryPurchases() {
            Runnable queryToExecute = () -> {
                Purchase.PurchasesResult purchasesResult = mBillingClient.queryPurchases(mSkuType);
                onPurchasesUpdated(purchasesResult.getBillingResult(), purchasesResult.getPurchasesList());
            };

            executeServiceRequest(mSkuType, queryToExecute);
        }

        void launchPurchase(SkuDetails skuDetails) {
            BillingFlowParams flowParams = BillingFlowParams.newBuilder().setSkuDetails(skuDetails).build();
            mBillingClient.launchBillingFlow(mActivity, flowParams);
        }
    }

    private class InAppBillingManager extends AbstractBillingManager {

        InAppBillingManager() {
            super(BillingClient.SkuType.INAPP);
            mSkus.add(WEB_PREMIUM);
            mSkus.add(SCORE_SHEETS);

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
                    mPurchasedSkus.remove(WEB_PREMIUM);
                    PrefUtils.unpurchaseWebPremium(mActivity);
                    PrefUtils.signOut(mActivity);
                    mActivity.recreate();
                } else if (PrefUtils.isScoreSheetsPurchased(mActivity)) {
                    mPurchasedSkus.remove(SCORE_SHEETS);
                    PrefUtils.unpurchaseScoreSheets(mActivity);
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

        private void handlePurchase(Purchase purchase) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (purchase.getSku().equals(BillingService.WEB_PREMIUM)) {
                    mPurchasedSkus.add(WEB_PREMIUM);
                    if (!PrefUtils.isWebPremiumPurchased(mActivity)) {
                        PrefUtils.purchaseWebPremium(mActivity, purchase.getPurchaseToken());
                        mActivity.recreate();
                    }
                } else if (purchase.getSku().equals(BillingService.SCORE_SHEETS)) {
                    mPurchasedSkus.add(SCORE_SHEETS);
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
    }

    private class SubscriptionBillingManager extends AbstractBillingManager {

        SubscriptionBillingManager() {
            super(BillingClient.SkuType.SUBS);
            mSkus.add(WEB_PREMIUM_SUBSCRIPTION);

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
                } else if (PrefUtils.isWebPremiumSubscribed(mActivity)) {
                    mPurchasedSkus.remove(WEB_PREMIUM_SUBSCRIPTION);
                    PrefUtils.unsubscribeWebPremium(mActivity);
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

        private void handlePurchase(Purchase purchase) {
            if (purchase.getPurchaseState() == Purchase.PurchaseState.PURCHASED) {
                if (purchase.getSku().equals(BillingService.WEB_PREMIUM_SUBSCRIPTION)) {
                    mPurchasedSkus.add(WEB_PREMIUM_SUBSCRIPTION);
                    if (PrefUtils.isWebPremiumSubscribed(mActivity)) {
                        if (!purchase.getPurchaseToken().equals(PrefUtils.getWebPremiumBillingToken(mActivity))) {
                            PrefUtils.subscribeWebPremium(mActivity, purchase.getPurchaseToken());
                            mActivity.recreate();
                        }
                    } else {
                        PrefUtils.subscribeWebPremium(mActivity, purchase.getPurchaseToken());
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
    }

    @Override
    public void executeServiceRequest(String skuType, Runnable runnable) {
        AbstractBillingManager abstractBillingManager = BillingClient.SkuType.INAPP.equals(skuType) ? mInAppBillingManager : mSubscriptionBillingManager;

        if (abstractBillingManager.mIsServiceConnected) {
            runnable.run();
        } else {
            abstractBillingManager.startServiceConnection(runnable);
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
    public List<SkuDetails> getSkuDetailsList(String skuType) {
        return BillingClient.SkuType.INAPP.equals(skuType) ? mInAppBillingManager.mSkuDetailsList : mSubscriptionBillingManager.mSkuDetailsList;
    }

    @Override
    public boolean isPurchased(String skuType, String sku) {
        return BillingClient.SkuType.INAPP.equals(skuType) ? mInAppBillingManager.mPurchasedSkus.contains(sku) : mSubscriptionBillingManager.mPurchasedSkus.contains(sku);
    }

    @Override
    public boolean isAllPurchased() {
        boolean hasWebPremium = false;
        boolean hasWebPremiumSubscription = false;
        boolean hasScoreSheets = false;

        for (String sku : mInAppBillingManager.mSkus) {
            switch (sku) {
                case SCORE_SHEETS:
                    hasScoreSheets = mInAppBillingManager.mPurchasedSkus.contains(sku);
                    break;
                case WEB_PREMIUM:
                    hasWebPremium = mInAppBillingManager.mPurchasedSkus.contains(sku);
                    break;
            }
        }

        for (String sku : mSubscriptionBillingManager.mSkus) {
            if (WEB_PREMIUM_SUBSCRIPTION.equals(sku)) {
                hasWebPremiumSubscription = mSubscriptionBillingManager.mPurchasedSkus.contains(sku);
            }
        }

        return (hasWebPremium || hasWebPremiumSubscription) && hasScoreSheets;
    }

    @Override
    public void launchPurchase(String skuType, SkuDetails skuDetails) {
        AbstractBillingManager abstractBillingManager = BillingClient.SkuType.INAPP.equals(skuType) ? mInAppBillingManager : mSubscriptionBillingManager;
        abstractBillingManager.launchPurchase(skuDetails);
    }

}
