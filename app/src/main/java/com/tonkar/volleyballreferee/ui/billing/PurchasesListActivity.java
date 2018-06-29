package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ListView;

import com.tonkar.volleyballreferee.R;

public class PurchasesListActivity extends AppCompatActivity {

    private static final String TAG = "VBR-BuyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "Create purchases list activity");
        setContentView(R.layout.activity_purchases_list);

        setTitle(getResources().getString(R.string.purchase));

        final ListView purchasesList = findViewById(R.id.purchases_list);
        final PurchasesListAdapter purchasesListAdapter = new PurchasesListAdapter(this, getLayoutInflater());
        purchasesList.setAdapter(purchasesListAdapter);
    }
}
