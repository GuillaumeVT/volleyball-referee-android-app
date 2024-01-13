package com.tonkar.volleyballreferee.ui.credit;

import android.os.Bundle;
import android.view.*;

import androidx.fragment.app.Fragment;

import com.tonkar.volleyballreferee.R;

public class CreditFragment extends Fragment {

    public CreditFragment() {}

    public static CreditFragment newInstance() {
        return new CreditFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_credit, container, false);
    }
}