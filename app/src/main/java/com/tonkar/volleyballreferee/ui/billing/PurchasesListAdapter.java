package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.android.billingclient.api.SkuDetails;
import com.google.android.material.button.MaterialButton;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

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
        mPurchasesList = mBillingService.getSkuDetailsList();
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
            purchaseView = mLayoutInflater.inflate(R.layout.purchases_list_item, null);
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
        viewHolder.purchaseTitle.setText(skuDetails.getTitle());
        switch (skuDetails.getSku()) {
            case BillingService.WEB_PREMIUM:
                viewHolder.purchaseSummary.setText(R.string.purchase_web_premium_summary);
                break;
            case BillingService.SCORE_SHEETS:
                viewHolder.purchaseSummary.setText(R.string.purchase_score_sheets_summary);
                break;
            default:
                viewHolder.purchaseSummary.setText("");
                break;
        }

        if (mBillingService.isPurchased(skuDetails.getSku())) {
            viewHolder.purchaseButton.setText(R.string.already_purchased);
            viewHolder.purchaseButton.setOnClickListener(null);
            viewHolder.purchaseButton.setClickable(false);
            viewHolder.purchaseButton.setIconResource(R.drawable.ic_check);
            viewHolder.purchaseButton.setIconTint(ColorStateList.valueOf(getContext().getResources().getColor(R.color.colorPrimary)));
        } else {
            viewHolder.purchaseButton.setText(skuDetails.getPrice());
            viewHolder.purchaseButton.setClickable(true);
            viewHolder.purchaseButton.setIconResource(R.drawable.ic_purchase);
            viewHolder.purchaseButton.setIconTint(ColorStateList.valueOf(getContext().getResources().getColor(R.color.colorPrimary)));
            viewHolder.purchaseButton.setOnClickListener(view -> mBillingService.executeServiceRequest(() -> mBillingService.launchPurchase(skuDetails)));
        }
    }

    void updatePurchasesList() {
        mPurchasesList.clear();
        mPurchasesList.addAll(mBillingService.getSkuDetailsList());
        notifyDataSetChanged();
    }
}
