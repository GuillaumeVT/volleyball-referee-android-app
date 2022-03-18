package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
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

    void bind(SkuDetails skuDetails, BillingService billingService) {
        final String skuType;

        mPurchaseTitle.setText(skuDetails.getTitle());
        switch (skuDetails.getSku()) {
            case BillingService.WEB_PREMIUM:
                mPurchaseSummary.setText(R.string.purchase_web_premium_summary);
                skuType = BillingClient.SkuType.INAPP;
                break;
            case BillingService.WEB_PREMIUM_SUBSCRIPTION:
                mPurchaseSummary.setText(R.string.purchase_web_premium_summary);
                skuType = BillingClient.SkuType.SUBS;
                break;
            case BillingService.SCORE_SHEETS:
                mPurchaseSummary.setText(R.string.purchase_score_sheets_summary);
                skuType = BillingClient.SkuType.INAPP;
                break;
            default:
                mPurchaseSummary.setText("");
                skuType = BillingClient.SkuType.INAPP;
                break;
        }

        final Context context = mPurchaseItem.getContext();

        if (billingService.isPurchased(skuType, skuDetails.getSku()) || (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(skuDetails.getSku()) && PrefUtils.isWebPremiumPurchased(context))) {
            mPurchaseButton.setText(R.string.already_purchased);
            mPurchaseButton.setOnClickListener(null);
            mPurchaseButton.setClickable(false);
            mPurchaseButton.setIconResource(R.drawable.ic_check);
            mPurchaseButton.setIconTint(ColorStateList.valueOf(context.getColor(R.color.colorOnPrimary)));
        } else {
            mPurchaseButton.setText(skuDetails.getPrice());
            mPurchaseButton.setClickable(true);
            mPurchaseButton.setIconResource(R.drawable.ic_purchase);
            mPurchaseButton.setIconTint(ColorStateList.valueOf(context.getColor(R.color.colorOnPrimary)));
            mPurchaseButton.setOnClickListener(view -> billingService.executeServiceRequest(skuType, () -> billingService.launchPurchase(skuType, skuDetails)));
        }
    }

    static PurchaseViewHolder create(ViewGroup parent) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.purchases_list_item, parent, false);
        return new PurchaseViewHolder(view);
    }
}
