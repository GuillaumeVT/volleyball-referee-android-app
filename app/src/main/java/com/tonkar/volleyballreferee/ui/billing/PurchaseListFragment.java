package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;

public class PurchaseListFragment extends Fragment {

    public PurchaseListFragment() {}

    public static PurchaseListFragment newInstance() {
        return new PurchaseListFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_purchase_list, container, false);
    }
}