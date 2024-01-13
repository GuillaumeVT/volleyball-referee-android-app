package com.tonkar.volleyballreferee.ui.billing;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.*;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.engine.billing.*;

public class PurchaseListFragment extends Fragment {

    private BillingService       mBillingService;
    private PurchaseViewModel    mPurchaseViewModel;
    private PurchasesListAdapter mPurchasesListAdapter;

    public PurchaseListFragment() {}

    public static PurchaseListFragment newInstance() {
        return new PurchaseListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBillingService = new BillingManager(requireActivity());

        mPurchasesListAdapter = new PurchasesListAdapter(new PurchasesListAdapter.PurchaseDiff(), mBillingService);
        mPurchasesListAdapter.setHasStableIds(true);

        mPurchaseViewModel = new ViewModelProvider(this).get(PurchaseViewModel.class);
        mPurchaseViewModel.init(mBillingService);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_purchase_list, container, false);

        RecyclerView purchaseList = fragmentView.findViewById(R.id.purchases_list);

        purchaseList.setAdapter(mPurchasesListAdapter);
        purchaseList.setLayoutManager(new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false));

        mPurchaseViewModel.getPurchaseList().observe(requireActivity(), mPurchasesListAdapter::submitList);
        mBillingService.addBillingListener(mPurchaseViewModel);

        return fragmentView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBillingService.removeBillingListener(mPurchaseViewModel);
    }
}