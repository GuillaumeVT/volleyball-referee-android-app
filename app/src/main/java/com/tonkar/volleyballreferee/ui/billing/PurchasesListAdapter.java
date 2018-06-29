package com.tonkar.volleyballreferee.ui.billing;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.interfaces.billing.BillingInterface;

import java.util.Arrays;
import java.util.List;

public class PurchasesListAdapter extends ArrayAdapter<String> {

    static class ViewHolder {
        TextView  purchaseTitle;
        TextView  purchaseSummary;
        Button    purchaseButton;
    }

    private final Context        mContext;
    private final LayoutInflater mLayoutInflater;
    private final List<String>   mPurchasesList;

    PurchasesListAdapter(Context context, LayoutInflater layoutInflater) {
        super(context, R.layout.purchases_list_item);
        mContext = context;
        mLayoutInflater = layoutInflater;
        mPurchasesList = Arrays.asList(BillingInterface.IN_APP_SKUS);
    }

    @Override
    public int getCount() {
        return mPurchasesList.size();
    }

    @Override
    public String getItem(int index) {
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

        String purchase = getItem(index);
        updatePurchase(viewHolder, purchase);

        return purchaseView;
    }

    private void updatePurchase(ViewHolder viewHolder, String purchase) {
        switch (purchase) {
            case BillingInterface.WEB_PREMIUM:
                viewHolder.purchaseTitle.setText(R.string.purchase_web_premium_title);
                viewHolder.purchaseSummary.setText(R.string.purchase_web_premium_summary);
                // TODO viewHolder.purchaseButton.setVisibility();
                viewHolder.purchaseButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // TODO start purchase
                    }
                });
                break;
            default:
                break;
        }
    }
}
