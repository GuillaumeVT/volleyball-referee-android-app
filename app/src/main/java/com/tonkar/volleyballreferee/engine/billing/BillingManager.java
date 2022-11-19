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
import com.android.billingclient.api.ProductDetails;
import com.android.billingclient.api.Purchase;
import com.android.billingclient.api.PurchasesUpdatedListener;
import com.android.billingclient.api.QueryProductDetailsParams;
import com.android.billingclient.api.QueryPurchasesParams;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.Tags;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BillingManager implements BillingService {

    private final Activity                   mActivity;
    private final InAppBillingManager        mInAppBillingManager;
    private final SubscriptionBillingManager mSubscriptionBillingManager;
    private final Set<BillingListener>       mBillingListeners;

    public BillingManager(Activity activity) {
        mActivity = activity;
        mBillingListeners = new HashSet<>();

        mInAppBillingManager = new InAppBillingManager();
        mSubscriptionBillingManager = new SubscriptionBillingManager();
    }

    private abstract class AbstractBillingManager implements PurchasesUpdatedListener {
        protected       String               mProductType;
        protected       BillingClient        mBillingClient;
        protected       boolean              mIsServiceConnected;
        protected final List<String>         mProductIds;
        protected final List<ProductDetails> mProductDetailList;
        protected final Set<String>          mPurchasedProducts;

        AbstractBillingManager(String productType) {
            mProductType = productType;
            mIsServiceConnected = false;
            mProductIds = new ArrayList<>();
            mProductDetailList = new ArrayList<>();
            mPurchasedProducts = new HashSet<>();
            mBillingClient = BillingClient
                    .newBuilder(mActivity)
                    .enablePendingPurchases()
                    .setListener(this)
                    .build();
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

        void queryProductList() {
            List<QueryProductDetailsParams.Product> productList = new ArrayList<>();

            for (String productId: mProductIds) {
                productList.add(
                        QueryProductDetailsParams.Product
                                .newBuilder()
                                .setProductId(productId)
                                .setProductType(mProductType)
                                .build()
                );
            }

            QueryProductDetailsParams params = QueryProductDetailsParams
                    .newBuilder()
                    .setProductList(productList)
                    .build();

            mBillingClient.queryProductDetailsAsync(
                    params,
                    (billingResult, productDetailsList) -> {
                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                            mProductDetailList.clear();
                            mProductDetailList.addAll(productDetailsList);
                        }

                        for (BillingListener listener : mBillingListeners) {
                            listener.onPurchasesUpdated();
                        }
                    });
        }

        void queryPurchases() {
            Runnable queryToExecute = () -> mBillingClient.queryPurchasesAsync(QueryPurchasesParams.newBuilder().setProductType(mProductType).build(), this::onPurchasesUpdated);
            executeServiceRequest(mProductType, queryToExecute);
        }

        void launchPurchase(ProductDetails productDetails) {
            BillingFlowParams.ProductDetailsParams productDetailsParams;

            if (BillingClient.ProductType.SUBS.equals(mProductType)) {
                String offerToken = productDetails
                        .getSubscriptionOfferDetails()
                        .get(0)
                        .getOfferToken();

                productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build();
            } else {
                productDetailsParams = BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .build();
            }

            BillingFlowParams billingFlowParams = BillingFlowParams
                    .newBuilder()
                    .setProductDetailsParamsList(List.of(productDetailsParams))
                    .build();

            mBillingClient.launchBillingFlow(mActivity, billingFlowParams);
        }
    }

    private class InAppBillingManager extends AbstractBillingManager {

        InAppBillingManager() {
            super(BillingClient.ProductType.INAPP);
            mProductIds.add(WEB_PREMIUM);
            mProductIds.add(SCORE_SHEETS);

            startServiceConnection(() -> {
                queryProductList();
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
                    mPurchasedProducts.remove(WEB_PREMIUM);
                    PrefUtils.unpurchaseWebPremium(mActivity);
                    PrefUtils.signOut(mActivity);
                    mActivity.recreate();
                } else if (PrefUtils.isScoreSheetsPurchased(mActivity)) {
                    mPurchasedProducts.remove(SCORE_SHEETS);
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
                if (purchase.getProducts().contains(BillingService.WEB_PREMIUM)) {
                    mPurchasedProducts.add(WEB_PREMIUM);
                    if (!PrefUtils.isWebPremiumPurchased(mActivity)) {
                        PrefUtils.purchaseWebPremium(mActivity, purchase.getPurchaseToken());
                        mActivity.recreate();
                    }
                } else if (purchase.getProducts().contains(BillingService.SCORE_SHEETS)) {
                    mPurchasedProducts.add(SCORE_SHEETS);
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
            super(BillingClient.ProductType.SUBS);
            mProductIds.add(WEB_PREMIUM_SUBSCRIPTION);

            startServiceConnection(() -> {
                queryProductList();
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
                    mPurchasedProducts.remove(WEB_PREMIUM_SUBSCRIPTION);
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
                if (purchase.getProducts().contains(BillingService.WEB_PREMIUM_SUBSCRIPTION)) {
                    mPurchasedProducts.add(WEB_PREMIUM_SUBSCRIPTION);
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
    public void executeServiceRequest(String productType, Runnable runnable) {
        AbstractBillingManager abstractBillingManager = BillingClient.ProductType.INAPP.equals(productType) ? mInAppBillingManager : mSubscriptionBillingManager;

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
    public List<ProductDetails> getProductDetailList(String productType) {
        return BillingClient.ProductType.INAPP.equals(productType) ? mInAppBillingManager.mProductDetailList : mSubscriptionBillingManager.mProductDetailList;
    }

    @Override
    public boolean isPurchased(String productType, String product) {
        return BillingClient.ProductType.INAPP.equals(productType) ? mInAppBillingManager.mPurchasedProducts.contains(product) : mSubscriptionBillingManager.mPurchasedProducts.contains(product);
    }

    @Override
    public void launchPurchase(String productType, ProductDetails productDetails) {
        AbstractBillingManager abstractBillingManager = BillingClient.ProductType.INAPP.equals(productType) ? mInAppBillingManager : mSubscriptionBillingManager;
        abstractBillingManager.launchPurchase(productDetails);
    }

}
