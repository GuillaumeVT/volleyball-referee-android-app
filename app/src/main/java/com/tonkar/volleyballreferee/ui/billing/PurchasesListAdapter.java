package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.SkuDetails;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.PrefUtils;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

import java.util.ArrayList;
import java.util.List;

public class PurchasesListAdapter extends ArrayAdapter<SkuDetails> {

    static class ViewHolder {
        TextView       purchaseTitle;
        TextView       purchaseSummary;
        MaterialButton purchaseButton;
    }

    private final LayoutInflater   mLayoutInflater;
    private final List<SkuDetails> mPurchasesList;
    private final BillingService   mBillingService;

    PurchasesListAdapter(Context context, LayoutInflater layoutInflater, BillingService billingService) {
        super(context, R.layout.purchases_list_item);
        mLayoutInflater = layoutInflater;
        mBillingService = billingService;
        mPurchasesList = new ArrayList<>();
        mPurchasesList.addAll(mBillingService.getSkuDetailsList(BillingClient.SkuType.SUBS));
        mPurchasesList.addAll(mBillingService.getSkuDetailsList(BillingClient.SkuType.INAPP));
    }

    @Override
    public int getCount() {
        return mPurchasesList.size();
    }

    @Override
    public SkuDetails getItem(int index) {
        return mPurchasesList.get(index);
    }

    @Override
    public long getItemId(int index) {
        return 0;
    }

    @Override
    public @NonNull View getView(int index, View view, @NonNull ViewGroup parent) {
        View purchaseView = view;
        ViewHolder viewHolder;

        if (purchaseView == null) {
            purchaseView = mLayoutInflater.inflate(R.layout.purchases_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.purchaseTitle = purchaseView.findViewById(R.id.purchase_title);
            viewHolder.purchaseSummary = purchaseView.findViewById(R.id.purchase_summary);
            viewHolder.purchaseButton = purchaseView.findViewById(R.id.purchase_button);
            purchaseView.setTag(viewHolder);
        }
        else {
            viewHolder = (ViewHolder) purchaseView.getTag();
        }

        SkuDetails skuDetails = getItem(index);
        updatePurchase(viewHolder, skuDetails);

        return purchaseView;
    }

    private void updatePurchase(ViewHolder viewHolder, final SkuDetails skuDetails) {
        final String skuType;

        viewHolder.purchaseTitle.setText(skuDetails.getTitle());
        switch (skuDetails.getSku()) {
            case BillingService.WEB_PREMIUM:
                viewHolder.purchaseSummary.setText(R.string.purchase_web_premium_summary);
                skuType = BillingClient.SkuType.INAPP;
                break;
            case BillingService.WEB_PREMIUM_SUBSCRIPTION:
                viewHolder.purchaseSummary.setText(R.string.purchase_web_premium_summary);
                skuType = BillingClient.SkuType.SUBS;
                break;
            case BillingService.SCORE_SHEETS:
                viewHolder.purchaseSummary.setText(R.string.purchase_score_sheets_summary);
                skuType = BillingClient.SkuType.INAPP;
                break;
            default:
                viewHolder.purchaseSummary.setText("");
                skuType = BillingClient.SkuType.INAPP;
                break;
        }

        if (mBillingService.isPurchased(skuType, skuDetails.getSku()) || (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(skuDetails.getSku()) && PrefUtils.isWebPremiumPurchased(getContext()))) {
            viewHolder.purchaseButton.setText(R.string.already_purchased);
            viewHolder.purchaseButton.setOnClickListener(null);
            viewHolder.purchaseButton.setClickable(false);
            viewHolder.purchaseButton.setIconResource(R.drawable.ic_check);
            viewHolder.purchaseButton.setIconTint(ColorStateList.valueOf(getContext().getColor(R.color.colorOnPrimary)));
        } else {
            viewHolder.purchaseButton.setText(skuDetails.getPrice());
            viewHolder.purchaseButton.setClickable(true);
            viewHolder.purchaseButton.setIconResource(R.drawable.ic_purchase);
            viewHolder.purchaseButton.setIconTint(ColorStateList.valueOf(getContext().getColor(R.color.colorOnPrimary)));
            viewHolder.purchaseButton.setOnClickListener(view -> mBillingService.executeServiceRequest(skuType, () -> mBillingService.launchPurchase(skuType, skuDetails)));
        }
    }

    void updatePurchasesList() {
        mPurchasesList.clear();
        for (SkuDetails skuDetails : mBillingService.getSkuDetailsList(BillingClient.SkuType.SUBS)) {
            if (BillingService.WEB_PREMIUM_SUBSCRIPTION.equals(skuDetails.getSku())) {
                mPurchasesList.add(skuDetails);
            }
        }
        for (SkuDetails skuDetails : mBillingService.getSkuDetailsList(BillingClient.SkuType.INAPP)) {
            if (BillingService.SCORE_SHEETS.equals(skuDetails.getSku())) {
                mPurchasesList.add(skuDetails);
            }
        }
        notifyDataSetChanged();
    }
}
