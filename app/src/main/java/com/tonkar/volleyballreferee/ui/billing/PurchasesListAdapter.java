package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.android.billingclient.api.SkuDetails;
import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.billing.BillingService;

import java.util.List;

public class PurchasesListAdapter extends ArrayAdapter<SkuDetails> {

    static class ViewHolder {
        TextView  purchaseTitle;
        TextView  purchaseSummary;
        Button    purchaseButton;
    }

    private final Context          mContext;
    private final LayoutInflater   mLayoutInflater;
    private final List<SkuDetails> mPurchasesList;
    private final BillingService   mBillingService;

    PurchasesListAdapter(Context context, LayoutInflater layoutInflater, BillingService billingService) {
        super(context, R.layout.purchases_list_item);
        mContext = context;
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
    public View getView(int index, View view, ViewGroup viewGroup) {
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

    private void updatePurchase(ViewHolder viewHolder, SkuDetails skuDetails) {
        viewHolder.purchaseTitle.setText(skuDetails.getTitle());
        viewHolder.purchaseSummary.setText(skuDetails.getDescription());

        if (mBillingService.isPurchased(skuDetails.getSku())) {
            viewHolder.purchaseButton.setText(R.string.already_purchased);
            viewHolder.purchaseButton.setOnClickListener(null);
            viewHolder.purchaseButton.setClickable(false);
        } else {
            viewHolder.purchaseButton.setText(skuDetails.getPrice());
            viewHolder.purchaseButton.setClickable(true);
            viewHolder.purchaseButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // TODO start purchase
                }
            });
        }
    }
}
