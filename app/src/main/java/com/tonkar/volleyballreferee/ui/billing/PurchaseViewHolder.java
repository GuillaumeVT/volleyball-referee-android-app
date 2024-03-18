package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.*;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.*;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

public class PurchaseViewHolder extends RecyclerView.ViewHolder {

    private final View           mPurchaseItem;
    private final TextView       mPurchaseTitle;
    private final TextView       mPurchaseSummary;
    private final MaterialButton mPurchaseButton;

    PurchaseViewHolder(@NonNull View itemView) {
        super(itemView);
        mPurchaseItem = itemView;
        mPurchaseTitle = itemView.findViewById(R.id.purchase_title);
        mPurchaseSummary = itemView.findViewById(R.id.purchase_summary);
        mPurchaseButton = itemView.findViewById(R.id.purchase_button);
    }

    void bind(ProductDetails productDetails, BillingService billingService) {
        final String productType;

        mPurchaseTitle.setText(productDetails.getTitle());
        switch (productDetails.getProductId()) {
            case BillingService.WEB_PREMIUM -> {
                mPurchaseSummary.setText(R.string.purchase_web_premium_summary);
                productType = BillingClient.ProductType.INAPP;
            }
            case BillingService.WEB_PREMIUM_SUBSCRIPTION -> {
                mPurchaseSummary.setText(R.string.purchase_web_premium_summary);
                productType = BillingClient.ProductType.SUBS;
            }
            default -> {
                mPurchaseSummary.setText("");
                productType = BillingClient.ProductType.INAPP;
            }
        }

        final Context context = mPurchaseItem.getContext();

        if (billingService.isPurchased(productType, productDetails.getProductId()) || (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(
                productDetails.getProductId()) && PrefUtils.isWebPremiumPurchased(context))) {
            mPurchaseButton.setText(R.string.already_purchased);
            mPurchaseButton.setOnClickListener(null);
            mPurchaseButton.setClickable(false);
            mPurchaseButton.setIconResource(R.drawable.ic_check);
            mPurchaseButton.setIconTint(ColorStateList.valueOf(context.getColor(R.color.colorOnPrimary)));
        } else {
            mPurchaseButton.setText(getPrice(productDetails, productType));
            mPurchaseButton.setClickable(true);
            mPurchaseButton.setIconResource(R.drawable.ic_purchase);
            mPurchaseButton.setIconTint(ColorStateList.valueOf(context.getColor(R.color.colorOnPrimary)));
            mPurchaseButton.setOnClickListener(view -> billingService.executeServiceRequest(productType,
                                                                                            () -> billingService.launchPurchase(productType,
                                                                                                                                productDetails)));
        }
    }

    private String getPrice(ProductDetails productDetails, String productType) {
        if (BillingClient.ProductType.SUBS.equals(productType)) {
            return productDetails.getSubscriptionOfferDetails().get(0).getPricingPhases().getPricingPhaseList().get(0).getFormattedPrice();
        } else {
            return productDetails.getOneTimePurchaseOfferDetails().getFormattedPrice();
        }
    }

    static PurchaseViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchases_list_item, parent, false);
        return new PurchaseViewHolder(view);
    }
}
