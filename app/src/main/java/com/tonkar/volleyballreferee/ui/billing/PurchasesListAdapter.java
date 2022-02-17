package com.tonkar.volleyballreferee.ui.billing;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.android.billingclient.api.SkuDetails;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

public class PurchasesListAdapter extends ListAdapter<SkuDetails, PurchaseViewHolder> {

    private final BillingService mBillingService;

    PurchasesListAdapter(@NonNull DiffUtil.ItemCallback<SkuDetails> diffCallback, BillingService billingService) {
        super(diffCallback);
        mBillingService = billingService;
    }

    @NonNull
    @Override
    public PurchaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return PurchaseViewHolder.create(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull PurchaseViewHolder holder, int position) {
        SkuDetails skuDetails = getItem(position);
        holder.bind(skuDetails, mBillingService);
    }

    static class PurchaseDiff extends DiffUtil.ItemCallback<SkuDetails> {
        @Override
        public boolean areItemsTheSame(@NonNull SkuDetails oldItem, @NonNull SkuDetails newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull SkuDetails oldItem, @NonNull SkuDetails newItem) {
            return oldItem.getSku().equals(newItem.getSku());
        }
    }
}
