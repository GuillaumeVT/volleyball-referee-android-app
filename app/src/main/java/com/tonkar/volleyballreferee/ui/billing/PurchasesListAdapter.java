package com.tonkar.volleyballreferee.ui.billing;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;

import com.android.billingclient.api.ProductDetails;
import com.tonkar.volleyballreferee.engine.billing.BillingService;

public class PurchasesListAdapter extends ListAdapter<ProductDetails, PurchaseViewHolder> {

    private final BillingService mBillingService;

    PurchasesListAdapter(@NonNull DiffUtil.ItemCallback<ProductDetails> diffCallback, BillingService billingService) {
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
        ProductDetails productDetails = getItem(position);
        holder.bind(productDetails, mBillingService);
    }

    static class PurchaseDiff extends DiffUtil.ItemCallback<ProductDetails> {
        @Override
        public boolean areItemsTheSame(@NonNull ProductDetails oldItem, @NonNull ProductDetails newItem) {
            return oldItem == newItem;
        }

        @Override
        public boolean areContentsTheSame(@NonNull ProductDetails oldItem, @NonNull ProductDetails newItem) {
            return oldItem.getProductId().equals(newItem.getProductId());
        }
    }
}
