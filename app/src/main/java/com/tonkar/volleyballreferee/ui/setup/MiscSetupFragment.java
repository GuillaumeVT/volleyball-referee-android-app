package com.tonkar.volleyballreferee.ui.setup;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;

import com.tonkar.volleyballreferee.R;
import com.tonkar.volleyballreferee.business.ServicesProvider;

import java.util.ArrayList;

public class MiscSetupFragment extends Fragment {

    public MiscSetupFragment() {
    }

    public static MiscSetupFragment newInstance() {
        MiscSetupFragment fragment = new MiscSetupFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i("VBR-TSActivity", "Create misc setup fragment");
        View view = inflater.inflate(R.layout.fragment_misc_setup, container, false);

        if (ServicesProvider.getInstance().areSetupServicesUnavailable()) {
            ServicesProvider.getInstance().restoreGameServiceForSetup(getActivity().getApplicationContext());
        }

        final AutoCompleteTextView leagueNameInput = view.findViewById(R.id.league_name_input_text);
        leagueNameInput.setThreshold(2);
        ArrayAdapter<String> leagueNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedLeagues()));
        leagueNameInput.setAdapter(leagueNameAdapter);
        leagueNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-MSActivity", "Update league name");
                ServicesProvider.getInstance().getGeneralService().setLeagueName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        leagueNameInput.setText(ServicesProvider.getInstance().getGeneralService().getLeagueName());

        final AutoCompleteTextView divisionNameInput = view.findViewById(R.id.division_name_input_text);
        divisionNameInput.setThreshold(2);
        ArrayAdapter<String> divisionNameAdapter = new ArrayAdapter<>(getContext(), R.layout.autocomplete_list_item, new ArrayList<>(ServicesProvider.getInstance().getRecordedGamesService().getRecordedDivisions()));
        divisionNameInput.setAdapter(divisionNameAdapter);
        divisionNameInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.i("VBR-MSActivity", "Update division name");
                ServicesProvider.getInstance().getGeneralService().setDivisionName(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        divisionNameInput.setText(ServicesProvider.getInstance().getGeneralService().getDivisionName());

        return view;
    }

}